package com.cartones.almacen.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.cartones.almacen.data.CategoryEntity
import com.cartones.almacen.data.MachineEntity
import com.cartones.almacen.data.WarehouseEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageCatalogsScreen(navController: NavController, viewModel: ItemViewModel) {
    var tabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Bodegas", "Clases", "Categorías", "Máquinas", "Unidades", "Áreas/Deptos")
    val tabIcons = listOf(Icons.Default.Warehouse, Icons.Default.Storefront, Icons.Default.Category, Icons.Default.PrecisionManufacturing, Icons.Default.Scale, Icons.Default.Business)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Settings, null, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Gestionar Catálogos", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Atrás")
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
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            ScrollableTabRow(
                selectedTabIndex = tabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 8.dp,
                indicator = { tabPositions ->
                    if (tabIndex < tabPositions.size) {
                        TabRowDefaults.Indicator(
                            Modifier.tabIndicatorOffset(tabPositions[tabIndex]),
                            height = 3.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = tabIndex == index,
                        onClick = { tabIndex = index },
                        text = { Text(title, fontWeight = if (tabIndex == index) FontWeight.Bold else FontWeight.Normal) },
                        icon = { Icon(tabIcons[index], null, modifier = Modifier.size(20.dp)) }
                    )
                }
            }

            when (tabIndex) {
                0 -> WarehouseTab(viewModel)
                1 -> ClassTab(viewModel)
                2 -> CategoryTab(viewModel)
                3 -> MachineTab(viewModel)
                4 -> UnitTab(viewModel)
                5 -> AreaDeptTab(viewModel)
            }
        }
    }
}

