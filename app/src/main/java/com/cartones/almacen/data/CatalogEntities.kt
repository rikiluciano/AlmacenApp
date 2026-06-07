package com.cartones.almacen.data

// Plain data classes for Firestore – no Room annotations.
// Default constructor values required for Firestore deserialization.

data class WarehouseEntity(
    val code: String = "",  // ej. "01", "04"
    val name: String = ""   // ej. "Bodega Principal"
)

data class ClassEntity(
    val code: String = "",          // ej. "001"
    val warehouseCode: String = "", // ej. "01"
    val name: String = ""           // ej. "Partes y Repuestos"
)

data class CategoryEntity(
    val code: String = "",      // ej. "019"
    val classCode: String = "", // ej. "001"
    val name: String = ""       // ej. "Tornillería"
)

data class MachineEntity(
    val code: String = "", // ej. "70"
    val name: String = ""  // ej. "Saturno 1"
)

data class UnitEntity(
    val name: String = "" // ej. "Unidad", "libra", "pie"
)

data class AreaDeptEntity(
    val code: String = "", // ej. "01"
    val name: String = ""  // ej. "Seguridad"
)
