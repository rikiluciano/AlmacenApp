package com.cartones.almacen.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun NavHostScreen(viewModel: ItemViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(navController = navController, viewModel = viewModel)
        }
        composable("add") {
            AddEditItemScreen(navController = navController, viewModel = viewModel, itemId = null, triggerScan = false)
        }
        composable("add/{scan}") { backStackEntry ->
            val scan = backStackEntry.arguments?.getString("scan")?.toBoolean() ?: false
            AddEditItemScreen(navController = navController, viewModel = viewModel, itemId = null, triggerScan = scan)
        }
        // IDs are now Firestore String doc IDs — no more toLongOrNull()
        composable("edit/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")
            AddEditItemScreen(navController = navController, viewModel = viewModel, itemId = id)
        }
        composable("detail/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")
            if (id != null) {
                ItemDetailScreen(navController = navController, viewModel = viewModel, itemId = id)
            }
        }
        composable("catalogs") {
            ManageCatalogsScreen(navController = navController, viewModel = viewModel)
        }
        composable("search") {
            CatalogSearchScreen(navController = navController, viewModel = viewModel)
        }
    }
}
