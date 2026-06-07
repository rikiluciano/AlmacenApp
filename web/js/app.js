// ═══════════════ FIREBASE CONFIGURATION ═══════════════
const firebaseConfig = {
    apiKey: "AIzaSyCWiqN_UFYIrLhnmlxxQAsPx0smBpAGWug",
    authDomain: "almacen-inteligente-2f515.firebaseapp.com",
    projectId: "almacen-inteligente-2f515",
    storageBucket: "almacen-inteligente-2f515.firebasestorage.app",
    messagingSenderId: "110904781496",
    appId: "1:110904781496:web:27db66c1144c0a7503624d",
    measurementId: "G-T1F0BE2FNC"
};

// Initialize Firebase
firebase.initializeApp(firebaseConfig);
const db = firebase.firestore();
const storage = firebase.storage();
// Disable network cache issues by forcing network
db.settings({ cacheSizeBytes: firebase.firestore.CACHE_SIZE_UNLIMITED });

// ════════ TOAST NOTIFICATIONS ════════
function showToast(message, type = 'success') {
    const container = document.getElementById('toast-container');
    if (!container) return;
    
    const toast = document.createElement('div');
    toast.className = 'toast ' + type;
    
    let icon = 'check_circle';
    if (type === 'error') icon = 'error';
    else if (type === 'info') icon = 'info';
    
    toast.innerHTML = `
        <span class="material-icons-round toast-icon">${icon}</span>
        <div class="toast-content">
            <p class="toast-message">${message}</p>
        </div>
    `;
    
    container.appendChild(toast);
    
    // Animate in
    setTimeout(() => toast.classList.add('show'), 10);
    
    // Remove after 4 seconds
    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => toast.remove(), 400);
    }, 4000);
}


// ═══════════════ GLOBAL STATE ═══════════════
let state = {
    items: [],
    warehouses: [],
    classes: [],
    categories: [],
    machines: [],
    units: [],
    areasDepts: [],
    activePage: "inventory",
    selectedItem: null,
    searchQuery: "",
    selectedFile: null,
    currentEditPhotoUrl: null
};

// Mappings for quick lookup
let warehouseMap = {};
let classMap = {};
let categoryMap = {};
let machineMap = {};
let areaDeptMap = {};

// ═══════════════ INITIALIZATION ═══════════════
window.addEventListener("DOMContentLoaded", () => {
    setupNavigation();
    setupSearch();
    setupLiveTime();
    setupModal();
    setupCatalogForms();
    setupForm();
    initRealtimeSync();
});

// ═══════════════ REALTIME SYNC ═══════════════
function initRealtimeSync() {
    const syncBadge = document.getElementById("sync-badge");
    const syncText = document.getElementById("sync-text");

    const setConnected = (connected) => {
        if (connected) {
            syncBadge.classList.remove("error");
            syncText.innerText = "Sincronizado";
        } else {
            syncBadge.classList.add("error");
            syncText.innerText = "Sin Conexión";
        }
    };

    // 1. Listen for items
    db.collection("items").orderBy("name").onSnapshot(snapshot => {
        state.items = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
        renderItems();
        updateStats();
        setConnected(true);
    }, error => {
        console.error("Error listening to items:", error);
        setConnected(false);
        // Clear spinner and show error in grid
        document.getElementById("items-grid").innerHTML = `
            <div class="empty-state">
                <span class="material-icons-round" style="color:#ef4444;">error_outline</span>
                <p style="color:#ef4444;">Error de conexión a la base de datos.</p>
                <p style="font-size:0.9em; opacity:0.7;">Por favor, verifica las reglas de Firestore o tu conexión a internet.</p>
            </div>
        `;
    });

    // 2. Listen for warehouses
    db.collection("warehouses").orderBy("code").onSnapshot(snapshot => {
        state.warehouses = snapshot.docs.map(doc => doc.data());
        warehouseMap = {};
        state.warehouses.forEach(w => { warehouseMap[w.code] = w.name; });
        renderCatalogs();
        updateStats();
        renderItems();
        populateSelect("add-c-wcode", state.warehouses, "code", "name", "Seleccione Bodega *");
        // Maintain format: 'code - name'
        const wSelectOpts = state.warehouses.map(w => ({ code: w.code, label: `${w.code} - ${w.name}` }));
        populateSelectRaw("add-c-wcode", wSelectOpts, "code", "label", "Seleccione Bodega *");
    });

    // 3. Listen for classes
    db.collection("classes").orderBy("code").onSnapshot(snapshot => {
        state.classes = snapshot.docs.map(doc => doc.data());
        classMap = {};
        state.classes.forEach(c => { classMap[c.code] = c; });
        renderCatalogs();
        updateStats();
        renderItems();
        const cSelectOpts = state.classes.map(c => ({ code: c.code, label: `${c.code} - ${c.name}` }));
        populateSelectRaw("add-cat-ccode", cSelectOpts, "code", "label", "Seleccione Clase *");
    });

    // 4. Listen for categories
    db.collection("categories").orderBy("code").onSnapshot(snapshot => {
        state.categories = snapshot.docs.map(doc => doc.data());
        categoryMap = {};
        state.categories.forEach(c => { categoryMap[`${c.classCode}-${c.code}`] = c.name; });
        renderCatalogs();
        updateStats();
        renderItems();
    });

    // 5. Listen for machines
    db.collection("machines").orderBy("code").onSnapshot(snapshot => {
        state.machines = snapshot.docs.map(doc => doc.data());
        machineMap = {};
        state.machines.forEach(m => { machineMap[m.code] = m.name; });
        renderCatalogs();
        updateStats();
        renderItems();
        populateSelect("f-machine", state.machines, "code", "name", "Sin máquina");
    });

    // 6. Listen for units
    db.collection("units").orderBy("name").onSnapshot(snapshot => {
        state.units = snapshot.docs.map(doc => doc.data());
        renderCatalogs();
        populateSelect("f-unit", state.units, "name", "name", "Sin unidad");
    });

    // 7. Listen for areas_depts
    db.collection("areas_depts").orderBy("code").onSnapshot(snapshot => {
        state.areasDepts = snapshot.docs.map(doc => doc.data());
        areaDeptMap = {};
        state.areasDepts.forEach(a => { areaDeptMap[a.code] = a.name; });
        renderCatalogs();
        populateSelect("f-area-dept", state.areasDepts, "code", "name", "Sin área/departamento");
    });
}

