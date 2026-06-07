package com.cartones.almacen.util

import android.content.Context
import android.content.Intent
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Date

object ShareAppManager {
    
    suspend fun shareDynamicLink(context: Context) {
        try {
            val db = FirebaseFirestore.getInstance()
            val tokenRef = db.collection("app_downloads").document()
            
            // Registramos el token en la base de datos
            val data = hashMapOf(
                "used" to false,
                "createdAt" to Date()
            )
            tokenRef.set(data).await()
            
            // La URL base es el mismo dominio de Firebase Hosting
            // Podemos usar la URL fija de Firebase Hosting
            val baseUrl = "https://almacen-inteligente-2f515.web.app/"
            val downloadUrl = "${baseUrl}download.html?t=${tokenRef.id}"
            
            // Creamos el intent de compartir
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Descargar Almacén Inteligente App")
                putExtra(Intent.EXTRA_TEXT, "Hola compañero, aquí tienes el enlace seguro y de un solo uso para descargar la última versión de la aplicación del almacén:\n\n$downloadUrl")
            }
            
            context.startActivity(Intent.createChooser(intent, "Compartir App Android"))
            
        } catch (e: Exception) {
            e.printStackTrace()
            // En un entorno de producción, aquí se podría mostrar un Toast de error.
        }
    }
}
