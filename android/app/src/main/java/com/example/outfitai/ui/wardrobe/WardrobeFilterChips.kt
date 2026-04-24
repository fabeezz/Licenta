package com.example.outfitai.ui.wardrobe

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.outfitai.data.model.ItemConstants

@Composable
internal fun FilterChipsRow(
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

        if (state.selectedTab == WardrobeTab.Pieces) {
            DropdownFilterChip(
                label = "Category",
                selected = state.filterCategory,
                options = ItemConstants.CATEGORIES,
                onSelect = { onFilterCategory(it) },
                onClear = { onFilterCategory(null) },
            )
        }

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

        if (state.selectedTab == WardrobeTab.Pieces) {
            DropdownFilterChip(
                label = "Color",
                selected = state.filterColor,
                options = ItemConstants.COLORS,
                onSelect = { onFilterColor(it) },
                onClear = { onFilterColor(null) },
            )
        }
    }
}

@Composable
internal fun DropdownFilterChip(
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
                    onClick = { onClear(); expanded = false },
                )
                HorizontalDivider()
            }
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { onSelect(option); expanded = false },
                )
            }
        }
    }
}
