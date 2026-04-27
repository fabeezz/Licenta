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
import com.example.outfitai.util.capitalizeFirst

@Composable
internal fun FilterChipsRow(
    state: WardrobeUiState,
    onFilterCategory: (String?) -> Unit,
    onFilterColor: (String?) -> Unit,
    onFilterWeather: (String?) -> Unit,
    onFilterStyle: (String?) -> Unit,
    onClearFilters: () -> Unit,
) {
    val allSelected = state.filterCategory == null &&
            state.filterColor == null &&
            state.filterWeather == null &&
            state.filterStyle == null

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
            label = "Weather",
            selected = state.filterWeather,
            options = ItemConstants.WEATHER_TAGS,
            onSelect = { onFilterWeather(it) },
            onClear = { onFilterWeather(null) },
        )

        DropdownFilterChip(
            label = "Style",
            selected = state.filterStyle,
            options = ItemConstants.STYLES,
            onSelect = { onFilterStyle(it) },
            onClear = { onFilterStyle(null) },
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
            label = { Text(if (isActive) selected!!.capitalizeFirst() else "$label ▾") },
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
                    text = { Text(option.capitalizeFirst()) },
                    onClick = { onSelect(option); expanded = false },
                )
            }
        }
    }
}
