package com.cartones.almacen.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class CodeVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        // Formato: XXX-XXX-XXXXXXXXX
        val digits = text.text.filter { it.isDigit() }.take(15)
        
        val out = StringBuilder()
        for (i in digits.indices) {
            if (i == 3 || i == 6) out.append("-")
            out.append(digits[i])
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 3) return offset
                if (offset <= 6) return offset + 1
                return offset + 2
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 3) return offset
                if (offset <= 7) return offset - 1
                return offset - 2
            }
        }

        return TransformedText(AnnotatedString(out.toString()), offsetMapping)
    }
}
