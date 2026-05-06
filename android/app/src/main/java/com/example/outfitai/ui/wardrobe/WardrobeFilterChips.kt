package com.example.outfitai.ui.wardrobe

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.outfitai.data.model.ItemConstants

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
                horizontalArrangement = Arrangement.spacedBy(6.dp),
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
                Spacer(Modifier.width(12.dp))
            }
            // Right-edge fade
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(24.dp)
                    .matchParentSize()
                    .background(Brush.horizontalGradient(listOf(Color.Transparent, bg))),
            )
        }

        Spacer(Modifier.width(8.dp))

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
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
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
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Title
            Text(
                text = "Filters",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            HorizontalDivider(modifier = Modifier.padding(top = 12.dp))

            // Scrollable content
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(top = 24.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp),
            ) {
                if (showColors) {
                    ColorSection(selected = state.filterColor, onSelect = onFilterColor)
                }
                WeatherSection(selected = state.filterWeather, onSelect = onFilterWeather)
                StyleSection(selected = state.filterStyle, onSelect = onFilterStyle)
                Spacer(Modifier.height(4.dp))
            }

            // Footer
            HorizontalDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = { onClearFilters(); onDismiss() }) {
                    Text(
                        text = "Clear filters",
                        color = MaterialTheme.colorScheme.outline,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                    )
                }
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                    modifier = Modifier.defaultMinSize(minWidth = 120.dp, minHeight = 48.dp),
                ) {
                    Text("Done", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
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
            horizontalArrangement = Arrangement.spacedBy(12.dp),
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
    val primary = MaterialTheme.colorScheme.primary
    val borderModifier = if (selected)
        Modifier.border(2.dp, primary, CircleShape).padding(2.dp)
    else
        Modifier

    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(if (selected) Color.Transparent else Color.Transparent)
            .then(if (selected) Modifier.border(2.dp, primary, CircleShape) else Modifier)
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(if (selected) 24.dp else 32.dp)
                .clip(CircleShape)
                .background(color)
                .then(
                    if (isLight) Modifier.border(1.dp, Color(0xFFE0E0E0), CircleShape)
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

private data class WeatherOption(val key: String, val label: String, val emoji: String)

private val weatherOptions = listOf(
    WeatherOption("cold", "Cold", "❄️"),
    WeatherOption("warm", "Warm", "☀️"),
    WeatherOption("rainy", "Rainy", "🌧️"),
    WeatherOption("all-weather", "All Weather", "🌈"),
)

@Composable
private fun WeatherSection(selected: String?, onSelect: (String?) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        FilterSectionHeader("Weather")
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            weatherOptions.chunked(2).forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
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
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(option.emoji, fontSize = 18.sp)
        Spacer(Modifier.width(8.dp))
        Text(option.label, color = textColor, fontWeight = FontWeight.Medium, fontSize = 15.sp)
    }
}

// ── Section: Style ────────────────────────────────────────────────────────────

private data class StyleOption(val key: String, val label: String, val emoji: String)

private val styleOptions = listOf(
    StyleOption("casual", "Casual", "👕"),
    StyleOption("formal", "Formal", "👔"),
    StyleOption("sporty", "Sporty", "👟"),
)

@Composable
private fun StyleSection(selected: String?, onSelect: (String?) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        FilterSectionHeader("Style")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
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
            .clip(RoundedCornerShape(50))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(option.emoji, fontSize = 16.sp)
        Text(option.label, color = textColor, fontWeight = FontWeight.Medium, fontSize = 15.sp)
    }
}

// ── Shared ────────────────────────────────────────────────────────────────────

@Composable
private fun FilterSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        color = MaterialTheme.colorScheme.outline,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp,
    )
}
