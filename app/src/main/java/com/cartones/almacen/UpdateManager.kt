package com.cartones.almacen

import android.app.Activity
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class UpdateManager(private val activity: Activity) {

    private val VERSION_JSON_URL = "https://almacen-inteligente-2f515.web.app/version.json"
    private var downloadId: Long = -1

    private val downloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id == downloadId) {
                installApk(context)
                activity.unregisterReceiver(this)
            }
        }
    }

    fun checkForUpdates() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(VERSION_JSON_URL)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().readText()
                    val json = JSONObject(response)
                    
                    val serverVersionCode = json.getInt("versionCode")
                    val serverVersionName = json.getString("versionName")
                    val downloadUrl = json.getString("downloadUrl")
                    val releaseNotes = json.optString("releaseNotes", "Nueva actualización disponible.")

                    val currentVersionCode = BuildConfig.VERSION_CODE

                    if (serverVersionCode > currentVersionCode) {
                        withContext(Dispatchers.Main) {
                            showUpdateDialog(serverVersionName, releaseNotes, downloadUrl)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("UpdateManager", "Error checking for updates", e)
            }
        }
    }

    private fun showUpdateDialog(versionName: String, releaseNotes: String, downloadUrl: String) {
        AlertDialog.Builder(activity)
            .setTitle("Actualización Disponible")
            .setMessage("Nueva versión: $versionName\n\n$releaseNotes")
            .setPositiveButton("Instalar") { _, _ ->
                checkPermissionsAndDownload(downloadUrl)
            }
            .setNegativeButton("Más tarde", null)
            .setCancelable(false)
            .show()
    }

    private fun checkPermissionsAndDownload(downloadUrl: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!activity.packageManager.canRequestPackageInstalls()) {
                // Inform user and take to settings
                AlertDialog.Builder(activity)
                    .setTitle("Permiso requerido")
                    .setMessage("Para actualizar automáticamente, permite a esta aplicación instalar aplicaciones desconocidas en la siguiente pantalla.")
                    .setPositiveButton("Ir a Ajustes") { _, _ ->
                        val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                            data = Uri.parse("package:${activity.packageName}")
                        }
                        activity.startActivity(intent)
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
                return
            }
        }
        startDownload(downloadUrl)
    }

    private fun startDownload(downloadUrl: String) {
        val request = DownloadManager.Request(Uri.parse(downloadUrl))
        request.setTitle("Descargando Actualización")
        request.setDescription("Almacén Inteligente se está actualizando...")
        
        // Remove old apk if exists
        val destinationFile = File(activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "update.apk")
        if (destinationFile.exists()) {
            destinationFile.delete()
        }

        request.setDestinationInExternalFilesDir(activity, Environment.DIRECTORY_DOWNLOADS, "update.apk")
        request.setMimeType("application/vnd.android.package-archive")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            activity.registerReceiver(
                downloadReceiver, 
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                Context.RECEIVER_EXPORTED
            )
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            activity.registerReceiver(
                downloadReceiver, 
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            )
        }

        val manager = activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadId = manager.enqueue(request)
    }

    private fun installApk(context: Context) {
        try {
            val apkFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "update.apk")
            if (!apkFile.exists()) return

            val apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                apkFile
            )

            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("UpdateManager", "Error installing APK", e)
        }
    }
}
