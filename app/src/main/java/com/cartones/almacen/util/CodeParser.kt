package com.cartones.almacen.util

object CodeParser {
    // Formato: AAA-BBB-CCCC... (3 dígitos - 3 dígitos - 1 a 9 dígitos)
    private val regex = Regex("""^\d{3}-\d{3}-\d{1,9}$""")

    fun isValid(code: String): Boolean = regex.matches(code)

    fun split(code: String): Triple<String, String, String> {
        if (!isValid(code)) throw IllegalArgumentException("Código inválido: $code")
        val parts = code.split("-")
        return Triple(parts[0], parts[1], parts[2])
    }

    fun suffix(code: String): String {
        return if (isValid(code)) split(code).third else code
    }

    /**
     * Auto-formatea el código mientras el usuario escribe.
     * Añade guiones automáticamente después de los primeros 3 y 6 dígitos.
     * Solo permite dígitos y los guiones auto-insertados.
     * Formato resultante: AAA-BBB-CCCCCCCCC
     */
    fun autoFormat(rawInput: String): String {
        // Extraer solo dígitos del input
        val digits = rawInput.filter { it.isDigit() }

        // Limitar a 15 dígitos máximo (3 + 3 + 9)
        val limited = digits.take(15)

        return buildString {
            for (i in limited.indices) {
                // Insertar guion después del 3er y 6to dígito
                if (i == 3 || i == 6) append('-')
                append(limited[i])
            }
        }
    }

    /**
     * Determina cuántos dígitos se han introducido (sin contar guiones).
     */
    fun digitCount(code: String): Int {
        return code.count { it.isDigit() }
    }
}
