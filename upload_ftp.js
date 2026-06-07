const ftp = require("basic-ftp");

async function upload() {
    const client = new ftp.Client();
    client.ftp.verbose = true;
    try {
        await client.access({
            host: "ftp.x10.mx", // or rlabs.x10.mx
            user: "almacen@rlabs.x10.mx",
            password: "contraseña123",
            secure: false
        });
        console.log("Connected!");
        await client.cd("domains/rlabs.x10.mx/public_html"); // try public_html
        await client.uploadFrom("app/build/outputs/apk/debug/app-debug.apk", "app-release.apk");
        console.log("Uploaded successfully!");
    } catch (err) {
        console.error("FTP Error:", err);
    }
    client.close();
}
upload();
