package com.cartones.almacen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.cartones.almacen.data.ItemRepository
import com.cartones.almacen.ui.ItemViewModel
import com.cartones.almacen.ui.ItemViewModelFactory
import com.cartones.almacen.ui.NavHostScreen
import com.cartones.almacen.ui.theme.AlmacenTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Check for updates automatically
        UpdateManager(this).checkForUpdates()

        // Firebase Firestore-backed repository (no more local DB)
        val repository = ItemRepository()
        val viewModelFactory = ItemViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, viewModelFactory)[ItemViewModel::class.java]

        setContent {
            AlmacenTheme {
                NavHostScreen(viewModel = viewModel)
            }
        }
    }
}