// Helper to populate select dropdowns
function populateSelect(elementId, list, valueKey, labelKey, defaultText) {
    const select = document.getElementById(elementId);
    if (!select) return;
    select.innerHTML = `<option value="">${defaultText}</option>`;
    list.forEach(item => {
        const option = document.createElement("option");
        option.value = item[valueKey];
        option.innerText = `${item[valueKey]} - ${item[labelKey]}`;
        select.appendChild(option);
    });
}

// Helper to populate raw label/value mappings
function populateSelectRaw(elementId, list, valueKey, labelKey, defaultText) {
    const select = document.getElementById(elementId);
    if (!select) return;
    select.innerHTML = `<option value="">${defaultText}</option>`;
    list.forEach(item => {
        const option = document.createElement("option");
        option.value = item[valueKey];
        option.innerText = item[labelKey];
        select.appendChild(option);
    });
}

// ═══════════════ LIVE TIME & FOOTER ═══════════════
function setupLiveTime() {
    const timeText = document.getElementById("live-time-text");
    const yearText = document.getElementById("current-year");
    
    setInterval(() => {
        const now = new Date();
        const formatter = new Intl.DateTimeFormat('es-DO', {
            timeZone: 'America/Santo_Domingo',
            hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: true
        });
        const dateStr = new Intl.DateTimeFormat('es-DO', {
            timeZone: 'America/Santo_Domingo',
            day: '2-digit', month: '2-digit', year: 'numeric'
        }).format(now);
        
        if(timeText) timeText.innerText = `${dateStr} ${formatter.format(now)}`;
        
        const yearFormatter = new Intl.DateTimeFormat('es-DO', {
            timeZone: 'America/Santo_Domingo',
            year: 'numeric'
        });
        if(yearText) yearText.innerText = yearFormatter.format(now);
    }, 1000);
}

// ═══════════════ NAVIGATION ═══════════════
function setupNavigation() {
    const navItems = document.querySelectorAll(".nav-item");
    const pages = document.querySelectorAll(".page");

    navItems.forEach(item => {
        item.addEventListener("click", (e) => {
            e.preventDefault();
            const pageId = item.getAttribute("data-page");
            
            navItems.forEach(n => n.classList.remove("active"));
            item.classList.add("active");

            pages.forEach(p => p.classList.remove("active"));
            document.getElementById(`page-${pageId}`).classList.add("active");

            state.activePage = pageId;

            if (pageId === "add") {
                resetForm();
            }
        });
    });

    document.getElementById("btn-cancel-form").addEventListener("click", () => {
        document.getElementById("nav-inventory").click();
    });
}

// ═══════════════ SEARCH LOGIC (SMART MATCH) ═══════════════
function setupSearch() {
    const searchInput = document.getElementById("search-input");
    const searchClear = document.getElementById("search-clear");

    searchInput.addEventListener("input", (e) => {
        state.searchQuery = e.target.value;
        if (state.searchQuery) {
            searchClear.style.display = "flex";
        } else {
            searchClear.style.display = "none";
        }
        renderItems();
    });

    searchClear.addEventListener("click", () => {
        searchInput.value = "";
        state.searchQuery = "";
        searchClear.style.display = "none";
        renderItems();
    });
}

