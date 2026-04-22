package com.example.outfitai.ui.wardrobe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.outfitai.data.model.ItemConstants
import com.example.outfitai.data.model.ItemOutDto
import com.example.outfitai.ui.components.AppBottomBar
import com.example.outfitai.ui.components.BottomBarDest
import com.example.outfitai.ui.upload.rememberUploadLauncher
import com.example.outfitai.util.mediaUrl

private enum class WardrobeTab { Pieces, Fits }

@Composable
fun WardrobeRoute(
    onLogout: () -> Unit,
    onItemClick: (Int) -> Unit,
    onStudioClick: () -> Unit,
    vm: WardrobeViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()
    val upload = rememberUploadLauncher(onDone = { vm.refresh() })

    WardrobeScreen(
        state = state,
        onRefresh = vm::refresh,
        onLogout = onLogout,
        onItemClick = onItemClick,
        onStudioClick = onStudioClick,
        onAddClick = upload.launch,
        onFilterCategory = vm::setFilterCategory,
        onFilterColor = vm::setFilterColor,
        onFilterSeason = vm::setFilterSeason,
        onFilterOccasion = vm::setFilterOccasion,
        onClearFilters = vm::clearFilters,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WardrobeScreen(
    state: WardrobeUiState,
    onRefresh: () -> Unit,
    onLogout: () -> Unit,
    onItemClick: (Int) -> Unit,
    onStudioClick: () -> Unit,
    onAddClick: () -> Unit,
    onFilterCategory: (String?) -> Unit,
    onFilterColor: (String?) -> Unit,
    onFilterSeason: (String?) -> Unit,
    onFilterOccasion: (String?) -> Unit,
    onClearFilters: () -> Unit,
) {
    var tab by remember { mutableStateOf(WardrobeTab.Pieces) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Wardrobe",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp,
                    )
                },
                actions = {
                    IconButton(onClick = onRefresh, enabled = !state.isLoading) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                ),
            )
        },
        bottomBar = {
            Box(modifier = Modifier.padding(bottom = 24.dp)) {
                AppBottomBar(
                    active = BottomBarDest.WARDROBE,
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
                .padding(pad)
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(12.dp))

            // Segmented control
            SegmentedControl(
                selected = tab,
                onSelect = { tab = it },
            )

            Spacer(Modifier.height(12.dp))

            // Filter chips
            FilterChipsRow(
                state = state,
                onFilterCategory = onFilterCategory,
                onFilterColor = onFilterColor,
                onFilterSeason = onFilterSeason,
                onFilterOccasion = onFilterOccasion,
                onClearFilters = onClearFilters,
            )

            Spacer(Modifier.height(16.dp))

            when (tab) {
                WardrobeTab.Pieces -> PiecesContent(
                    state = state,
                    onItemClick = onItemClick,
                    modifier = Modifier.weight(1f),
                )
                WardrobeTab.Fits -> FitsContent(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SegmentedControl(
    selected: WardrobeTab,
    onSelect: (WardrobeTab) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
        ) {
            WardrobeTab.entries.forEach { tabOption ->
                val isSelected = selected == tabOption
                Surface(
                    onClick = { onSelect(tabOption) },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.surfaceContainerLowest else Color.Transparent,
                    shadowElevation = if (isSelected) 1.dp else 0.dp,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = tabOption.name,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                            .wrapContentWidth(Alignment.CenterHorizontally),
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterChipsRow(
    state: WardrobeUiState,
    onFilterCategory: (String?) -> Unit,
    onFilterColor: (String?) -> Unit,
    onFilterSeason: (String?) -> Unit,
    onFilterOccasion: (String?) -> Unit,
    onClearFilters: () -> Unit,
) {
    val allSelected = state.filterCategory == null &&
        state.filterColor == null &&
        state.filterSeason == null &&
        state.filterOccasion == null

    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // "All" chip
        FilterChip(
            selected = allSelected,
            onClick = onClearFilters,
            label = { Text("All") },
            shape = RoundedCornerShape(50),
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
            ),
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = allSelected,
                borderColor = MaterialTheme.colorScheme.outlineVariant,
                selectedBorderColor = MaterialTheme.colorScheme.primary,
            ),
        )

        DropdownFilterChip(
            label = "Category",
            selected = state.filterCategory,
            options = ItemConstants.CATEGORIES,
            onSelect = { onFilterCategory(it) },
            onClear = { onFilterCategory(null) },
        )

        DropdownFilterChip(
            label = "Season",
            selected = state.filterSeason,
            options = ItemConstants.SEASONS,
            onSelect = { onFilterSeason(it) },
            onClear = { onFilterSeason(null) },
        )

        DropdownFilterChip(
            label = "Occasion",
            selected = state.filterOccasion,
            options = ItemConstants.OCCASIONS,
            onSelect = { onFilterOccasion(it) },
            onClear = { onFilterOccasion(null) },
        )
    }
}

@Composable
private fun DropdownFilterChip(
    label: String,
    selected: String?,
    options: List<String>,
    onSelect: (String) -> Unit,
    onClear: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val isActive = selected != null

    Box {
        FilterChip(
            selected = isActive,
            onClick = { expanded = true },
            label = { Text(if (isActive) selected!! else "$label ▾") },
            shape = RoundedCornerShape(50),
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
            ),
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = isActive,
                borderColor = MaterialTheme.colorScheme.outlineVariant,
                selectedBorderColor = MaterialTheme.colorScheme.primary,
            ),
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            if (isActive) {
                DropdownMenuItem(
                    text = { Text("Clear") },
                    onClick = {
                        onClear()
                        expanded = false
                    },
                )
                HorizontalDivider()
            }
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun PiecesContent(
    state: WardrobeUiState,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        state.isLoading -> Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        state.error != null -> Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = state.error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(20.dp),
            )
        }
        state.items.isEmpty() -> Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "No items yet. Tap + to upload your first piece.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(20.dp),
            )
        }
        else -> LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = modifier,
        ) {
            items(state.items, key = { it.id }) { item ->
                ItemTile(item = item, onClick = { onItemClick(item.id) })
            }
        }
    }
}

@Composable
private fun ItemTile(item: ItemOutDto, onClick: () -> Unit) {
    val filename = item.imageNoBgName ?: item.imageOriginalName

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF9F9F9),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(3f / 4f),
    ) {
        AsyncImage(
            model = mediaUrl(filename),
            contentDescription = item.category,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentScale = ContentScale.Fit,
        )
    }
}

@Composable
private fun FitsContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "TODO: Fits implementation coming soon",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
