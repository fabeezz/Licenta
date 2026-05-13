package com.example.outfitai.ui.wardrobe

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.outfitai.data.model.ItemConstants
import com.example.outfitai.ui.components.LoomButton
import com.example.outfitai.ui.components.LoomButtonVariant
import com.example.outfitai.ui.theme.Spacing

// ── Category strip (Pieces tab) ───────────────────────────────────────────────

@Composable
internal fun WardrobeCategoryStrip(
    selectedBucket: CategoryBucket?,
    hasActiveFilters: Boolean,
    onBucketSelect: (CategoryBucket?) -> Unit,
    onFilterClick: () -> Unit,
) {
    val bg = MaterialTheme.colorScheme.background

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CategoryPill(label = "All", selected = selectedBucket == null, onClick = { onBucketSelect(null) })
                CategoryBucket.entries.forEach { bucket ->
                    CategoryPill(
                        label = bucket.label,
                        selected = selectedBucket == bucket,
                        onClick = { onBucketSelect(bucket) },
                    )
                }
                Spacer(Modifier.width(Spacing.md))
            }
            // Right-edge fade
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(Spacing.xxl)
                    .matchParentSize()
                    .background(Brush.horizontalGradient(listOf(Color.Transparent, bg))),
            )
        }

        Spacer(Modifier.width(Spacing.sm))

        Box(contentAlignment = Alignment.TopEnd) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(onClick = onFilterClick),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Filters",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp),
                )
            }
            if (hasActiveFilters) {
                Box(
                    modifier = Modifier
                        .offset(x = 2.dp, y = (-2).dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                )
            }
        }
    }
}

@Composable
private fun CategoryPill(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val text = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.outline

    Text(
        text = label,
        color = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier
            .clip(CircleShape)
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
    )
}

// ── Fits filter row (tune button only, no category strip) ─────────────────────

@Composable
internal fun FitsFilterRow(hasActiveFilters: Boolean, onFilterClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(onClick = onFilterClick),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Filters",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp),
                )
            }
            if (hasActiveFilters) {
                Box(
                    modifier = Modifier
                        .offset(x = 2.dp, y = (-2).dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                )
            }
        }
    }
}

// ── Filter bottom sheet ───────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WardrobeFiltersSheet(
    state: WardrobeUiState,
    showColors: Boolean = true,
    onFilterColor: (String?) -> Unit,
    onFilterWeather: (String?) -> Unit,
    onFilterStyle: (String?) -> Unit,
    onClearFilters: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Filters",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            HorizontalDivider(modifier = Modifier.padding(top = Spacing.md))

            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.xxl)
                    .padding(top = Spacing.xxl),
                verticalArrangement = Arrangement.spacedBy(28.dp),
            ) {
                if (showColors) {
                    ColorSection(selected = state.filterColor, onSelect = onFilterColor)
                }
                WeatherSection(selected = state.filterWeather, onSelect = onFilterWeather)
                StyleSection(selected = state.filterStyle, onSelect = onFilterStyle)
                Spacer(Modifier.height(Spacing.xs))
            }

            HorizontalDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.xxl, vertical = Spacing.xl)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LoomButton(
                    text = "Clear filters",
                    onClick = { onClearFilters(); onDismiss() },
                    variant = LoomButtonVariant.Ghost,
                )
                LoomButton(
                    text = "Done",
                    onClick = onDismiss,
                    modifier = Modifier.defaultMinSize(minWidth = 120.dp),
                )
            }
        }
    }
}

// ── Section: Colors ───────────────────────────────────────────────────────────

@Composable
private fun ColorSection(selected: String?, onSelect: (String?) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        FilterSectionHeader("Colors")
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            ItemConstants.COLORS.forEach { name ->
                ColorSwatch(
                    name = name,
                    color = colorForName(name),
                    isLight = name == "white" || name == "beige" || name == "cream",
                    selected = selected == name,
                    onClick = { onSelect(if (selected == name) null else name) },
                )
            }
        }
    }
}

