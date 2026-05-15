package com.example.outfitai.ui.wardrobe

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    val fade = remember(bg) { Brush.horizontalGradient(listOf(Color.Transparent, bg)) }

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
                    .background(fade),
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

private val sortedColors = listOf(
    "black", "white", "gray", "beige", "brown",
    "burgundy", "red", "pink", "orange", "yellow",
    "olive", "dark green", "green", "cyan", "blue", "navy", "purple",
)

private val weatherPills = listOf(
    Triple("cold", "Cold", Icons.Filled.AcUnit),
    Triple("warm", "Warm", Icons.Filled.WbSunny),
    Triple("rainy", "Rainy", Icons.Filled.Umbrella),
    Triple("all-weather", "All Weather", Icons.Filled.Cloud),
)

private val stylePills = listOf(
    Triple("casual", "Casual", Icons.Filled.Checkroom),
    Triple("formal", "Formal", Icons.Filled.Work),
    Triple("sporty", "Sporty", Icons.Filled.DirectionsRun),
)

@Composable
private fun FilterPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector? = null,
) {
    val cs = MaterialTheme.colorScheme
    val bg = if (selected) cs.primary else cs.surfaceContainer
    val fg = if (selected) cs.onPrimary else cs.onSurfaceVariant

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        modifier = Modifier
            .clip(CircleShape)
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
    ) {
        icon?.let {
            Icon(imageVector = it, contentDescription = null, tint = fg, modifier = Modifier.size(16.dp))
        }
        Text(text = label, color = fg, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WardrobeFiltersSheet(
    initialColor: String?,
    initialWeather: String?,
    initialStyle: String?,
    showColors: Boolean = true,
    onApply: (color: String?, weather: String?, style: String?) -> Unit,
    onDismiss: () -> Unit,
) {
    var color by rememberSaveable { mutableStateOf(initialColor) }
    var weather by rememberSaveable { mutableStateOf(initialWeather) }
    var style by rememberSaveable { mutableStateOf(initialStyle) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.xl, vertical = Spacing.lg),
            horizontalAlignment = Alignment.Start,
        ) {
            if (showColors) {
                Text(
                    text = "Colors",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = Spacing.sm),
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    sortedColors.forEach { name ->
                        ColorSwatch(
                            name = name,
                            color = colorForName(name),
                            isLight = name == "white" || name == "beige" || name == "cream",
                            selected = color == name,
                            onClick = { color = if (color == name) null else name },
                        )
                    }
                }
                Spacer(modifier = Modifier.height(Spacing.xl))
            }

            Text(
                text = "Weather",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = Spacing.sm),
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                weatherPills.forEach { (key, label, icon) ->
                    FilterPill(
                        label    = label,
                        selected = weather == key,
                        onClick  = { weather = if (weather == key) null else key },
                        icon     = icon,
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xl))

            Text(
                text = "Style",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = Spacing.sm),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                stylePills.forEach { (key, label, icon) ->
                    FilterPill(
                        label    = label,
                        selected = style == key,
                        onClick  = { style = if (style == key) null else key },
                        icon     = icon,
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xxl))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.lg),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                LoomButton(
                    text     = "Clear",
                    onClick  = { color = null; weather = null; style = null },
                    variant  = LoomButtonVariant.Secondary,
                    modifier = Modifier.weight(1f),
                )
                LoomButton(
                    text     = "Apply",
                    onClick  = { onApply(color, weather, style) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

// ── Color swatch ──────────────────────────────────────────────────────────────

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
