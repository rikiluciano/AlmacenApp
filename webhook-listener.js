const http = require('http');
const { execSync } = require('child_process');
const crypto = require('crypto');
const fs = require('fs');

const PORT = 3000;
const SECRET = 'almacen_super_secret_2026'; // Reemplazar en producción si se desea
const REPO_DIR = '/home/ubuntu/AlmacenApp';

const server = http.createServer((req, res) => {
    if (req.method === 'POST' && req.url === '/webhook') {
        let body = '';
        req.on('data', chunk => {
            body += chunk.toString();
        });

        req.on('end', () => {
            // Verificar firma de GitHub (Opcional pero recomendado)
            const signature = req.headers['x-hub-signature-256'];
            const hmac = crypto.createHmac('sha256', SECRET);
            const digest = 'sha256=' + hmac.update(body).digest('hex');

            // Si tienes el secret configurado en github, descomenta esta validación:
            /*
            if (signature !== digest) {
                res.writeHead(401, { 'Content-Type': 'text/plain' });
                res.end('Firma inválida.');
                return;
            }
            */

            console.log('🔔 Webhook recibido. Iniciando despliegue...');
            res.writeHead(200, { 'Content-Type': 'text/plain' });
            res.end('Deploy iniciado\n');

            // Ejecutar en background
            setTimeout(() => {
                try {
                    console.log('🔄 Actualizando repositorio...');
                    execSync('git pull origin main', { cwd: REPO_DIR, stdio: 'inherit' });

                    console.log('📦 Ejecutando script de Build & Deploy...');
                    // El script asume que npm install ya se hizo en el repo, si no, lo hacemos:
                    execSync('npm install', { cwd: REPO_DIR, stdio: 'inherit' });
                    
                    // Ejecutar el script que ya tenemos configurado
                    execSync('node scripts/build_and_deploy.js', { cwd: REPO_DIR, stdio: 'inherit' });

                    console.log('✅ Despliegue completado con éxito.');
                } catch (error) {
                    console.error('❌ Error durante el despliegue:', error);
                }
            }, 1000);
        });
    } else {
        res.writeHead(404, { 'Content-Type': 'text/plain' });
        res.end('No encontrado');
    }
});

server.listen(PORT, () => {
    console.log(`🚀 Servidor Webhook escuchando en http://0.0.0.0:${PORT}/webhook`);
});