@Composable
private fun ColorSwatch(
    name: String,
    color: Color,
    isLight: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .then(if (selected) Modifier.border(2.dp, cs.primary, CircleShape) else Modifier)
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(if (selected) 24.dp else 32.dp)
                .clip(CircleShape)
                .background(color)
                .then(
                    if (isLight) Modifier.border(1.dp, cs.outline, CircleShape)
                    else Modifier
                ),
        )
    }
}

private fun colorForName(name: String): Color = when (name) {
    "black"      -> Color(0xFF1C1C1E)
    "white"      -> Color(0xFFFFFFFF)
    "gray"       -> Color(0xFF808080)
    "beige"      -> Color(0xFFD2B48C)
    "burgundy"   -> Color(0xFF800020)
    "pink"       -> Color(0xFFFFC0CB)
    "red"        -> Color(0xFFCC0000)
    "brown"      -> Color(0xFF8B4513)
    "orange"     -> Color(0xFFFF8C00)
    "olive"      -> Color(0xFF6B6B2A)
    "yellow"     -> Color(0xFFFFD700)
    "dark green" -> Color(0xFF1B4332)
    "green"      -> Color(0xFF228B22)
    "cyan"       -> Color(0xFF00B4D8)
    "navy"       -> Color(0xFF001F5B)
    "blue"       -> Color(0xFF2563EB)
    "purple"     -> Color(0xFF6B21A8)
    else         -> Color(0xFF9E9E9E)
}

// ── Section: Weather ──────────────────────────────────────────────────────────

private data class WeatherOption(val key: String, val label: String, val icon: ImageVector)

private val weatherOptions = listOf(
    WeatherOption("cold", "Cold", Icons.Outlined.AcUnit),
    WeatherOption("warm", "Warm", Icons.Outlined.WbSunny),
    WeatherOption("rainy", "Rainy", Icons.Outlined.WaterDrop),
    WeatherOption("all-weather", "All Weather", Icons.Outlined.Cloud),
)

@Composable
private fun WeatherSection(selected: String?, onSelect: (String?) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        FilterSectionHeader("Weather")
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            weatherOptions.chunked(2).forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    row.forEach { opt ->
                        WeatherTile(
                            option = opt,
                            selected = selected == opt.key,
                            onClick = { onSelect(if (selected == opt.key) null else opt.key) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun WeatherTile(
    option: WeatherOption,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.lg, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(option.icon, contentDescription = null, tint = textColor, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(Spacing.sm))
        Text(option.label, color = textColor, style = MaterialTheme.typography.titleMedium)
    }
}

// ── Section: Style ────────────────────────────────────────────────────────────

private data class StyleOption(val key: String, val label: String, val icon: ImageVector)

private val styleOptions = listOf(
    StyleOption("casual", "Casual", Icons.Outlined.Checkroom),
    StyleOption("formal", "Formal", Icons.Outlined.Work),
    StyleOption("sporty", "Sporty", Icons.Outlined.DirectionsRun),
)

@Composable
private fun StyleSection(selected: String?, onSelect: (String?) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        FilterSectionHeader("Style")
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            styleOptions.forEach { opt ->
                StyleChip(
                    option = opt,
                    selected = selected == opt.key,
                    onClick = { onSelect(if (selected == opt.key) null else opt.key) },
                )
            }
        }
    }
}

@Composable
private fun StyleChip(option: StyleOption, selected: Boolean, onClick: () -> Unit) {
    val bgColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.xl, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Icon(option.icon, contentDescription = null, tint = textColor, modifier = Modifier.size(16.dp))
        Text(option.label, color = textColor, style = MaterialTheme.typography.titleMedium)
    }
}

// ── Shared ────────────────────────────────────────────────────────────────────

@Composable
private fun FilterSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
