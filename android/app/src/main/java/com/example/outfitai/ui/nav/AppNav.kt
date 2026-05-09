package com.example.outfitai.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.outfitai.ui.gaps.GapsRoute
import com.example.outfitai.ui.insights.InsightsRoute
import com.example.outfitai.ui.inspiration.InspirationRoute
import com.example.outfitai.ui.itemdetails.ItemDetailsRoute
import com.example.outfitai.ui.outfits.OutfitDetailRoute
import com.example.outfitai.ui.outfits.OutfitStudioRoute
import com.example.outfitai.ui.profile.ProfileRoute
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

    const val Profile = "profile"
    const val Insights = "insights"
    const val Gaps = "gaps"
    const val Inspiration = "inspiration"
}

private fun NavController.navigateTab(route: String) {
    navigate(route) {
        popUpTo(Routes.Wardrobe) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
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
                onStudioClick = { navController.navigateTab(Routes.OutfitStudio) },
                onWardrobeClick = { navController.navigateTab(Routes.Wardrobe) },
                onProfileClick = { navController.navigateTab(Routes.Profile) },
            )
        }

        composable(Routes.OutfitStudio) {
            OutfitStudioRoute(
                onBack = { navController.popBackStack() },
                onWardrobeClick = { navController.navigateTab(Routes.Wardrobe) },
                onTripClick = { navController.navigateTab(Routes.TripPlanner) },
                onProfileClick = { navController.navigateTab(Routes.Profile) },
                onInspirationClick = { navController.navigate(Routes.Inspiration) },
            )
        }

        composable(Routes.Inspiration) {
            InspirationRoute(
                onBack = { navController.popBackStack() },
                onOutfitSaved = { outfitId ->
                    navController.navigate(Routes.outfitDetail(outfitId)) {
                        popUpTo(Routes.Inspiration) { inclusive = true }
                    }
                },
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
                onStudioClick = { navController.navigateTab(Routes.OutfitStudio) },
                onTripClick = { navController.navigateTab(Routes.TripPlanner) },
                onProfileClick = { navController.navigateTab(Routes.Profile) },
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
                },
                onItemMutatedInPlace = {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("wardrobe_refresh", true)
                },
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

        composable(Routes.Profile) {
            ProfileRoute(
                onBack = { navController.popBackStack() },
                onOpenInsights = { navController.navigate(Routes.Insights) },
                onOpenGaps = { navController.navigate(Routes.Gaps) },
                onLogout = onLogout,
                onTripClick = { navController.navigateTab(Routes.TripPlanner) },
                onStudioClick = { navController.navigateTab(Routes.OutfitStudio) },
                onWardrobeClick = { navController.navigateTab(Routes.Wardrobe) },
            )
        }

        composable(Routes.Insights) {
            InsightsRoute(
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.Gaps) {
            GapsRoute(
                onBack = { navController.popBackStack() },
            )
        }
    }
}