@Composable
fun WarehouseTab(viewModel: ItemViewModel) {
    val warehouses by viewModel.allWarehouses.collectAsState()
    val context = LocalContext.current
    var code by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf<WarehouseEntity?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Formulario de entrada
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Agregar Bodega",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(10.dp))
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Código") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        supportingText = { Text("Ej: Bodega Principal") }
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val trimCode = code.trim()
                            if (trimCode.isNotBlank() && name.isNotBlank()) {
                                viewModel.insertWarehouse(trimCode, name.trim())
                                code = ""
                                name = ""
                                Toast.makeText(context, "✓ Bodega agregada", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Complete código (2 dígitos) y nombre", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Agregar")
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            "${warehouses.size} bodegas registradas",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Spacer(Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(warehouses) { w ->
                CatalogItemRow(
                    code = w.code,
                    name = w.name,
                    icon = Icons.Default.Warehouse,
                    accentColor = Color(0xFF1B5E20),
                    onDelete = { showDeleteDialog = w }
                )
            }
        }
    }

    showDeleteDialog?.let { w ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Eliminar Bodega") },
            text = { Text("¿Eliminar bodega ${w.code} - ${w.name}?") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deleteWarehouse(w); showDeleteDialog = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("ELIMINAR", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("Cancelar") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassTab(viewModel: ItemViewModel) {
    val classes by viewModel.allClasses.collectAsState()
    val warehouses by viewModel.allWarehouses.collectAsState()
    val warehousesMap by viewModel.warehousesMap.collectAsState()
    val context = LocalContext.current
    var code by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var selectedWarehouse by remember { mutableStateOf<WarehouseEntity?>(null) }
    var expandedWarehouse by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<com.cartones.almacen.data.ClassEntity?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Agregar Clase",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(10.dp))
                
                ExposedDropdownMenuBox(
                    expanded = expandedWarehouse,
                    onExpandedChange = { expandedWarehouse = !expandedWarehouse }
                ) {
                    OutlinedTextField(
                        value = selectedWarehouse?.let { "${it.code} - ${it.name}" } ?: "Seleccionar Bodega...",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedWarehouse) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedWarehouse,
                        onDismissRequest = { expandedWarehouse = false }
                    ) {
                        warehouses.forEach { w ->
                            DropdownMenuItem(
                                text = { Text("${w.code} - ${w.name}") },
                                onClick = {
                                    selectedWarehouse = w
                                    expandedWarehouse = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(Modifier.height(8.dp))

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Código") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        supportingText = { Text("Ej: Repuestos") }
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val trimCode = code.trim()
                            if (trimCode.isNotBlank() && name.isNotBlank() && selectedWarehouse != null) {
                                viewModel.insertClass(trimCode, name.trim(), selectedWarehouse!!.code)
                                code = ""
                                name = ""
                                Toast.makeText(context, "✓ Clase agregada", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Complete bodega, código y nombre", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Agregar")
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            "${classes.size} clases registradas",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Spacer(Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(classes) { c ->
                val wName = warehousesMap[c.warehouseCode] ?: "Desconocida"
                CatalogItemRow(
                    code = "${c.warehouseCode} → ${c.code}",
                    name = "${c.name} (Bodega: $wName)",
                    icon = Icons.Default.Storefront,
                    accentColor = Color(0xFF0277BD),
                    onDelete = { showDeleteDialog = c }
                )
            }
        }
    }

    showDeleteDialog?.let { c ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Eliminar Clase") },
            text = { Text("¿Eliminar clase ${c.code} - ${c.name}?") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deleteClass(c); showDeleteDialog = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("ELIMINAR", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("Cancelar") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryTab(viewModel: ItemViewModel) {
    val categories by viewModel.allCategories.collectAsState()
    val classes by viewModel.allClasses.collectAsState()
    val classesMap by viewModel.classesMap.collectAsState()
    val context = LocalContext.current
    var code by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var selectedClass by remember { mutableStateOf<com.cartones.almacen.data.ClassEntity?>(null) }
    var expandedClass by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<CategoryEntity?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Agregar Categoría",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(10.dp))
                
                ExposedDropdownMenuBox(
                    expanded = expandedClass,
                    onExpandedChange = { expandedClass = !expandedClass }
                ) {
                    OutlinedTextField(
                        value = selectedClass?.let { "${it.code} - ${it.name}" } ?: "Seleccionar Clase...",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedClass) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedClass,
                        onDismissRequest = { expandedClass = false }
                    ) {
                        classes.forEach { c ->
                            DropdownMenuItem(
                                text = { Text("${c.code} - ${c.name}") },
                                onClick = {
                                    selectedClass = c
                                    expandedClass = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(Modifier.height(8.dp))

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Código") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        supportingText = { Text("Ej: Tornillería") }
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val trimCode = code.trim()
                            if (trimCode.isNotBlank() && name.isNotBlank() && selectedClass != null) {
                                viewModel.insertCategory(trimCode, name.trim(), selectedClass!!.code)
                                code = ""
                                name = ""
                                Toast.makeText(context, "✓ Categoría agregada", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Complete clase, código y nombre", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Agregar")
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            "${categories.size} categorías registradas",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Spacer(Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(categories) { c ->
                val clsName = classesMap[c.classCode]?.name ?: "Desconocida"
                CatalogItemRow(
                    code = "${c.classCode} → ${c.code}",
                    name = "${c.name} (Clase: $clsName)",
                    icon = Icons.Default.Category,
                    accentColor = Color(0xFFE65100),
                    onDelete = { showDeleteDialog = c }
                )
            }
        }
    }

    showDeleteDialog?.let { c ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Eliminar Categoría") },
            text = { Text("¿Eliminar categoría ${c.code} - ${c.name}?") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deleteCategory(c); showDeleteDialog = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("ELIMINAR", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun MachineTab(viewModel: ItemViewModel) {
    val machines by viewModel.allMachines.collectAsState()
    val context = LocalContext.current
    var code by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf<MachineEntity?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Agregar Máquina",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(10.dp))
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Código") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        supportingText = { Text("Ej: 70") }
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        supportingText = { Text("Ej: Saturno 1") }
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (code.isNotBlank() && name.isNotBlank()) {
                                viewModel.insertMachine(code.trim(), name.trim())
                                code = ""
                                name = ""
                                Toast.makeText(context, "✓ Máquina agregada", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Complete código y nombre", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Agregar")
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            "${machines.size} máquinas registradas",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Spacer(Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(machines) { m ->
                CatalogItemRow(
                    code = m.code,
                    name = m.name,
                    icon = Icons.Default.PrecisionManufacturing,
                    accentColor = Color(0xFF6A1B9A),
                    onDelete = { showDeleteDialog = m }
                )
            }
        }
    }

    showDeleteDialog?.let { m ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Eliminar Máquina") },
            text = { Text("¿Eliminar máquina ${m.code} - ${m.name}?") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deleteMachine(m); showDeleteDialog = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("ELIMINAR", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun CatalogItemRow(
    code: String,
    name: String,
    icon: ImageVector,
    accentColor: Color,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = accentColor, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    code,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
                Text(
                    name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete, null,
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun UnitTab(viewModel: ItemViewModel) {
    val units by viewModel.allUnits.collectAsState()
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf<com.cartones.almacen.data.UnitEntity?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Agregar Unidad de Medida",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(10.dp))
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre (Ej: Libra, Unidad)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                viewModel.insertUnit(name.trim())
                                name = ""
                                Toast.makeText(context, "✓ Unidad agregada", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Ingrese el nombre de la unidad", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Agregar")
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            "${units.size} unidades registradas",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Spacer(Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(units) { u ->
                CatalogItemRow(
                    code = "UNIDAD",
                    name = u.name,
                    icon = Icons.Default.Scale,
                    accentColor = Color(0xFF00838F),
                    onDelete = { showDeleteDialog = u }
                )
            }
        }
    }

    showDeleteDialog?.let { u ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Eliminar Unidad") },
            text = { Text("¿Eliminar unidad ${u.name}?") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deleteUnit(u); showDeleteDialog = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("ELIMINAR", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun AreaDeptTab(viewModel: ItemViewModel) {
    val areas by viewModel.allAreasDepts.collectAsState()
    val context = LocalContext.current
    var code by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf<com.cartones.almacen.data.AreaDeptEntity?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Agregar Área / Departamento",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(10.dp))
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Código") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        supportingText = { Text("Ej: 01") }
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        supportingText = { Text("Ej: Seguridad") }
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (code.isNotBlank() && name.isNotBlank()) {
                                viewModel.insertAreaDept(code.trim(), name.trim())
                                code = ""
                                name = ""
                                Toast.makeText(context, "✓ Área/Departamento agregada", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Complete código y nombre", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Agregar")
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            "${areas.size} áreas/departamentos registradas",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Spacer(Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(areas) { a ->
                CatalogItemRow(
                    code = a.code,
                    name = a.name,
                    icon = Icons.Default.Business,
                    accentColor = Color(0xFF00695C),
                    onDelete = { showDeleteDialog = a }
                )
            }
        }
    }

    showDeleteDialog?.let { a ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Eliminar Área / Depto") },
            text = { Text("¿Eliminar ${a.code} - ${a.name}?") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deleteAreaDept(a); showDeleteDialog = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("ELIMINAR", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("Cancelar") }
            }
        )
    }
}
