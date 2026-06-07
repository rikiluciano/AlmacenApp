<?php
header('Content-Type: text/plain');

$host = 'sql310.infinityfree.com';
$user = 'if0_42105996';
$pass = 'rikiluciano123';
$dbname = 'if0_42105996_almacen_db';

// Crear conexión
$conn = new mysqli($host, $user, $pass, $dbname);

// Comprobar conexión
if ($conn->connect_error) {
    die("Error de conexión: " . $conn->connect_error);
}

echo "Conectado a la base de datos '$dbname' exitosamente.\n\n";

$queries = [
    "CREATE TABLE IF NOT EXISTS units (
        name VARCHAR(50) PRIMARY KEY,
        syncId VARCHAR(36) UNIQUE,
        updatedAt BIGINT NOT NULL,
        isDeleted TINYINT(1) DEFAULT 0
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;",

    "CREATE TABLE IF NOT EXISTS warehouses (
        code VARCHAR(50) PRIMARY KEY,
        name VARCHAR(255) NOT NULL,
        syncId VARCHAR(36) UNIQUE,
        updatedAt BIGINT NOT NULL,
        isDeleted TINYINT(1) DEFAULT 0
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;",

    "CREATE TABLE IF NOT EXISTS classes (
        code VARCHAR(50) PRIMARY KEY,
        warehouseCode VARCHAR(50) NOT NULL,
        name VARCHAR(255) NOT NULL,
        syncId VARCHAR(36) UNIQUE,
        updatedAt BIGINT NOT NULL,
        isDeleted TINYINT(1) DEFAULT 0,
        FOREIGN KEY (warehouseCode) REFERENCES warehouses(code) ON UPDATE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;",

    "CREATE TABLE IF NOT EXISTS categories (
        id VARCHAR(100) PRIMARY KEY,
        classCode VARCHAR(50) NOT NULL,
        name VARCHAR(255) NOT NULL,
        syncId VARCHAR(36) UNIQUE,
        updatedAt BIGINT NOT NULL,
        isDeleted TINYINT(1) DEFAULT 0,
        FOREIGN KEY (classCode) REFERENCES classes(code) ON UPDATE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;",

    "CREATE TABLE IF NOT EXISTS machines (
        code VARCHAR(50) PRIMARY KEY,
        name VARCHAR(255) NOT NULL,
        syncId VARCHAR(36) UNIQUE,
        updatedAt BIGINT NOT NULL,
        isDeleted TINYINT(1) DEFAULT 0
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;",

    "CREATE TABLE IF NOT EXISTS items (
        syncId VARCHAR(36) PRIMARY KEY,
        name VARCHAR(255) NOT NULL,
        code VARCHAR(100) NOT NULL,
        codeSuffix VARCHAR(50) NOT NULL,
        warehouse VARCHAR(50) NOT NULL,
        category VARCHAR(50) NOT NULL,
        location VARCHAR(255) NOT NULL,
        partNumber VARCHAR(100),
        machine VARCHAR(50),
        subCategory VARCHAR(100),
        photoPath VARCHAR(500),
        extraInfo TEXT,
        unit VARCHAR(50),
        updatedAt BIGINT NOT NULL,
        isDeleted TINYINT(1) DEFAULT 0
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;"
];

foreach ($queries as $query) {
    if ($conn->query($query) === TRUE) {
        echo "Tabla verificada/creada con éxito.\n";
    } else {
        echo "Error al crear la tabla: " . $conn->error . "\n";
        echo "Consulta: " . $query . "\n\n";
    }
}

$conn->close();
echo "\nConfiguración completada.";
?>