// Smart Match Algorithm (matches Android implementation)
function smartMatch(query, target) {
    if (target === null || target === undefined) return false;
    const targetStr = target.toString();
    const cleanQuery = query.toLowerCase().trim();
    if (!cleanQuery) return false;

    const delimiters = /[\s\-_/]+/;
    const queryWords = cleanQuery.split(delimiters).filter(w => w.length > 0);
    const targetWords = targetStr.toLowerCase().trim().split(delimiters).filter(w => w.length > 0);

    if (queryWords.length === 0) return false;

    return queryWords.every(qw => {
        return targetWords.some(tw => {
            return tw.startsWith(qw) && qw.length >= (tw.length / 2);
        }) || targetStr.toLowerCase().includes(qw);
    });
}

// ═══════════════ RENDERING ═══════════════
function renderItems() {
    const grid = document.getElementById("items-grid");
    const countBadge = document.getElementById("items-count-badge");
    const title = document.getElementById("items-section-title");
    grid.innerHTML = "";

    const query = state.searchQuery.trim();
    
    // Si la búsqueda está vacía o tiene menos de 3 letras, no mostrar nada
    if (query.length < 3) {
        title.innerText = "Búsqueda inteligente";
        countBadge.innerText = "0 artículos";
        grid.innerHTML = `
            <div class="empty-state">
                <span class="material-icons-round">manage_search</span>
                <p>${query.length === 0 ? "Escribe al menos 3 letras para buscar un artículo." : "Continúa escribiendo para ver resultados..."}</p>
            </div>
        `;
        return;
    }

    const filtered = state.items.filter(item => {
        return smartMatch(query, item.name) ||
               smartMatch(query, item.code) ||
               smartMatch(query, item.partNumber) ||
               smartMatch(query, item.machine) ||
               smartMatch(query, item.extraInfo) ||
               smartMatch(query, item.location) ||
               smartMatch(query, item.subCategory);
    });

    title.innerText = "Resultados de búsqueda";
    countBadge.innerText = `${filtered.length} artículo${filtered.length !== 1 ? 's' : ''}`;

    if (filtered.length === 0) {
        grid.innerHTML = `
            <div class="empty-state">
                <span class="material-icons-round">search_off</span>
                <p>No se encontraron artículos para "${query}"</p>
            </div>
        `;
        return;
    }

    filtered.forEach(item => {
        const card = document.createElement("div");
        card.className = "item-card";
        
        let imgHtml = `<span class="material-icons-round">inventory_2</span>`;
        if (item.photoPath) {
            imgHtml = `<img src="${item.photoPath}" alt="${item.name}" loading="lazy">`;
        }

        const cls = classMap[item.warehouse];
        const wCode = cls ? cls.warehouseCode : "?";
        let wNameRaw = warehouseMap[wCode] ? `${wCode} - ${warehouseMap[wCode]}` : "Desconocida";
        const wName = wNameRaw.replace(/^bodega\s*[:-]?\s*/i, "");
        const cName = classMap[item.warehouse]?.name || item.warehouse;
        const catName = categoryMap[`${item.warehouse}-${item.category}`] || item.category;
        const machName = item.machine ? (machineMap[item.machine] || item.machine) : "";

        card.innerHTML = `
            <div class="item-card-image">${imgHtml}</div>
            <div class="item-card-body">
                <div class="item-card-code-premium">${item.code}</div>
                <h4 class="item-card-name" title="${item.name}">${item.name}</h4>
                <div class="item-card-detail">
                    <span class="material-icons-round">warehouse</span>
                    <span>Bodega: <strong>${wName}</strong></span>
                </div>
                <div class="item-card-detail">
                    <span class="material-icons-round">layers</span>
                    <span>Clase: <strong>${cName}</strong></span>
                </div>
                <div class="item-card-detail">
                    <span class="material-icons-round">category</span>
                    <span>Categoría: <strong>${catName}</strong></span>
                </div>
                ${machName ? `
                <div class="item-card-detail">
                    <span class="material-icons-round">precision_manufacturing</span>
                    <span>Máquina: <strong>${machName}</strong></span>
                </div>
                ` : ""}
                ${item.areaDept ? `
                <div class="item-card-detail">
                    <span class="material-icons-round">business</span>
                    <span>Área/Depto: <strong>${areaName}</strong></span>
                </div>
                ` : ""}
                <div class="item-card-detail">
                    <span class="material-icons-round">place</span>
                    <span>Ubicación: <strong>${item.location}</strong></span>
                </div>
            </div>
        `;
        
        card.addEventListener("click", () => {
            openDetailModal(item);
        });

        grid.appendChild(card);
    });
}

