package com.cartones.almacen.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.cartones.almacen.data.ItemEntity
import com.cartones.almacen.util.CodeParser
import com.cartones.almacen.util.CodeVisualTransformation
import com.cartones.almacen.util.StorageUtil
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditItemScreen(navController: NavController, viewModel: ItemViewModel, itemId: String?, triggerScan: Boolean = false) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var fullCode by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var partNumber by remember { mutableStateOf("") }
    var machine by remember { mutableStateOf("") }
    var areaDept by remember { mutableStateOf("") }
    var subCategory by remember { mutableStateOf("") }
    var extraInfo by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var photoUrl by remember { mutableStateOf<String?>(null) } // URL de Firebase Storage ya guardada
    var isSaving by remember { mutableStateOf(false) }

    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Auto-detección de clase, bodega y categoría
    val warehousesMap by viewModel.warehousesMap.collectAsState()
    val classesMap by viewModel.classesMap.collectAsState()
    val categoriesMap by viewModel.categoriesMap.collectAsState()
    val units by viewModel.allUnits.collectAsState()
    val machinesList by viewModel.allMachines.collectAsState()
    val areasDeptsList by viewModel.allAreasDepts.collectAsState()

    val detectedClassCode = remember(fullCode) { if (fullCode.length >= 3) fullCode.take(3) else null }
    val detectedCategoryCode = remember(fullCode) { if (fullCode.length >= 6) fullCode.drop(3).take(3) else null }

    val classEntity = detectedClassCode?.let { classesMap[it] }
    val warehouseName = classEntity?.let { warehousesMap[it.warehouseCode] }
    val categoryEntity = if (detectedClassCode != null && detectedCategoryCode != null) {
        categoriesMap["$detectedClassCode-$detectedCategoryCode"]
    } else null
    val categoryName = categoryEntity?.name

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) { photoUri = tempCameraUri; photoUrl = null }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) { photoUri = uri; photoUrl = null }
    }

    val scannerOptions = GmsBarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
        .build()
    val scanner = GmsBarcodeScanning.getClient(context, scannerOptions)

    // Cargar datos del ítem existente
    LaunchedEffect(itemId) {
        if (itemId != null) {
            val item = viewModel.getById(itemId)
            if (item != null) {
                name = item.name
                fullCode = item.code.replace("-", "")
                location = item.location
                partNumber = item.partNumber ?: ""
                machine = item.machine ?: ""
                areaDept = item.areaDept ?: ""
                subCategory = item.subCategory ?: ""
                extraInfo = item.extraInfo ?: ""
                unit = item.unit ?: ""
                photoUrl = item.photoPath
                photoUri = item.photoPath?.let { Uri.parse(it) }
            }
        }
    }

    // Lanzar escáner automáticamente si triggerScan es true y no estamos editando
    LaunchedEffect(triggerScan) {
        if (triggerScan && itemId == null) {
            try {
                scanner.startScan()
                    .addOnSuccessListener { barcode ->
                        barcode.rawValue?.let { scannedCode ->
                            fullCode = scannedCode.filter { it.isDigit() }.take(15)
                        }
                    }
                    .addOnFailureListener { e ->
                        val msg = e.message.toString()
                        if (!msg.contains("canceled", ignoreCase = true)) {
                            if (msg.contains("Failed to scan code", ignoreCase = true)) {
                                Toast.makeText(context, "Configurando escáner... Intenta de nuevo.", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Error al escanear: $msg", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
            } catch (e: Exception) {
                Toast.makeText(context, "Error iniciando escáner: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (itemId == null) Icons.Default.AddCircle else Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            if (itemId == null) "Registrar Artículo" else "Editar Artículo",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ═══════ SECCIÓN: DATOS OBLIGATORIOS ═══════
            SectionHeader(
                icon = Icons.Default.Star,
                title = "DATOS OBLIGATORIOS",
                color = MaterialTheme.colorScheme.error
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre del Artículo *") },
                leadingIcon = { Icon(Icons.Default.Label, null, tint = MaterialTheme.colorScheme.primary) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Código con auto-formateo y QR
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = fullCode,
                    onValueChange = { newValue ->
                        fullCode = newValue.filter { it.isDigit() }.take(15)
                    },
                    label = { Text("Código (001-002-003...) *") },
                    leadingIcon = { Icon(Icons.Default.Fingerprint, null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = CodeVisualTransformation(),
                    isError = fullCode.isNotBlank() && !CodeParser.isValid(CodeParser.autoFormat(fullCode)),
                    supportingText = {
                        if (fullCode.isNotBlank() && !CodeParser.isValid(CodeParser.autoFormat(fullCode))) {
                            val digits = fullCode.length
                            if (digits < 7) Text("Ingresa al menos 7 dígitos (${digits}/7+)")
                            else Text("Formato: 001-019-123456789")
                        }
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilledIconButton(
                    onClick = {
                        try {
                            scanner.startScan()
                                .addOnSuccessListener { barcode ->
                                    barcode.rawValue?.let { scannedCode ->
                                        fullCode = scannedCode.filter { it.isDigit() }.take(15)
                                    }
                                }
                                .addOnFailureListener { e ->
                                    val msg = e.message.toString()
                                    if (!msg.contains("canceled", ignoreCase = true)) {
                                        if (msg.contains("Failed to scan code", ignoreCase = true)) {
                                            Toast.makeText(context, "Configurando escáner... Intenta de nuevo.", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, "Error al escanear: $msg", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error iniciando escáner: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier.size(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Icon(
                        Icons.Default.QrCodeScanner,
                        contentDescription = "Escanear QR",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Indicadores de detección automática
            if (detectedClassCode != null || detectedCategoryCode != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Detección Automática", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(Modifier.height(6.dp))
                        if (detectedClassCode != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (classEntity != null && warehouseName != null) Icons.Default.CheckCircle else Icons.Default.HelpOutline, null,
                                    tint = if (classEntity != null && warehouseName != null) Color(0xFF2E7D32) else Color(0xFFFF6F00),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("Bodega: ${classEntity?.warehouseCode ?: "?"}${if (warehouseName != null) " → $warehouseName" else " (no registrada)"}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (classEntity != null) Icons.Default.CheckCircle else Icons.Default.HelpOutline, null,
                                    tint = if (classEntity != null) Color(0xFF2E7D32) else Color(0xFFFF6F00),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("Clase: $detectedClassCode${if (classEntity != null) " → ${classEntity.name}" else " (no registrada)"}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        if (detectedCategoryCode != null) {
                            Spacer(Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (categoryName != null) Icons.Default.CheckCircle else Icons.Default.HelpOutline, null,
                                    tint = if (categoryName != null) Color(0xFF2E7D32) else Color(0xFFFF6F00),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("Categoría: $detectedCategoryCode${if (categoryName != null) " → $categoryName" else " (no registrada)"}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Ubicación Física * (Ej: 1-A-5)") },
                leadingIcon = { Icon(Icons.Default.Place, null, tint = MaterialTheme.colorScheme.primary) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                supportingText = { Text("Tramo-Lado-Nivel (Ej: 1-A-5)") }
            )

            Divider(modifier = Modifier.padding(vertical = 4.dp))

            // ═══════ SECCIÓN: DATOS OPCIONALES ═══════
            SectionHeader(icon = Icons.Default.Tune, title = "DATOS OPCIONALES", color = MaterialTheme.colorScheme.secondary)

            var unitExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = unitExpanded,
                onExpandedChange = { unitExpanded = !unitExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("Unidad de Medida (Opcional)") },
                    leadingIcon = { Icon(Icons.Default.Scale, null, tint = MaterialTheme.colorScheme.secondary) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                if (units.isNotEmpty()) {
                    ExposedDropdownMenu(expanded = unitExpanded, onDismissRequest = { unitExpanded = false }) {
                        units.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption.name) },
                                onClick = { unit = selectionOption.name; unitExpanded = false }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = partNumber,
                onValueChange = { partNumber = it },
                label = { Text("Número de Parte") },
                leadingIcon = { Icon(Icons.Default.Tag, null, tint = MaterialTheme.colorScheme.secondary) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            var machineExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = machineExpanded,
                onExpandedChange = { machineExpanded = !machineExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = machine,
                    onValueChange = { machine = it },
                    label = { Text("Máquina (Opcional)") },
                    leadingIcon = { Icon(Icons.Default.PrecisionManufacturing, null, tint = MaterialTheme.colorScheme.secondary) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = machineExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                if (machinesList.isNotEmpty()) {
                    ExposedDropdownMenu(expanded = machineExpanded, onDismissRequest = { machineExpanded = false }) {
                        machinesList.forEach { m ->
                            DropdownMenuItem(
                                text = { Text("${m.code} - ${m.name}") },
                                onClick = { machine = "${m.code} - ${m.name}"; machineExpanded = false }
                            )
                        }
                    }
                }
            }

            var areaDeptExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = areaDeptExpanded,
                onExpandedChange = { areaDeptExpanded = !areaDeptExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = areaDept,
                    onValueChange = { areaDept = it },
                    label = { Text("Área / Departamento (Opcional)") },
                    leadingIcon = { Icon(Icons.Default.Business, null, tint = MaterialTheme.colorScheme.secondary) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = areaDeptExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                if (areasDeptsList.isNotEmpty()) {
                    ExposedDropdownMenu(expanded = areaDeptExpanded, onDismissRequest = { areaDeptExpanded = false }) {
                        areasDeptsList.forEach { a ->
                            DropdownMenuItem(
                                text = { Text("${a.code} - ${a.name}") },
                                onClick = { areaDept = "${a.code} - ${a.name}"; areaDeptExpanded = false }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = subCategory,
                onValueChange = { subCategory = it },
                label = { Text("Sub-Categoría (Ej: Plásticos, Tornillos Allen)") },
                leadingIcon = { Icon(Icons.Default.AccountTree, null, tint = MaterialTheme.colorScheme.secondary) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = extraInfo,
                onValueChange = { extraInfo = it },
                label = { Text("Información Adicional") },
                leadingIcon = { Icon(Icons.Default.Notes, null, tint = MaterialTheme.colorScheme.secondary) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(12.dp)
            )

            Divider(modifier = Modifier.padding(vertical = 4.dp))

            // ═══════ SECCIÓN: FOTOGRAFÍA ═══════
            SectionHeader(icon = Icons.Default.CameraAlt, title = "FOTOGRAFÍA (OPCIONAL)", color = MaterialTheme.colorScheme.tertiary)

            val displayPhotoUri = photoUri ?: photoUrl?.let { Uri.parse(it) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(
                        width = 2.dp,
                        color = if (displayPhotoUri != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { galleryLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (displayPhotoUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(displayPhotoUri),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.error)
                            .clickable { photoUri = null; photoUrl = null },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                        Spacer(Modifier.height(8.dp))
                        Text("Tocar para seleccionar de galería", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            OutlinedButton(
                onClick = {
                    try {
                        val file = StorageUtil.createImageFile(context)
                        val uri = StorageUtil.getUriForFile(context, file)
                        tempCameraUri = uri
                        cameraLauncher.launch(uri)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error abriendo cámara: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tomar Foto con Cámara", fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ═══════ BOTÓN GUARDAR ═══════
            Button(
                onClick = {
                    if (name.isBlank() || fullCode.isBlank() || location.isBlank()) {
                        Toast.makeText(context, "Complete los campos obligatorios (*)", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val formattedCode = CodeParser.autoFormat(fullCode)
                    if (!CodeParser.isValid(formattedCode)) {
                        Toast.makeText(context, "Código inválido. Formato: 001-019-123456", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val parts = CodeParser.split(formattedCode)
                    isSaving = true

                    scope.launch {
                        var finalPhotoUrl = photoUrl // URL existente (si editamos y no cambiamos foto)

                        // Si hay una nueva imagen local seleccionada, subirla a Firebase Storage
                        if (photoUri != null && photoUri.toString() != photoUrl) {
                            try {
                                val stream = context.contentResolver.openInputStream(photoUri!!)
                                val originalBitmap = android.graphics.BitmapFactory.decodeStream(stream)
                                stream?.close()
                                
                                val MAX_SIZE = 600
                                val scale = kotlin.math.min(MAX_SIZE.toFloat() / originalBitmap.width, MAX_SIZE.toFloat() / originalBitmap.height)
                                val width = if (scale < 1) (originalBitmap.width * scale).toInt() else originalBitmap.width
                                val height = if (scale < 1) (originalBitmap.height * scale).toInt() else originalBitmap.height
                                
                                val resized = android.graphics.Bitmap.createScaledBitmap(originalBitmap, width, height, true)
                                val out = java.io.ByteArrayOutputStream()
                                resized.compress(android.graphics.Bitmap.CompressFormat.JPEG, 60, out)
                                val base64 = android.util.Base64.encodeToString(out.toByteArray(), android.util.Base64.NO_WRAP)
                                finalPhotoUrl = "data:image/jpeg;base64,$base64"
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error procesando foto: ${e.message}", Toast.LENGTH_LONG).show()
                                isSaving = false
                                return@launch
                            }
                        } else if (photoUri == null) {
                            finalPhotoUrl = null // El usuario eliminó la foto
                        }

                        val item = ItemEntity(
                            id = itemId ?: "",
                            name = name.trim(),
                            code = formattedCode.trim(),
                            codeSuffix = parts.third,
                            warehouse = parts.first,
                            category = parts.second,
                            location = location.trim(),
                            partNumber = partNumber.trim().ifBlank { null },
                            machine = machine.trim().ifBlank { null },
                            areaDept = areaDept.trim().ifBlank { null },
                            subCategory = subCategory.trim().ifBlank { null },
                            photoPath = finalPhotoUrl,
                            extraInfo = extraInfo.trim().ifBlank { null },
                            unit = unit.trim().ifBlank { null },
                            updatedAt = System.currentTimeMillis()
                        )

                        if (itemId == null) {
                            viewModel.insert(item)
                            Toast.makeText(context, "✓ Artículo registrado en la nube", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.update(item)
                            Toast.makeText(context, "✓ Artículo actualizado en la nube", Toast.LENGTH_SHORT).show()
                        }
                        isSaving = false
                        navController.popBackStack()
                    }
                },
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 3.dp)
                    Spacer(Modifier.width(10.dp))
                    Text("GUARDANDO EN LA NUBE...", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(if (itemId == null) "REGISTRAR ARTÍCULO" else "GUARDAR CAMBIOS", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun SectionHeader(icon: ImageVector, title: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}
