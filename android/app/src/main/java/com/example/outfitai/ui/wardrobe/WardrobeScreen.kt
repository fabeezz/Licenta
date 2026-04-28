package com.example.outfitai.ui.wardrobe

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.outfitai.ui.components.AppBottomBar
import com.example.outfitai.ui.components.BottomBarDest
import com.example.outfitai.ui.upload.rememberUploadLauncher

@Composable
fun WardrobeRoute(
    onLogout: () -> Unit,
    onItemClick: (Int) -> Unit,
    onOutfitClick: (Int) -> Unit,
    onStudioClick: () -> Unit,
    onTripClick: () -> Unit = {},
    vm: WardrobeViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()
    val upload = rememberUploadLauncher(onDone = { vm.refresh() })

    WardrobeScreen(
        state = state,
        onRefresh = vm::refresh,
        onLogout = onLogout,
        onItemClick = onItemClick,
        onOutfitClick = onOutfitClick,
        onStudioClick = onStudioClick,
        onTripClick = onTripClick,
        onAddClick = upload.launch,
        onTabSelect = vm::setTab,
        onFilterCategory = vm::setFilterCategory,
        onFilterColor = vm::setFilterColor,
        onFilterWeather = vm::setFilterWeather,
        onFilterStyle = vm::setFilterStyle,
        onClearFilters = vm::clearFilters,
        onCreateCollection = vm::createCollection,
        onRenameCollection = vm::renameCollection,
        onDeleteCollection = vm::deleteCollection,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WardrobeScreen(
    state: WardrobeUiState,
    onRefresh: () -> Unit,
    onLogout: () -> Unit,
    onItemClick: (Int) -> Unit,
    onOutfitClick: (Int) -> Unit,
    onStudioClick: () -> Unit,
    onTripClick: () -> Unit,
    onAddClick: () -> Unit,
    onTabSelect: (WardrobeTab) -> Unit,
    onFilterCategory: (String?) -> Unit,
    onFilterColor: (String?) -> Unit,
    onFilterWeather: (String?) -> Unit,
    onFilterStyle: (String?) -> Unit,
    onClearFilters: () -> Unit,
    onCreateCollection: (String, List<Int>) -> Unit,
    onRenameCollection: (Int, String) -> Unit,
    onDeleteCollection: (Int) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Wardrobe", style = MaterialTheme.typography.titleLarge)
                },
                actions = {
                    IconButton(onClick = onRefresh, enabled = !state.isLoading) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
        bottomBar = {
            Box(modifier = Modifier.padding(bottom = 24.dp)) {
                AppBottomBar(
                    active = BottomBarDest.WARDROBE,
                    onTrip = onTripClick,
                    onStudio = onStudioClick,
                    onAdd = onAddClick,
                    onWardrobe = {},
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = pad.calculateTopPadding())
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(12.dp))
            SegmentedControl(selected = state.selectedTab, onSelect = onTabSelect)
            Spacer(Modifier.height(12.dp))
            if (state.selectedTab != WardrobeTab.Collections) {
                FilterChipsRow(
                    state = state,
                    onFilterCategory = onFilterCategory,
                    onFilterColor = onFilterColor,
                    onFilterWeather = onFilterWeather,
                    onFilterStyle = onFilterStyle,
                    onClearFilters = onClearFilters,
                )
                Spacer(Modifier.height(16.dp))
            }
            when (state.selectedTab) {
                WardrobeTab.Pieces -> PiecesContent(
                    state = state,
                    onItemClick = onItemClick,
                    bottomPadding = pad.calculateBottomPadding(),
                    modifier = Modifier.weight(1f),
                )
                WardrobeTab.Fits -> FitsContent(
                    state = state,
                    onOutfitClick = onOutfitClick,
                    bottomPadding = pad.calculateBottomPadding(),
                    modifier = Modifier.weight(1f),
                )
                WardrobeTab.Collections -> CollectionsContent(
                    state = state,
                    onOutfitClick = onOutfitClick,
                    onCreateCollection = onCreateCollection,
                    onRenameCollection = onRenameCollection,
                    onDeleteCollection = onDeleteCollection,
                    bottomPadding = pad.calculateBottomPadding(),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
