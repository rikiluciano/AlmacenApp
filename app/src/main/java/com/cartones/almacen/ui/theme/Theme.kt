package com.cartones.almacen.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ═══════════════ PALETA ALMACÉN INDUSTRIAL PREMIUM ═══════════════
val WarehouseGreen = Color(0xFF1B5E20)
val WarehouseGreenLight = Color(0xFF388E3C)
val WarehouseGreenDark = Color(0xFF0D3B12)
val WarehouseSurface = Color(0xFF0F1A12)
val WarehouseSurfaceLight = Color(0xFFF5F9F5)
val AmberAccent = Color(0xFFFF6F00)
val AmberAccentLight = Color(0xFFFFA000)
val AmberAccentDark = Color(0xFFE65100)

private val DarkColorScheme = darkColorScheme(
    primary = WarehouseGreenLight,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1B3A1F),
    onPrimaryContainer = Color(0xFFA5D6A7),
    secondary = AmberAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF3E2723),
    onSecondaryContainer = Color(0xFFFFCC80),
    tertiary = Color(0xFF4FC3F7),
    onTertiary = Color.White,
    background = Color(0xFF0D1B0F),
    onBackground = Color(0xFFE8F0E8),
    surface = Color(0xFF141E16),
    onSurface = Color(0xFFE8F0E8),
    surfaceVariant = Color(0xFF1E2E21),
    onSurfaceVariant = Color(0xFFB0C4B1),
    error = Color(0xFFEF5350),
    onError = Color.White,
    errorContainer = Color(0xFF3B1414),
    onErrorContainer = Color(0xFFEF9A9A),
    outline = Color(0xFF4A6B4D)
)

private val LightColorScheme = lightColorScheme(
    primary = WarehouseGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC8E6C9),
    onPrimaryContainer = Color(0xFF0D3B12),
    secondary = AmberAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE0B2),
    onSecondaryContainer = Color(0xFF3E2723),
    tertiary = Color(0xFF0277BD),
    onTertiary = Color.White,
    background = Color(0xFFF5F9F5),
    onBackground = Color(0xFF1A1C1A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1C1A),
    surfaceVariant = Color(0xFFEEF3EE),
    onSurfaceVariant = Color(0xFF49544A),
    error = Color(0xFFD32F2F),
    onError = Color.White,
    errorContainer = Color(0xFFFFEBEE),
    onErrorContainer = Color(0xFFB71C1C),
    outline = Color(0xFF73887A)
)

@Composable
fun AlmacenTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
