package com.cartones.almacen.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.navigation.NavController
import com.cartones.almacen.data.ItemEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: ItemViewModel) {
    val items by viewModel.items.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val warehousesMap by viewModel.warehousesMap.collectAsState()
    val classesMap by viewModel.classesMap.collectAsState()
    val categoriesMap by viewModel.categoriesMap.collectAsState()
    val machinesMap by viewModel.machinesMap.collectAsState()

    val totalItems by viewModel.totalItems.collectAsState()
    val totalWarehouses by viewModel.totalWarehouses.collectAsState()
    val totalClasses by viewModel.totalClasses.collectAsState()
    val totalCategories by viewModel.totalCategories.collectAsState()
    val totalMachines by viewModel.totalMachines.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface
            ) {
                // Drawer header con gradiente industrial
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)
                                )
                            )
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Warehouse, null,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Almacén De Repuestos",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "Cartones del Caribe S.A.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Inicio") },
                    selected = true,
                    onClick = { scope.launch { drawerState.close() } },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.AddCircle, contentDescription = null) },
                    label = { Text("Registrar Artículo") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("add")
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Search, contentDescription = null) },
                    label = { Text("Buscador de Bodega/Categoría/Máquina") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("search")
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Gestionar Catálogos") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("catalogs")
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                
                Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Share, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    label = { Text("Compartir App Android", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
                    selected = false,
                    onClick = {
                        scope.launch { 
                            drawerState.close()
                            com.cartones.almacen.util.ShareAppManager.shareDynamicLink(navController.context)
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                Spacer(Modifier.weight(1f))
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(Modifier.height(8.dp))
                PremiumFooter()
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Warehouse,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú")
                        }
                    },
                    actions = {
                        LiveTimeDisplay()
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
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Barra de búsqueda con estilo industrial
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.search(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        placeholder = {
                            Text(
                                "Buscar nombre, código, N° parte, máquina...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Buscar",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.search("") }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Limpiar",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                AnimatedContent(
                    targetState = searchQuery.length < 3,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "content_switch"
                ) { isDashboard ->
                    if (isDashboard) {
                        // ═══════════════ DASHBOARD DE BIENVENIDA ═══════════════
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Tarjeta de bienvenida
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(20.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                if (searchQuery.isEmpty()) "¡Bienvenido!" else "Búsqueda inteligente",
                                                style = MaterialTheme.typography.headlineSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                if (searchQuery.isEmpty()) "Busca piezas por nombre, código, N° parte o máquina." else "Continúa escribiendo para ver resultados...",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                            )
                                        }
                                        Spacer(Modifier.width(12.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(52.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Inventory2,
                                                null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Acciones rápidas
                            item {
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    "ACCESOS RÁPIDOS",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            item {
                                QuickActionCard(
                                    icon = Icons.Default.AddCircle,
                                    title = "Registrar Nuevo Artículo",
                                    subtitle = "Agrega una pieza o repuesto",
                                    accentColor = MaterialTheme.colorScheme.primary,
                                    onClick = { navController.navigate("add") }
                                )
                            }
                            item {
                                QuickActionCard(
                                    icon = Icons.Default.QrCodeScanner,
                                    title = "Escanear Código QR",
                                    subtitle = "Registra artículo escaneando QR",
                                    accentColor = MaterialTheme.colorScheme.tertiary,
                                    onClick = { navController.navigate("add/true") }
                                )
                            }
                            item {
                                QuickActionCard(
                                    icon = Icons.Default.FindInPage,
                                    title = "Buscador de Bodega/Categoría/Máquina",
                                    subtitle = "Consulta bodega, categoría o máquina",
                                    accentColor = Color(0xFF0277BD),
                                    onClick = { navController.navigate("search") }
                                )
                            }
                            item {
                                QuickActionCard(
                                    icon = Icons.Default.Tune,
                                    title = "Gestionar Catálogos",
                                    subtitle = "Administra bodegas, categorías y máquinas",
                                    accentColor = Color(0xFFE65100),
                                    onClick = { navController.navigate("catalogs") }
                                )
                            }
                            item { PremiumFooter() }
                            item { Spacer(Modifier.height(40.dp)) }
                        }
                    } else {
                        // ═══════════════ RESULTADOS DE BÚSQUEDA ═══════════════
                        Column(modifier = Modifier.fillMaxSize()) {
                            if (items.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            Icons.Default.SearchOff, null,
                                            modifier = Modifier.size(72.dp),
                                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                                        )
                                        Spacer(Modifier.height(16.dp))
                                        Text(
                                            "No se encontraron resultados",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                        Text(
                                            "para \"$searchQuery\"",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                                        )
                                    }
                                }
                            } else {
                                // Contador de resultados
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.primaryContainer)
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            "${items.size} resultado${if (items.size != 1) "s" else ""}",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                                LazyColumn(
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(items) { item ->
                                        val cls = classesMap[item.warehouse]
                                        val wCode = cls?.warehouseCode ?: "?"
                                        val warehouseName = cls?.let { warehousesMap[it.warehouseCode] } ?: "Desconocida"
                                        val className = cls?.name ?: item.warehouse
                                        val catEntity = categoriesMap["${item.warehouse}-${item.category}"]
                                        val categoryName = catEntity?.name ?: item.category
                                        val machineName = item.machine?.let { machinesMap[it] } ?: ""
                                        
                                        ItemCard(
                                            item = item,
                                            warehouseCode = wCode,
                                            warehouseName = warehouseName,
                                            className = className,
                                            categoryName = categoryName,
                                            machineName = machineName,
                                            onClick = { navController.navigate("detail/${item.id}") }
                                        )
                                    }
                                    item { PremiumFooter() }
                                    item { Spacer(modifier = Modifier.height(80.dp)) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ════════════════════════ COMPONENTES ════════════════════════

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    count: Int,
    gradientColors: List<Color>
) {
    Card(
        modifier = modifier.height(140.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = Brush.linearGradient(gradientColors))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Column {
                    Text(
                        count.toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        label,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = accentColor, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                )
            }
            Icon(
                Icons.Default.ChevronRight, null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
            )
        }
    }
}

@Composable
fun ItemCard(
    item: ItemEntity,
    warehouseCode: String,
    warehouseName: String,
    className: String,
    categoryName: String,
    machineName: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp, pressedElevation = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Cabecera superior oscura / acentuada
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.code,
                        style = MaterialTheme.typography.titleMedium.copy(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF60A5FA), Color(0xFFA78BFA), Color(0xFFF472B6))
                            )
                        ),
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    Text(
                        text = item.name.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

            // Cuerpo de detalles
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val combinedWarehouse = "$warehouseCode - $warehouseName"
                val cleanWarehouse = combinedWarehouse.replace(Regex("^(?i)bodega\\s*[:-]?\\s*"), "")
                DetailRowWithIcon(Icons.Default.Warehouse, "Bodega", cleanWarehouse)
                DetailRowWithIcon(Icons.Default.Layers, "Clase", className)
                DetailRowWithIcon(Icons.Default.Category, "Categoría", categoryName)
                
                
                // Ubicación en un bloque destacado
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Place, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("UBICACIÓN FÍSICA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                            Text(item.location, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
                
                if (!item.partNumber.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    DetailRowWithIcon(Icons.Default.Build, "N° DE PARTE", item.partNumber)
                }
                if (machineName.isNotBlank()) {
                    DetailRowWithIcon(Icons.Default.PrecisionManufacturing, "MÁQUINA", machineName)
                }
                if (!item.areaDept.isNullOrBlank()) {
                    DetailRowWithIcon(Icons.Default.Business, "ÁREA/DEPTO", item.areaDept)
                }
                if (!item.unit.isNullOrBlank()) {
                    DetailRowWithIcon(Icons.Default.Scale, "UNIDAD MEDIDA", item.unit)
                }
            }
        }
    }
}

@Composable
fun DetailRowWithIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            modifier = Modifier.size(16.dp).padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = buildAnnotatedString {
                if (label.isNotEmpty()) {
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.Medium)) {
                        append("$label:  ")
                    }
                }
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold)) {
                    append(value)
                }
            },
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun LiveTimeDisplay() {
    var currentTime by remember { androidx.compose.runtime.mutableStateOf(java.time.ZonedDateTime.now(java.time.ZoneId.of("America/Santo_Domingo"))) }
    
    LaunchedEffect(Unit) {
        while(true) {
            kotlinx.coroutines.delay(1000)
            currentTime = java.time.ZonedDateTime.now(java.time.ZoneId.of("America/Santo_Domingo"))
        }
    }
    
    val formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss a")
    
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 8.dp)) {
        Box(modifier = Modifier.size(8.dp).background(Color(0xFF10B981), androidx.compose.foundation.shape.CircleShape))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = currentTime.format(formatter),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
fun PremiumFooter(modifier: Modifier = Modifier) {
    val currentYear = java.time.ZonedDateTime.now(java.time.ZoneId.of("America/Santo_Domingo")).year
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1E29), shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            .padding(horizontal = 24.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "RLabs",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(Color(0xFFEF4444), shape = CircleShape)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SocialIcon(iconRes = com.cartones.almacen.R.drawable.ic_x_logo)
                SocialIcon(iconRes = com.cartones.almacen.R.drawable.ic_github_logo)
                SocialIcon(iconRes = com.cartones.almacen.R.drawable.ic_linkedin_logo)
                SocialIcon(iconRes = com.cartones.almacen.R.drawable.ic_facebook_logo)
                SocialIcon(iconRes = com.cartones.almacen.R.drawable.ic_instagram_logo)
                SocialIcon(iconRes = com.cartones.almacen.R.drawable.ic_whatsapp_logo)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        androidx.compose.material3.Divider(color = Color(0xFF2D3748), thickness = 1.dp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "© $currentYear RLabs. Todos los derechos reservados.",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = Color(0xFF64748B),
            textAlign = androidx.compose.ui.text.style.TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun SocialIcon(iconRes: Int) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .androidx.compose.foundation.border(1.dp, Color(0xFF4A5568), RoundedCornerShape(6.dp))
            .background(Color.Transparent, RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = androidx.compose.ui.res.painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = Color(0xFFCBD5E1)
        )
    }
}
