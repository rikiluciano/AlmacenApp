<?php
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST, OPTIONS");
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

$target_dir = __DIR__ . "/uploads/";
if (!file_exists($target_dir)) {
    mkdir($target_dir, 0777, true);
}

if (!isset($_FILES["photo"])) {
    http_response_code(400);
    echo json_encode(["error" => "No se encontró el archivo 'photo'"]);
    exit();
}

$file = $_FILES["photo"];
$filename = basename($file["name"]);
// Generate a unique name
$newFileName = uniqid() . "_" . preg_replace("/[^a-zA-Z0-9.-]/", "_", $filename);
$target_file = $target_dir . $newFileName;

$imageFileType = strtolower(pathinfo($target_file, PATHINFO_EXTENSION));
$valid_extensions = ["jpg", "jpeg", "png", "webp"];

if (!in_array($imageFileType, $valid_extensions)) {
    http_response_code(400);
    echo json_encode(["error" => "Solo se permiten imágenes (jpg, jpeg, png, webp)"]);
    exit();
}

if (move_uploaded_file($file["tmp_name"], $target_file)) {
    $protocol = (!empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off' || $_SERVER['SERVER_PORT'] == 443) ? "https://" : "http://";
    $domain = $_SERVER['HTTP_HOST'];
    $url = $protocol . $domain . "/uploads/" . $newFileName;
    
    echo json_encode(["success" => true, "photoUrl" => $url]);
} else {
    http_response_code(500);
    echo json_encode(["error" => "Error al guardar el archivo en el servidor"]);
}
?>
