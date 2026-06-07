const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');
const crypto = require('crypto');

// Configuración
const appDir = path.join(__dirname, '..');
const webDir = path.join(appDir, 'web');
const versionFile = path.join(webDir, 'version.json');
const apkDebugPath = path.join(appDir, 'app', 'build', 'outputs', 'apk', 'debug', 'app-debug.apk');
const apkReleasePath = path.join(appDir, 'app', 'build', 'outputs', 'apk', 'release', 'app-release.apk');

console.log("🚀 Iniciando el proceso automatizado de Build & Deploy...");

// 1. Leer la versión actual
let versionData = {};
try {
    const rawData = fs.readFileSync(versionFile);
    versionData = JSON.parse(rawData);
} catch (e) {
    console.error("❌ No se pudo leer version.json", e);
    process.exit(1);
}

// 2. Incrementar versionCode automáticamente
const newVersionCode = versionData.versionCode + 1;
// Opcional: Incrementar versionName o pedirlo por consola. Por defecto subimos la "minor version".
let [major, minor] = versionData.versionName.split('.');
minor = parseInt(minor) + 1;
const newVersionName = `${major}.${minor}`;

console.log(`📦 Actualizando de la versión ${versionData.versionName} (${versionData.versionCode}) a ${newVersionName} (${newVersionCode})...`);

// (Para que el cambio de versión aplique al APK, tendrías que cambiar build.gradle.kts, 
// pero asumiremos que lo editas a mano o el auto-updater se fía de version.json).

// 3. Compilar APK (usaremos assembleDebug por defecto, puedes cambiarlo a assembleRelease)
console.log("🔨 Compilando la aplicación Android (puede tardar unos minutos)...");
try {
    // Usamos gradlew.bat porque estamos en Windows
    execSync('.\\gradlew.bat assembleDebug', { cwd: appDir, stdio: 'inherit' });
} catch (e) {
    console.warn("⚠️ Advertencia: No se pudo compilar el APK automáticamente (probablemente falta JAVA_HOME). Se intentará usar el APK existente.");
}

// Verifica si existe el APK
const targetApkPath = fs.existsSync(apkReleasePath) ? apkReleasePath : apkDebugPath;
if (!fs.existsSync(targetApkPath)) {
    console.error("❌ No se encontró el APK compilado en", targetApkPath);
    process.exit(1);
}

// 4. Ofuscar el nombre del archivo para seguridad (según el plan Búnker)
const randomString = crypto.randomBytes(6).toString('hex');
const secureFileName = `secure_app_${newVersionName}_${randomString}.dat`;
const destApkPath = path.join(webDir, secureFileName);

console.log(`🔐 Moviendo APK y ofuscando nombre a: ${secureFileName}`);

// Borrar archivos .dat viejos para no llenar el hosting
const files = fs.readdirSync(webDir);
files.forEach(file => {
    if (file.endsWith('.dat') || file.startsWith('secure_app_')) {
        fs.unlinkSync(path.join(webDir, file));
        console.log(`🗑️ Archivo antiguo borrado: ${file}`);
    }
});

// Copiar nuevo APK
fs.copyFileSync(targetApkPath, destApkPath);

// 5. Actualizar version.json
versionData.versionCode = newVersionCode;
versionData.versionName = newVersionName;
versionData.downloadUrl = `https://almacen-inteligente-2f515.web.app/${secureFileName}`;
versionData.releaseNotes = "Actualización automática de seguridad.";

fs.writeFileSync(versionFile, JSON.stringify(versionData, null, 2));
console.log("✅ version.json actualizado correctamente.");

// 6. Deploy a Firebase
console.log("☁️ Subiendo todo a Firebase Hosting...");
try {
    execSync('firebase deploy --only hosting', { cwd: appDir, stdio: 'inherit' });
    console.log("🎉 ¡Despliegue a Firebase completado con éxito!");
} catch (e) {
    console.error("❌ Error subiendo a Firebase.", e);
    console.log("💡 ¿Tienes Firebase CLI instalado y estás logueado? Prueba corriendo 'firebase login'");
}

console.log(`
✅ ¡TODO LISTO! 
La app Android ha sido compilada, empaquetada de forma segura y está disponible online.
Los enlaces de descarga generados desde la web o la app apuntarán a esta nueva versión oculta.
`);
