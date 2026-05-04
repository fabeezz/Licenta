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
import com.example.outfitai.ui.outfits.OutfitDetailRoute
import com.example.outfitai.ui.outfits.OutfitStudioRoute
import com.example.outfitai.ui.trips.TripPlannerRoute
import com.example.outfitai.ui.wardrobe.WardrobeRoute
import com.example.outfitai.ui.wardrobe.WardrobeViewModel

object Routes {
    const val Wardrobe = "wardrobe"
    const val OutfitStudio = "outfit-studio"
    const val TripPlanner = "trip-planner"
    const val ItemDetails = "item/{itemId}"
    fun itemDetails(id: Int) = "item/$id"

    const val OutfitDetail = "outfit-detail/{outfitId}"
    fun outfitDetail(id: Int) = "outfit-detail/$id"
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
        composable(Routes.TripPlanner) {
            TripPlannerRoute(
                onClose = { navController.popBackStack() },
                onSaved = {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("wardrobe_refresh", true)
                    navController.popBackStack()
                },
            )
        }

        composable(Routes.OutfitStudio) {
            OutfitStudioRoute(
                onBack = { navController.popBackStack() },
                onWardrobeClick = { navController.popBackStack() },
                onTripClick = { navController.navigate(Routes.TripPlanner) },
            )
        }

        composable(Routes.Wardrobe) {
            val vm: WardrobeViewModel = hiltViewModel()

            val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle

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
                onStudioClick = { navController.navigate(Routes.OutfitStudio) },
                onTripClick = { navController.navigate(Routes.TripPlanner) },
                vm = vm,
                onItemClick = { id -> navController.navigate(Routes.itemDetails(id)) },
                onOutfitClick = { id -> navController.navigate(Routes.outfitDetail(id)) }
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

        composable(
            route = Routes.OutfitDetail,
            arguments = listOf(navArgument("outfitId") { type = NavType.IntType })
        ) {
            OutfitDetailRoute(
                onBack = { navController.popBackStack() },
                onDeleted = {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("wardrobe_refresh", true)
                    navController.popBackStack()
                }
            )
        }
    }
}