function updateStats() {
    const elItems = document.getElementById("stat-items");
    const elWarehouses = document.getElementById("stat-warehouses");
    const elClasses = document.getElementById("stat-classes");
    const elMachines = document.getElementById("stat-machines");
    
    if (elItems) elItems.innerText = state.items.length;
    if (elWarehouses) elWarehouses.innerText = state.warehouses.length;
    if (elClasses) elClasses.innerText = state.classes.length;
    if (elMachines) elMachines.innerText = state.machines.length;
}

function renderCatalogs() {
    // Helper to generate list item with delete button
    const createLi = (contentHtml, deleteCallback) => {
        const li = document.createElement("li");
        
        const contentDiv = document.createElement("div");
        contentDiv.className = "catalog-list-content";
        contentDiv.innerHTML = contentHtml;
        
        const delBtn = document.createElement("button");
        delBtn.className = "catalog-delete-btn";
        delBtn.innerHTML = `<span class="material-icons-round" style="font-size:16px;">delete</span>`;
        delBtn.addEventListener("click", (e) => {
            e.stopPropagation();
            deleteCallback();
        });
        
        li.appendChild(contentDiv);
        li.appendChild(delBtn);
        return li;
    };

    // 1. Warehouses
    const wList = document.getElementById("cat-warehouses");
    wList.innerHTML = state.warehouses.length ? "" : '<li class="empty-msg">No hay bodegas registradas</li>';
    state.warehouses.forEach(w => {
        const li = createLi(`<strong>${w.code}</strong> <span>${w.name}</span>`, () => {
            if (confirm(`¿Eliminar bodega ${w.code} - ${w.name}?`)) {
                db.collection("warehouses").doc(w.code).delete().catch(err => showToast("Error: " + err.message, "error"));
            }
        });
        wList.appendChild(li);
    });

    // 2. Classes
    const cList = document.getElementById("cat-classes");
    cList.innerHTML = state.classes.length ? "" : '<li class="empty-msg">No hay clases registradas</li>';
    state.classes.forEach(c => {
        const li = createLi(`<strong>${c.code}</strong> <span>${c.name}</span> <span class="catalog-badge">B: ${c.warehouseCode}</span>`, () => {
            if (confirm(`¿Eliminar clase ${c.code} - ${c.name}?`)) {
                db.collection("classes").doc(c.code).delete().catch(err => showToast("Error: " + err.message, "error"));
            }
        });
        cList.appendChild(li);
    });

    // 3. Categories
    const catList = document.getElementById("cat-categories");
    catList.innerHTML = state.categories.length ? "" : '<li class="empty-msg">No hay categorías registradas</li>';
    state.categories.forEach(c => {
        const docId = `${c.classCode}-${c.code}`;
        const li = createLi(`<strong>${c.code}</strong> <span>${c.name}</span> <span class="catalog-badge">C: ${c.classCode}</span>`, () => {
            if (confirm(`¿Eliminar categoría ${c.code} - ${c.name}?`)) {
                db.collection("categories").doc(docId).delete().catch(err => showToast("Error: " + err.message, "error"));
            }
        });
        catList.appendChild(li);
    });

    // 4. Machines
    const mList = document.getElementById("cat-machines");
    mList.innerHTML = state.machines.length ? "" : '<li class="empty-msg">No hay máquinas registradas</li>';
    state.machines.forEach(m => {
        const li = createLi(`<strong>${m.code}</strong> <span>${m.name}</span>`, () => {
            if (confirm(`¿Eliminar máquina ${m.code} - ${m.name}?`)) {
                db.collection("machines").doc(m.code).delete().catch(err => showToast("Error: " + err.message, "error"));
            }
        });
        mList.appendChild(li);
    });

    // 5. Units
    const uList = document.getElementById("cat-units");
    uList.innerHTML = state.units.length ? "" : '<li class="empty-msg">No hay unidades registradas</li>';
    state.units.forEach(u => {
        const li = createLi(`<span>${u.name}</span>`, () => {
            if (confirm(`¿Eliminar unidad de medida "${u.name}"?`)) {
                db.collection("units").doc(u.name).delete().catch(err => showToast("Error: " + err.message, "error"));
            }
        });
        uList.appendChild(li);
    });

    // 6. Areas/Depts
    const aList = document.getElementById("cat-areas");
    if (aList) {
        aList.innerHTML = state.areasDepts.length ? "" : '<li class="empty-msg">No hay áreas/departamentos registrados</li>';
        state.areasDepts.forEach(a => {
            const li = createLi(`<strong>${a.code}</strong> <span>${a.name}</span>`, () => {
                if (confirm(`¿Eliminar área/departamento ${a.code} - ${a.name}?`)) {
                    db.collection("areas_depts").doc(a.code).delete().catch(err => showToast("Error: " + err.message, "error"));
                }
            });
            aList.appendChild(li);
        });
    }
}

