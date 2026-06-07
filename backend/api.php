<?php
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET, POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Authorization, X-API-KEY");
header("Content-Type: application/json; charset=UTF-8");

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

$apiKey = isset($_SERVER['HTTP_X_API_KEY']) ? $_SERVER['HTTP_X_API_KEY'] : '';
$validApiKey = "ALMACEN_SYNC_KEY_2026";

if ($apiKey !== $validApiKey) {
    http_response_code(401);
    echo json_encode(["error" => "No autorizado"]);
    exit();
}

$host = 'sql310.infinityfree.com';
$user = 'if0_42105996';
$pass = 'rikiluciano123';
$dbname = 'if0_42105996_almacen_db';

$conn = new mysqli($host, $user, $pass, $dbname);
if ($conn->connect_error) {
    http_response_code(500);
    echo json_encode(["error" => "Error de base de datos"]);
    exit();
}

$action = isset($_GET['action']) ? $_GET['action'] : '';
$data = json_decode(file_get_contents("php://input"), true);

if ($action === 'sync_down') {
    $lastSync = isset($_GET['lastSync']) ? intval($_GET['lastSync']) : 0;
    
    $response = [];
    
    // Función helper para traer datos
    function getUpdatedRecords($conn, $table, $lastSync) {
        $stmt = $conn->prepare("SELECT * FROM $table WHERE updatedAt > ?");
        $stmt->bind_param("i", $lastSync);
        $stmt->execute();
        $result = $stmt->get_result();
        $records = [];
        while($row = $result->fetch_assoc()) {
            $records[] = $row;
        }
        return $records;
    }
    
    $response['items'] = getUpdatedRecords($conn, 'items', $lastSync);
    $response['warehouses'] = getUpdatedRecords($conn, 'warehouses', $lastSync);
    $response['classes'] = getUpdatedRecords($conn, 'classes', $lastSync);
    $response['categories'] = getUpdatedRecords($conn, 'categories', $lastSync);
    $response['machines'] = getUpdatedRecords($conn, 'machines', $lastSync);
    $response['units'] = getUpdatedRecords($conn, 'units', $lastSync);
    
    echo json_encode(["success" => true, "data" => $response, "serverTime" => round(microtime(true) * 1000)]);
    
} elseif ($action === 'sync_up') {
    if (!$data) {
        echo json_encode(["error" => "No hay datos"]);
        exit();
    }
    
    $conn->begin_transaction();
    try {
        // Items
        if (isset($data['items']) && is_array($data['items'])) {
            $stmt = $conn->prepare("INSERT INTO items (syncId, name, code, codeSuffix, warehouse, category, location, partNumber, machine, subCategory, photoPath, extraInfo, unit, updatedAt, isDeleted) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name), code=VALUES(code), codeSuffix=VALUES(codeSuffix), warehouse=VALUES(warehouse), category=VALUES(category), location=VALUES(location), partNumber=VALUES(partNumber), machine=VALUES(machine), subCategory=VALUES(subCategory), photoPath=VALUES(photoPath), extraInfo=VALUES(extraInfo), unit=VALUES(unit), updatedAt=VALUES(updatedAt), isDeleted=VALUES(isDeleted)");
            
            foreach ($data['items'] as $item) {
                $stmt->bind_param("sssssssssssssii", 
                    $item['syncId'], $item['name'], $item['code'], $item['codeSuffix'], 
                    $item['warehouse'], $item['category'], $item['location'], $item['partNumber'], 
                    $item['machine'], $item['subCategory'], $item['photoPath'], $item['extraInfo'], 
                    $item['unit'], $item['updatedAt'], $item['isDeleted']);
                $stmt->execute();
            }
        }
        
        // Warehouses
        if (isset($data['warehouses']) && is_array($data['warehouses'])) {
            $stmt = $conn->prepare("INSERT INTO warehouses (code, name, syncId, updatedAt, isDeleted) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name), syncId=VALUES(syncId), updatedAt=VALUES(updatedAt), isDeleted=VALUES(isDeleted)");
            foreach ($data['warehouses'] as $w) {
                $stmt->bind_param("sssii", $w['code'], $w['name'], $w['syncId'], $w['updatedAt'], $w['isDeleted']);
                $stmt->execute();
            }
        }

        // Classes
        if (isset($data['classes']) && is_array($data['classes'])) {
            $stmt = $conn->prepare("INSERT INTO classes (code, warehouseCode, name, syncId, updatedAt, isDeleted) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE warehouseCode=VALUES(warehouseCode), name=VALUES(name), syncId=VALUES(syncId), updatedAt=VALUES(updatedAt), isDeleted=VALUES(isDeleted)");
            foreach ($data['classes'] as $c) {
                $stmt->bind_param("ssssii", $c['code'], $c['warehouseCode'], $c['name'], $c['syncId'], $c['updatedAt'], $c['isDeleted']);
                $stmt->execute();
            }
        }

        // Categories
        if (isset($data['categories']) && is_array($data['categories'])) {
            $stmt = $conn->prepare("INSERT INTO categories (id, classCode, name, syncId, updatedAt, isDeleted) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE classCode=VALUES(classCode), name=VALUES(name), syncId=VALUES(syncId), updatedAt=VALUES(updatedAt), isDeleted=VALUES(isDeleted)");
            foreach ($data['categories'] as $cat) {
                $stmt->bind_param("ssssii", $cat['id'], $cat['classCode'], $cat['name'], $cat['syncId'], $cat['updatedAt'], $cat['isDeleted']);
                $stmt->execute();
            }
        }

        // Machines
        if (isset($data['machines']) && is_array($data['machines'])) {
            $stmt = $conn->prepare("INSERT INTO machines (code, name, syncId, updatedAt, isDeleted) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name), syncId=VALUES(syncId), updatedAt=VALUES(updatedAt), isDeleted=VALUES(isDeleted)");
            foreach ($data['machines'] as $m) {
                $stmt->bind_param("sssii", $m['code'], $m['name'], $m['syncId'], $m['updatedAt'], $m['isDeleted']);
                $stmt->execute();
            }
        }

        // Units
        if (isset($data['units']) && is_array($data['units'])) {
            $stmt = $conn->prepare("INSERT INTO units (name, syncId, updatedAt, isDeleted) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE syncId=VALUES(syncId), updatedAt=VALUES(updatedAt), isDeleted=VALUES(isDeleted)");
            foreach ($data['units'] as $u) {
                $stmt->bind_param("ssii", $u['name'], $u['syncId'], $u['updatedAt'], $u['isDeleted']);
                $stmt->execute();
            }
        }

        $conn->commit();
        echo json_encode(["success" => true, "message" => "Sync complete", "serverTime" => round(microtime(true) * 1000)]);
    } catch (Exception $e) {
        $conn->rollback();
        http_response_code(500);
        echo json_encode(["error" => "Error during sync: " . $e->getMessage()]);
    }
} else {
    http_response_code(400);
    echo json_encode(["error" => "Invalid action"]);
}

$conn->close();
?>
