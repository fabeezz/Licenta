package com.example.outfitai.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.outfitai.ui.itemdetails.ItemDetailsRoute
import com.example.outfitai.ui.wardrobe.WardrobeRoute
import com.example.outfitai.ui.wardrobe.WardrobeViewModel
import com.example.outfitai.ui.outfits.OutfitsRoute
object Routes {
    const val Wardrobe = "wardrobe"
    const val Outfits = "outfits"
    const val ItemDetails = "item/{itemId}"
    fun itemDetails(id: Int) = "item/$id"
}

@Composable
fun AppNav(
    onLogout: () -> Unit,
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.Wardrobe
    ) {
        composable(Routes.Outfits) {
            OutfitsRoute(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.Wardrobe) {
            val vm: WardrobeViewModel = hiltViewModel()

            // Access the savedStateHandle safely
            val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle

            // Use the extension function directly on the handle
            val shouldRefresh by savedStateHandle?.getStateFlow("wardrobe_refresh", false)
                ?.collectAsState()
                ?: androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

            LaunchedEffect(shouldRefresh) {
                if (shouldRefresh) {
                    vm.refresh()
                    savedStateHandle?.set("wardrobe_refresh", false)
                }
            }

            WardrobeRoute(
                onLogout = onLogout,
                onOutfitsClick = { navController.navigate(Routes.Outfits) },
                vm = vm,
                onItemClick = { id -> navController.navigate(Routes.itemDetails(id)) }
            )
        }

        composable(
            route = Routes.ItemDetails,
            arguments = listOf(navArgument("itemId") { type = NavType.IntType })
        ) {
            ItemDetailsRoute(
                onBack = { navController.popBackStack() },
                onItemChanged = {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("wardrobe_refresh", true)
                    navController.popBackStack()
                }
            )
        }
    }
}