// ═══════════════ CATALOG WRITE LOGIC ═══════════════
function setupCatalogForms() {
    // 1. Add Warehouse
    document.getElementById("form-add-warehouse").addEventListener("submit", async (e) => {
        e.preventDefault();
        const code = document.getElementById("add-w-code").value.trim();
        const name = document.getElementById("add-w-name").value.trim();
        if (!code) { showToast("El código es requerido.", "error"); return; }
        
        try {
            await db.collection("warehouses").doc(code).set({ code, name });
            showToast("Bodega guardada en la nube.");
            document.getElementById("form-add-warehouse").reset();
        } catch (err) {
            showToast("Error al guardar bodega: " + err.message, "error");
        }
    });

    // 2. Add Class
    document.getElementById("form-add-class").addEventListener("submit", async (e) => {
        e.preventDefault();
        const code = document.getElementById("add-c-code").value.trim();
        const name = document.getElementById("add-c-name").value.trim();
        const warehouseCode = document.getElementById("add-c-wcode").value;
        if (!code) { showToast("El código es requerido.", "error"); return; }
        if (!warehouseCode) { showToast("Seleccione una bodega.", "error"); return; }

        try {
            await db.collection("classes").doc(code).set({ code, warehouseCode, name });
            document.getElementById("form-add-class").reset();
        } catch (err) {
            showToast("Error al guardar clase: " + err.message, "error");
        }
    });

    // 3. Add Category
    document.getElementById("form-add-category").addEventListener("submit", async (e) => {
        e.preventDefault();
        const code = document.getElementById("add-cat-code").value.trim();
        const name = document.getElementById("add-cat-name").value.trim();
        const classCode = document.getElementById("add-cat-ccode").value;
        if (!code) { showToast("El código es requerido.", "error"); return; }
        if (!classCode) { showToast("Seleccione una clase.", "error"); return; }

        const docId = `${classCode}-${code}`;
        try {
            await db.collection("categories").doc(docId).set({ code, classCode, name });
            document.getElementById("form-add-category").reset();
        } catch (err) {
            showToast("Error al guardar categoría: " + err.message, "error");
        }
    });

    // 4. Add Machine
    document.getElementById("form-add-machine").addEventListener("submit", async (e) => {
        e.preventDefault();
        const code = document.getElementById("add-m-code").value.trim();
        const name = document.getElementById("add-m-name").value.trim();
        if (!code || !name) return;

        try {
            await db.collection("machines").doc(code).set({ code, name });
            document.getElementById("form-add-machine").reset();
        } catch (err) {
            showToast("Error al guardar máquina: " + err.message, "error");
        }
    });

    // Add Area/Dept
    const formAddArea = document.getElementById("form-add-area");
    if (formAddArea) {
        formAddArea.addEventListener("submit", async (e) => {
            e.preventDefault();
            const code = document.getElementById("add-a-code").value.trim();
            const name = document.getElementById("add-a-name").value.trim();
            if (!code || !name) return;
            try {
                await db.collection("areas_depts").doc(code).set({ code, name });
                formAddArea.reset();
            } catch (err) {
                showToast("Error al guardar área/depto: " + err.message, "error");
            }
        });
    }

    // 5. Add Unit
    document.getElementById("form-add-unit").addEventListener("submit", async (e) => {
        e.preventDefault();
        const name = document.getElementById("add-u-name").value.trim();

        try {
            await db.collection("units").doc(name).set({ name });
            document.getElementById("form-add-unit").reset();
        } catch (err) {
            showToast("Error al guardar unidad: " + err.message, "error");
        }
    });
}

