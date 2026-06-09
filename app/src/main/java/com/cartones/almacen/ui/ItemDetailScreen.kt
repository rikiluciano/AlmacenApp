package com.cartones.almacen.ui

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.cartones.almacen.data.ItemEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(navController: NavController, viewModel: ItemViewModel, itemId: String) {
    val item by viewModel.getByIdFlow(itemId).collectAsState(initial = null)
    val warehousesMap by viewModel.warehousesMap.collectAsState()
    val classesMap by viewModel.classesMap.collectAsState()
    val categoriesMap by viewModel.categoriesMap.collectAsState()
    val machinesMap by viewModel.machinesMap.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, null, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Detalles del Artículo", fontWeight = FontWeight.Bold)
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
        val currentItem = item
        if (currentItem == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(12.dp))
                    Text("Cargando...", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
            return@Scaffold
        }

        val parts = currentItem.code.split("-")
        val classCode = if (parts.isNotEmpty()) parts[0] else currentItem.warehouse
        val categoryCode = if (parts.size >= 2) parts[1] else currentItem.category
        
        val classEntity = classesMap[classCode]
        val wCode = classEntity?.warehouseCode ?: "?"
        val wName = classEntity?.let { warehousesMap[it.warehouseCode] } ?: "Desconocida"
        val className = classEntity?.name ?: classCode
        val categoryEntity = categoriesMap["$classCode-$categoryCode"]
        val categoryName = categoryEntity?.name ?: categoryCode
        val machineName = currentItem.machine?.let { machinesMap[it] } ?: ""

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ═══════ IMAGEN GRANDE ═══════
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (!currentItem.photoPath.isNullOrEmpty()) {
                    val imageModel = if (currentItem.photoPath.startsWith("data:image")) {
                        try {
                            val base64String = currentItem.photoPath.substringAfter("base64,")
                            android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
                        } catch (e: Exception) {
                            currentItem.photoPath
                        }
                    } else {
                        currentItem.photoPath
                    }

                    Image(
                        painter = rememberAsyncImagePainter(imageModel),
                        contentDescription = "Foto del artículo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ImageNotSupported,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Sin fotografía",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // ═══════ TÍTULO ═══════
                Text(
                    text = currentItem.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                // ═══════ IDENTIFICACIÓN ═══════
                DetailSectionTitle(icon = Icons.Default.Fingerprint, title = "IDENTIFICACIÓN")
                Spacer(Modifier.height(8.dp))
                DetailInfoCard {
                    DetailRow(label = "CÓDIGO COMPLETO", value = currentItem.code)
                    Divider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    DetailRow(label = "CÓDIGO ARTÍCULO", value = currentItem.codeSuffix)
                    if (!currentItem.partNumber.isNullOrBlank()) {
                        Divider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        DetailRow(label = "N° DE PARTE", value = currentItem.partNumber)
                    }
                    if (!currentItem.unit.isNullOrBlank()) {
                        Divider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        DetailRow(label = "UNIDAD MEDIDA", value = currentItem.unit)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ═══════ CLASIFICACIÓN ═══════
                DetailSectionTitle(icon = Icons.Default.Category, title = "CLASIFICACIÓN")
                Spacer(Modifier.height(8.dp))
                DetailInfoCard {
                    DetailRow(label = "BODEGA", value = "$wCode - $wName")
                    Divider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    DetailRow(label = "CLASE", value = "$classCode - $className")
                    Divider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    DetailRow(label = "CATEGORÍA", value = "$categoryCode - $categoryName")
                    if (!currentItem.subCategory.isNullOrBlank()) {
                        Divider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        DetailRow(label = "SUB-CATEGORÍA", value = currentItem.subCategory)
                    }
                    if (!currentItem.machine.isNullOrBlank()) {
                        Divider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        DetailRow(label = "MÁQUINA", value = "${currentItem.machine}${if (machineName.isNotBlank()) " - $machineName" else ""}")
                    }
                    if (!currentItem.areaDept.isNullOrBlank()) {
                        Divider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        DetailRow(label = "ÁREA/DEPTO", value = currentItem.areaDept)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ═══════ UBICACIÓN ═══════
                DetailSectionTitle(icon = Icons.Default.Place, title = "UBICACIÓN FÍSICA")
                Spacer(Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Place, null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(
                                "Ubicación",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                            )
                            Text(
                                currentItem.location,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ═══════ INFORMACIÓN ADICIONAL ═══════
                DetailSectionTitle(icon = Icons.Default.Notes, title = "INFORMACIÓN ADICIONAL")
                Spacer(Modifier.height(8.dp))
                if (currentItem.extraInfo.isNullOrBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            "No hay información adicional registrada.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            currentItem.extraInfo,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // ═══════ BOTONES DE ACCIÓN ═══════
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { navController.navigate("edit/${currentItem.id}") },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Modificar", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Eliminar", fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text("Eliminar Artículo", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("¿Estás seguro de que deseas eliminar este artículo permanentemente? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        item?.let { viewModel.delete(it) }
                        showDeleteDialog = false
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("ELIMINAR", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun DetailSectionTitle(icon: ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon, null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun DetailInfoCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.ExtraBold
        )
    }
}
