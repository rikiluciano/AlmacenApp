package com.cartones.almacen.data

// Plain data class – no Room annotations needed anymore.
// Firestore uses the field names directly via reflection.
data class ItemEntity(
    val id: String = "",          // Firestore document ID
    val name: String = "",
    val code: String = "",        // Código completo: AAA-BBB-CCCC...
    val codeSuffix: String = "",  // Último grupo numérico
    val warehouse: String = "",   // Bodega (primer bloque)
    val category: String = "",    // Categoría (segundo bloque)
    val location: String = "",    // Ubicación (texto libre, múltiple)
    val partNumber: String? = null,
    val machine: String? = null,
    val areaDept: String? = null,
    val subCategory: String? = null,
    val photoPath: String? = null, // URL de Firebase Storage
    val extraInfo: String? = null,
    val unit: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)