// ═══════════════ FORM LOGIC & AUTO-DETECTION ═══════════════
function setupForm() {
    const codeInput = document.getElementById("f-code");
    const autoCard = document.getElementById("auto-detect-card");

    // Native Photo picker variables
    const fileInput = document.getElementById("f-photo-file");
    const uploadCard = document.getElementById("web-image-upload-card");
    const placeholder = document.getElementById("web-image-placeholder");
    const preview = document.getElementById("web-image-preview");
    const previewImg = document.getElementById("web-preview-img");
    const deleteBtn = document.getElementById("btn-delete-web-img");

    // Open file picker when clicking upload card
    uploadCard.addEventListener("click", () => {
        fileInput.click();
    });

    fileInput.addEventListener("change", (e) => {
        const file = e.target.files[0];
        if (file) {
            state.selectedFile = file;
            state.currentEditPhotoUrl = null;
            const reader = new FileReader();
            reader.onload = (event) => {
                previewImg.src = event.target.result;
                placeholder.style.display = "none";
                preview.style.display = "block";
            };
            reader.readAsDataURL(file);
        }
    });

    deleteBtn.addEventListener("click", (e) => {
        e.stopPropagation();
        state.selectedFile = null;
        fileInput.value = "";
        previewImg.src = "";
        preview.style.display = "none";
        placeholder.style.display = "flex";
        state.currentEditPhotoUrl = null; 
    });

    codeInput.addEventListener("input", (e) => {
        let clean = e.target.value.replace(/\D/g, "");
        if (clean.length > 15) clean = clean.substring(0, 15);
        
        // Format layout preview (AAA-BBB-CCCC...)
        let formatted = clean;
        let classCode = "", catCode = "", suffix = "";
        
        if (clean.length >= 3) {
            classCode = clean.substring(0, 3);
            formatted = classCode;
            if (clean.length >= 6) {
                catCode = clean.substring(3, 6);
                formatted += "-" + catCode;
                if (clean.length > 6) {
                    suffix = clean.substring(6);
                    formatted += "-" + suffix;
                }
            } else {
                formatted += "-" + clean.substring(3);
            }
        }
        
        document.getElementById("code-preview").innerText = formatted ? `Formato: ${formatted}` : "";

        // Detect catalogs
        if (clean.length >= 6) {
            autoCard.style.display = "flex";
            const cCode = clean.substring(0, 3);
            const caCode = clean.substring(3, 6);
            
            // Clase
            const cls = classMap[cCode];
            const clEl = document.getElementById("detect-class");
            if (cls) {
                clEl.className = "auto-detect-row found";
                clEl.innerHTML = `<span class="material-icons-round">check_circle</span> Clase: ${cls.name}`;
            } else {
                clEl.className = "auto-detect-row notfound";
                clEl.innerHTML = `<span class="material-icons-round">warning</span> Clase no registrada`;
            }

            // Bodega (lookup from class entity's warehouseCode)
            const wEl = document.getElementById("detect-warehouse");
            if (cls) {
                const whCode = cls.warehouseCode;
                let wNameRaw = warehouseMap[whCode] ? `${whCode} - ${warehouseMap[whCode]}` : null;
                const wName = wNameRaw ? wNameRaw.replace(/^bodega\s*[:-]?\s*/i, "") : null;
                if (wName) {
                    wEl.className = "auto-detect-row found";
                    wEl.innerHTML = `<span class="material-icons-round">check_circle</span> ${wName}`;
                } else {
                    wEl.className = "auto-detect-row notfound";
                    wEl.innerHTML = `<span class="material-icons-round">warning</span> Bodega no registrada`;
                }
            } else {
                wEl.className = "auto-detect-row notfound";
                wEl.innerHTML = `<span class="material-icons-round">warning</span> Bodega no identificable`;
            }

            // Categoría
            const catKey = `${cCode}-${caCode}`;
            const catName = categoryMap[catKey];
            const catEl = document.getElementById("detect-category");
            if (catName) {
                catEl.className = "auto-detect-row found";
                catEl.innerHTML = `<span class="material-icons-round">check_circle</span> Categoría: ${catName}`;
            } else {
                catEl.className = "auto-detect-row notfound";
                catEl.innerHTML = `<span class="material-icons-round">warning</span> Categoría no registrada`;
            }
        } else {
            autoCard.style.display = "none";
        }
    });

    document.getElementById("item-form").addEventListener("submit", async (e) => {
        e.preventDefault();
        
        const docId = document.getElementById("form-doc-id").value;
        const name = document.getElementById("f-name").value.trim();
        const codeDigits = document.getElementById("f-code").value.replace(/\D/g, "");
        const location = document.getElementById("f-location").value.trim();
        const unit = document.getElementById("f-unit").value;
        const partNumber = document.getElementById("f-part-number").value.trim() || null;
        const machine = document.getElementById("f-machine").value || null;
        const areaDept = document.getElementById("f-area-dept").value.trim() || null;
        const subCategory = document.getElementById("f-subcategory").value.trim() || null;
        const extraInfo = document.getElementById("f-extra-info").value.trim() || null;

        if (codeDigits.length < 7) {
            showToast("El código debe tener al menos 7 dígitos (Clase y Categoría).", "error");
            return;
        }

        const warehouse = codeDigits.substring(0, 3); // stored as Clase in 'warehouse'
        const category = codeDigits.substring(3, 6);   // stored as Categoría in 'category'
        const codeSuffix = codeDigits.substring(6);

        // Format code layout
        const formattedCode = `${codeDigits.substring(0, 3)}-${codeDigits.substring(3, 6)}-${codeDigits.substring(6)}`;

        let finalPhotoUrl = state.currentEditPhotoUrl || null;

        const btnSave = document.getElementById("btn-save-item");
        const originalHtml = btnSave.innerHTML;

        if (state.selectedFile) {
            try {
                btnSave.disabled = true;
                btnSave.innerHTML = `<div class="spinner" style="width:16px;height:16px;border-width:2px;margin-right:8px;display:inline-block;vertical-align:middle;"></div> Procesando foto...`;
                
                finalPhotoUrl = await new Promise((resolve, reject) => {
                    const reader = new FileReader();
                    reader.onload = (e) => {
                        const img = new Image();
                        img.onload = () => {
                            const canvas = document.createElement("canvas");
                            const MAX_WIDTH = 600;
                            const MAX_HEIGHT = 600;
                            let width = img.width;
                            let height = img.height;
                            
                            if (width > height) {
                                if (width > MAX_WIDTH) { height *= MAX_WIDTH / width; width = MAX_WIDTH; }
                            } else {
                                if (height > MAX_HEIGHT) { width *= MAX_HEIGHT / height; height = MAX_HEIGHT; }
                            }
                            canvas.width = width;
                            canvas.height = height;
                            const ctx = canvas.getContext("2d");
                            ctx.drawImage(img, 0, 0, width, height);
                            resolve(canvas.toDataURL("image/jpeg", 0.6));
                        };
                        img.onerror = reject;
                        img.src = e.target.result;
                    };
                    reader.onerror = reject;
                    reader.readAsDataURL(state.selectedFile);
                });
            } catch (err) {
                console.error("Error procesando foto:", err);
                showToast("Error procesando foto: " + err.message, "error");
                btnSave.disabled = false;
                btnSave.innerHTML = originalHtml;
                return;
            }
        }

        const itemData = {
            name,
            code: formattedCode,
            warehouse,
            category,
            codeSuffix,
            location,
            unit,
            partNumber,
            machine,
            areaDept,
            subCategory,
            extraInfo,
            photoPath: finalPhotoUrl, // Stored as 'photoPath' to match Android ItemEntity
            updatedAt: Date.now()
        };

        try {
            if (docId) {
                await db.collection("items").doc(docId).set(itemData, { merge: true });
                showToast("Modificación guardada exitosamente en la nube.");
            } else {
                await db.collection("items").add(itemData);
                showToast("Nuevo artículo creado exitosamente en la nube.");
            }
            btnSave.disabled = false;
            btnSave.innerHTML = originalHtml;
            document.getElementById("nav-inventory").click();
        } catch (err) {
            console.error("Error saving item:", err);
            showToast("Error al guardar el artículo: " + err.message, "error");
            btnSave.disabled = false;
            btnSave.innerHTML = originalHtml;
        }
    });
}

