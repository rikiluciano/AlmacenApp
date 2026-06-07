const ftp = require("basic-ftp");
const path = require("path");

async function upload() {
    const client = new ftp.Client();
    client.ftp.verbose = true;
    try {
        console.log("Conectando al servidor FTP...");
        await client.access({
            host: "ftpupload.net",
            user: "if0_42105996",
            password: "rikiluciano123",
            secure: false
        });
        
        // ftpupload.net (InfinityFree) suele tener una carpeta htdocs para la web pública.
        // Vamos a verificar si existe "htdocs" e ingresar ahí, o usar la raíz.
        const list = await client.list();
        const hasHtdocs = list.some(item => item.name === "htdocs");
        if (hasHtdocs) {
            await client.cd("htdocs");
            console.log("Directorio cambiado a 'htdocs'.");
        }

        console.log("Subiendo app-release.apk...");
        await client.uploadFrom(
            path.resolve(__dirname, "../app/build/outputs/apk/debug/app-debug.apk"), 
            "app-release.apk"
        );
        
        console.log("Subiendo version.json...");
        await client.uploadFrom(
            path.resolve(__dirname, "../web/version.json"), 
            "version.json"
        );

        console.log("¡Archivos subidos exitosamente!");
    }
    catch(err) {
        console.error("Error durante la subida:", err);
    }
    client.close();
}

upload();
