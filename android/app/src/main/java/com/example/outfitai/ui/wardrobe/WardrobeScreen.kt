package com.example.outfitai.ui.wardrobe

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.outfitai.ui.components.AppBottomBar
import com.example.outfitai.ui.components.BottomBarDest
import com.example.outfitai.ui.components.LoomTopBar
import com.example.outfitai.ui.theme.Spacing
import com.example.outfitai.ui.upload.rememberUploadLauncher

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun WardrobeRoute(
    onItemClick: (Int) -> Unit,
    onOutfitClick: (Int) -> Unit,
    onStudioClick: () -> Unit,
    onTripClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    vm: WardrobeViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()
    val upload = rememberUploadLauncher(onDone = { vm.refresh() })

    WardrobeScreen(
        state                   = state,
        onRefresh               = vm::refresh,
        onItemClick             = onItemClick,
        onOutfitClick           = onOutfitClick,
        onStudioClick           = onStudioClick,
        onTripClick             = onTripClick,
        onProfileClick          = onProfileClick,
        onAddClick              = upload.launch,
        onTabSelect             = vm::setTab,
        onFilterBucket          = vm::setFilterBucket,
        onFilterColor           = vm::setFilterColor,
        onFilterWeather         = vm::setFilterWeather,
        onFilterStyle           = vm::setFilterStyle,
        onClearFilters          = vm::clearFilters,
        onSearchQueryChange     = vm::setSearchQuery,
        onCreateCollection      = vm::createCollection,
        onRenameCollection      = vm::renameCollection,
        onDeleteCollection      = vm::deleteCollection,
        sharedTransitionScope   = sharedTransitionScope,
        animatedVisibilityScope = animatedVisibilityScope,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun WardrobeScreen(
    state: WardrobeUiState,
    onRefresh: () -> Unit,
    onItemClick: (Int) -> Unit,
    onOutfitClick: (Int) -> Unit,
    onStudioClick: () -> Unit,
    onTripClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAddClick: () -> Unit,
    onTabSelect: (WardrobeTab) -> Unit,
    onFilterBucket: (CategoryBucket?) -> Unit,
    onFilterColor: (String?) -> Unit,
    onFilterWeather: (String?) -> Unit,
    onFilterStyle: (String?) -> Unit,
    onClearFilters: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onCreateCollection: (String, List<Int>) -> Unit,
    onRenameCollection: (Int, String) -> Unit,
    onDeleteCollection: (Int) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    var sheetOpen by remember { mutableStateOf(false) }
    var searchActive by remember { mutableStateOf(false) }

    val hasActiveFilters = state.filterColor != null ||
            state.filterWeather != null ||
            state.filterStyle != null

    Scaffold(
        topBar = {
            LoomTopBar(
                title   = "Wardrobe",
                actions = {
                    if (state.selectedTab == WardrobeTab.Clothes) {
                        IconButton(onClick = {
                            searchActive = !searchActive
                            if (!searchActive) onSearchQueryChange("")
                        }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    }
                    IconButton(onClick = onRefresh, enabled = !state.isLoading) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
            )
        },
        bottomBar = {
            Box(modifier = Modifier.padding(bottom = Spacing.xxl)) {
                AppBottomBar(
                    active     = BottomBarDest.WARDROBE,
                    onTrip     = onTripClick,
                    onStudio   = onStudioClick,
                    onAdd      = onAddClick,
                    onWardrobe = {},
                    onProfile  = onProfileClick,
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = pad.calculateTopPadding())
                .padding(horizontal = Spacing.xl),
        ) {
            AnimatedVisibility(
                visible = searchActive && state.selectedTab == WardrobeTab.Clothes,
                enter   = expandVertically(),
                exit    = shrinkVertically(),
            ) {
                Column {
                    Spacer(Modifier.height(Spacing.xs))
                    WardrobeSearchBar(
                        query         = state.searchQuery,
                        onQueryChange = onSearchQueryChange,
                    )
                    Spacer(Modifier.height(Spacing.sm))
                }
            }

            Spacer(Modifier.height(Spacing.md))
            SegmentedControl(selected = state.selectedTab, onSelect = onTabSelect)
            Spacer(Modifier.height(Spacing.md))

            when (state.selectedTab) {
                WardrobeTab.Clothes -> {
                    WardrobeCategoryStrip(
                        selectedBucket   = state.filterBucket,
                        hasActiveFilters = hasActiveFilters,
                        onBucketSelect   = onFilterBucket,
                        onFilterClick    = { sheetOpen = true },
                    )
                    Spacer(Modifier.height(Spacing.lg))
                    PiecesContent(
                        state                   = state,
                        onItemClick             = onItemClick,
                        bottomPadding           = pad.calculateBottomPadding(),
                        sharedTransitionScope   = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope,
                        modifier                = Modifier.weight(1f),
                    )
                }
                WardrobeTab.Outfits -> {
                    FitsFilterRow(
                        hasActiveFilters = state.filterWeather != null || state.filterStyle != null,
                        onFilterClick    = { sheetOpen = true },
                    )
                    Spacer(Modifier.height(Spacing.lg))
                    FitsContent(
                        state         = state,
                        onOutfitClick = onOutfitClick,
                        bottomPadding = pad.calculateBottomPadding(),
                        modifier      = Modifier.weight(1f),
                    )
                }
                WardrobeTab.Collections -> CollectionsContent(
                    state              = state,
                    onOutfitClick      = onOutfitClick,
                    onCreateCollection = onCreateCollection,
                    onRenameCollection = onRenameCollection,
                    onDeleteCollection = onDeleteCollection,
                    bottomPadding      = pad.calculateBottomPadding(),
                    modifier           = Modifier.weight(1f),
                )
            }
        }

        if (sheetOpen) {
            WardrobeFiltersSheet(
                state           = state,
                showColors      = state.selectedTab == WardrobeTab.Clothes,
                onFilterColor   = onFilterColor,
                onFilterWeather = onFilterWeather,
                onFilterStyle   = onFilterStyle,
                onClearFilters  = { onClearFilters() },
                onDismiss       = { sheetOpen = false },
            )
        }
    }
}