function resetForm() {
    document.getElementById("form-page-title").innerText = "Nuevo Artículo";
    document.getElementById("form-doc-id").value = "";
    document.getElementById("item-form").reset();
    document.getElementById("code-preview").innerText = "";
    document.getElementById("auto-detect-card").style.display = "none";
    
    // Clear photo upload card
    state.selectedFile = null;
    state.currentEditPhotoUrl = null;
    document.getElementById("f-photo-file").value = "";
    document.getElementById("web-preview-img").src = "";
    document.getElementById("web-image-preview").style.display = "none";
    document.getElementById("web-image-placeholder").style.display = "flex";
}

// ═══════════════ MODAL & DETAIL VIEW ═══════════════
function setupModal() {
    const overlay = document.getElementById("modal-overlay");
    const closeBtn = document.getElementById("modal-close-btn");

    const closeModal = () => { overlay.style.display = "none"; };

    closeBtn.addEventListener("click", closeModal);
    overlay.addEventListener("click", (e) => {
        if (e.target === overlay) closeModal();
    });

    // Edit item from modal
    document.getElementById("modal-edit-btn").addEventListener("click", () => {
        if (!state.selectedItem) return;
        closeModal();
        
        // Go to page add/edit
        document.getElementById("nav-add").click();
        document.getElementById("form-page-title").innerText = "Modificar Artículo";
        
        const item = state.selectedItem;
        document.getElementById("form-doc-id").value = item.id;
        document.getElementById("f-name").value = item.name;
        document.getElementById("f-code").value = item.code.replace(/\D/g, "");
        document.getElementById("f-location").value = item.location;
        document.getElementById("f-unit").value = item.unit || "";
        document.getElementById("f-part-number").value = item.partNumber || "";
        document.getElementById("f-machine").value = item.machine || "";
        document.getElementById("f-area-dept").value = item.areaDept || "";
        document.getElementById("f-subcategory").value = item.subCategory || "";
        document.getElementById("f-extra-info").value = item.extraInfo || "";
        
        // Setup image preview if editing
        state.selectedFile = null;
        state.currentEditPhotoUrl = item.photoPath || null;
        if (item.photoPath) {
            document.getElementById("web-preview-img").src = item.photoPath;
            document.getElementById("web-image-placeholder").style.display = "none";
            document.getElementById("web-image-preview").style.display = "block";
        } else {
            document.getElementById("web-preview-img").src = "";
            document.getElementById("web-image-preview").style.display = "none";
            document.getElementById("web-image-placeholder").style.display = "flex";
        }

        // Trigger input event to build detect preview
        document.getElementById("f-code").dispatchEvent(new Event("input"));
    });

    // Delete item from modal
    document.getElementById("modal-delete-btn").addEventListener("click", async () => {
        if (!state.selectedItem) return;
        const item = state.selectedItem;
        if (confirm(`¿Estás seguro de que deseas eliminar permanentemente a "${item.name}"?`)) {
            try {
                await db.collection("items").doc(item.id).delete();
                closeModal();
            } catch (err) {
                console.error("Error deleting item:", err);
                showToast("Error al eliminar el artículo: " + err.message, "error");
            }
        }
    });
}

