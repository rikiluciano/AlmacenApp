#!/bin/bash
REPO_DIR="/home/ubuntu/AlmacenApp"
cd $REPO_DIR || exit

# Obtener información del servidor remoto
git fetch origin main

# Comparar versión local con la versión remota
LOCAL=$(git rev-parse HEAD)
REMOTE=$(git rev-parse origin/main)

if [ "$LOCAL" != "$REMOTE" ]; then
    echo "$(date): Nuevos cambios detectados en GitHub. Iniciando actualización..."
    git pull origin main
    
    # Instalar dependencias si hubieron cambios
    npm install
    
    # Ejecutar nuestro script de compilación y subida a Firebase
    node scripts/build_and_deploy.js
    
    echo "$(date): Despliegue automatizado completado."
fi
