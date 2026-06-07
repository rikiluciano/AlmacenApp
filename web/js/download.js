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

document.addEventListener("DOMContentLoaded", async () => {
    const urlParams = new URLSearchParams(window.location.search);
    const token = urlParams.get('t');

    const loadingState = document.getElementById('loading-state');
    const downloadState = document.getElementById('download-state');
    const successState = document.getElementById('success-state');
    const errorState = document.getElementById('error-state');
    const errorMsg = document.getElementById('error-msg');
    const progressBar = document.getElementById('progress-bar');
    const progressText = document.getElementById('progress-text');

    if (!token) {
        loadingState.style.display = 'none';
        errorState.style.display = 'block';
        errorMsg.innerText = "No se proporcionó ningún token de descarga.";
        return;
    }

    try {
        const tokenRef = db.collection('app_downloads').doc(token);
        const doc = await tokenRef.get();

        if (!doc.exists) {
            loadingState.style.display = 'none';
            errorState.style.display = 'block';
            errorMsg.innerText = "Este enlace no existe o es inválido.";
            return;
        }

        const data = doc.data();

        if (data.used) {
            loadingState.style.display = 'none';
            errorState.style.display = 'block';
            errorMsg.innerText = "Este enlace ya fue utilizado por alguien más. Pide un nuevo enlace.";
            return;
        }

        // Marcar como usado inmediatamente (quemar el token)
        await tokenRef.update({ used: true, usedAt: firebase.firestore.FieldValue.serverTimestamp() });

        // Cambiar UI a descargando
        loadingState.style.display = 'none';
        downloadState.style.display = 'block';
        document.getElementById('progress-container').style.display = 'block';

        // Obtener la URL real ofuscada desde version.json
        const response = await fetch('version.json?_t=' + new Date().getTime());
        const versionData = await response.json();
        
        // Bloquear inspección básica descargando el archivo como Blob
        const apkUrl = versionData.downloadUrl;
        
        const apkResponse = await fetch(apkUrl);
        if (!apkResponse.ok) throw new Error("No se pudo descargar el archivo.");

        // Leer como blob con progreso si es posible, de forma simple:
        const contentLength = apkResponse.headers.get('content-length');
        const total = parseInt(contentLength, 10);
        let loaded = 0;

        const reader = apkResponse.body.getReader();
        const chunks = [];

        while(true) {
            const {done, value} = await reader.read();
            if (done) break;
            chunks.push(value);
            loaded += value.length;
            if (total) {
                let percent = Math.round((loaded / total) * 100);
                progressBar.style.width = percent + '%';
                progressText.innerText = percent + '%';
            }
        }

        const blob = new Blob(chunks, {type: 'application/vnd.android.package-archive'});
        const localUrl = URL.createObjectURL(blob);

        // Forzar la descarga en el navegador sin mostrar la URL real
        const a = document.createElement('a');
        a.href = localUrl;
        a.download = `AlmacenApp_v${versionData.versionName}.apk`;
        document.body.appendChild(a);
        a.click();
        
        // Limpieza de memoria
        setTimeout(() => {
            a.remove();
            URL.revokeObjectURL(localUrl);
        }, 1000);

        downloadState.style.display = 'none';
        successState.style.display = 'block';

    } catch (e) {
        console.error(e);
        loadingState.style.display = 'none';
        downloadState.style.display = 'none';
        errorState.style.display = 'block';
        errorMsg.innerText = "Ocurrió un error procesando la descarga: " + e.message;
    }
});