function openDetailModal(item) {
    state.selectedItem = item;
    const overlay = document.getElementById("modal-overlay");
    const hero = document.getElementById("modal-hero");
    
    // Set image hero
    if (item.photoPath) {
        hero.innerHTML = `<img src="${item.photoPath}" alt="${item.name}">`;
    } else {
        hero.innerHTML = `<span class="material-icons-round">inventory_2</span>`;
    }

    document.getElementById("modal-name").innerText = item.name;

    const details = document.getElementById("modal-details");
    details.innerHTML = "";

    const cls = classMap[item.warehouse];
    const wCode = cls ? cls.warehouseCode : "?";
    let wNameRaw = warehouseMap[wCode] ? `${wCode} - ${warehouseMap[wCode]}` : "Desconocida";
    const wName = wNameRaw.replace(/^bodega\s*[:-]?\s*/i, "");
    const className = cls ? cls.name : item.warehouse;
    const catName = categoryMap[`${item.warehouse}-${item.category}`] || item.category;
    const machName = machineMap[item.machine] || item.machine;
    const areaName = areaDeptMap[item.areaDept] || item.areaDept;

    const addDetail = (icon, label, value) => {
        if (value === undefined || value === null || value === "") return;
        const div = document.createElement("div");
        div.className = "modal-detail";
        div.innerHTML = `
            <span class="material-icons-round">${icon}</span>
            <div class="modal-detail-text">
                <span class="modal-detail-label">${label}</span>
                <span class="modal-detail-value">${value}</span>
            </div>
        `;
        details.appendChild(div);
    };

    addDetail("qr_code", "Código Completo", item.code);
    addDetail("warehouse", "Bodega", wName);
    addDetail("layers", "Clase", className);
    addDetail("category", "Categoría", catName);
    addDetail("place", "Ubicación Física", item.location);
    addDetail("scale", "Unidad de Medida", item.unit);
    addDetail("build", "N° de Parte", item.partNumber);
    addDetail("precision_manufacturing", "Máquina", machName);
    addDetail("business", "Área/Depto", areaName);
    addDetail("folder", "Sub-Categoría", item.subCategory);
    addDetail("notes", "Información Adicional", item.extraInfo);

    overlay.style.display = "flex";
}

/* ════════ REOJ Y AÑO DINÁMICO ════════ */
function updateLiveTime() {
    const timeDisplay = document.getElementById('live-time-display');
    const footerYear = document.getElementById('footer-year');
    
    if (timeDisplay || footerYear) {
        // Dominican Republic uses UTC-4 (America/Santo_Domingo)
        const options = { 
            timeZone: 'America/Santo_Domingo',
            year: 'numeric', month: '2-digit', day: '2-digit',
            hour: '2-digit', minute: '2-digit', second: '2-digit',
            hour12: true
        };
        const dateStr = new Intl.DateTimeFormat('es-DO', options).format(new Date());
        
        if (timeDisplay) {
            timeDisplay.textContent = dateStr;
        }
        if (footerYear) {
            const yearStr = new Intl.DateTimeFormat('es-DO', { timeZone: 'America/Santo_Domingo', year: 'numeric' }).format(new Date());
            footerYear.textContent = yearStr;
        }
    }
}

// Iniciar reloj
setInterval(updateLiveTime, 1000);
updateLiveTime();
