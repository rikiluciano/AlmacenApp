const fs = require('fs');

const toastFn = `
// ════════ TOAST NOTIFICATIONS ════════
function showToast(message, type = 'success') {
    const container = document.getElementById('toast-container');
    if (!container) return;
    
    const toast = document.createElement('div');
    toast.className = 'toast ' + type;
    
    let icon = 'check_circle';
    if (type === 'error') icon = 'error';
    else if (type === 'info') icon = 'info';
    
    toast.innerHTML = \`
        <span class="material-icons-round toast-icon">\${icon}</span>
        <div class="toast-content">
            <p class="toast-message">\${message}</p>
        </div>
    \`;
    
    container.appendChild(toast);
    
    // Animate in
    setTimeout(() => toast.classList.add('show'), 10);
    
    // Remove after 4 seconds
    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => toast.remove(), 400);
    }, 4000);
}
`;

let code = fs.readFileSync('web/js/app.js', 'utf8');

if (!code.includes('function showToast')) {
    code = code.replace('// ═══════════════ ESTADO GLOBAL ═══════════════', toastFn + '\n// ═══════════════ ESTADO GLOBAL ═══════════════');
}

// Transform all alert(...) to showToast(..., "error" or "success")
code = code.replace(/alert\("Error: " \+ err\.message\)/g, 'showToast("Error: " + err.message, "error")');
code = code.replace(/alert\("El código de bodega debe tener exactamente 2 dígitos\."\)/g, 'showToast("El código de bodega debe tener exactamente 2 dígitos.", "error")');
code = code.replace(/alert\("✅ Bodega guardada en la nube\."\)/g, 'showToast("Bodega guardada en la nube.")');
code = code.replace(/alert\("Error al guardar bodega: " \+ err\.message\)/g, 'showToast("Error al guardar bodega: " + err.message, "error")');

code = code.replace(/alert\("El código de clase debe tener exactamente 3 dígitos\."\)/g, 'showToast("El código de clase debe tener exactamente 3 dígitos.", "error")');
code = code.replace(/alert\("Seleccione una bodega\."\)/g, 'showToast("Seleccione una bodega.", "error")');
code = code.replace(/alert\("Error al guardar clase: " \+ err\.message\)/g, 'showToast("Error al guardar clase: " + err.message, "error")');

code = code.replace(/alert\("El código de categoría debe tener exactamente 3 dígitos\."\)/g, 'showToast("El código de categoría debe tener exactamente 3 dígitos.", "error")');
code = code.replace(/alert\("Seleccione una clase\."\)/g, 'showToast("Seleccione una clase.", "error")');
code = code.replace(/alert\("Error al guardar categoría: " \+ err\.message\)/g, 'showToast("Error al guardar categoría: " + err.message, "error")');

code = code.replace(/alert\("Error al guardar máquina: " \+ err\.message\)/g, 'showToast("Error al guardar máquina: " + err.message, "error")');
code = code.replace(/alert\("Error al guardar unidad: " \+ err\.message\)/g, 'showToast("Error al guardar unidad: " + err.message, "error")');

code = code.replace(/alert\("El código debe tener al menos 7 dígitos \(Clase y Categoría\)\."\)/g, 'showToast("El código debe tener al menos 7 dígitos (Clase y Categoría).", "error")');
code = code.replace(/alert\("Error procesando foto: " \+ err\.message\)/g, 'showToast("Error procesando foto: " + err.message, "error")');

code = code.replace(/alert\("✅ Modificación guardada exitosamente en la nube\."\)/g, 'showToast("Modificación guardada exitosamente en la nube.")');
code = code.replace(/alert\("✅ Nuevo artículo creado exitosamente en la nube\."\)/g, 'showToast("Nuevo artículo creado exitosamente en la nube.")');
code = code.replace(/alert\("Error al guardar el artículo: " \+ err\.message\)/g, 'showToast("Error al guardar el artículo: " + err.message, "error")');

code = code.replace(/alert\("Error al eliminar el artículo: " \+ err\.message\)/g, 'showToast("Error al eliminar el artículo: " + err.message, "error")');

fs.writeFileSync('web/js/app.js', code);
console.log('Done!');
