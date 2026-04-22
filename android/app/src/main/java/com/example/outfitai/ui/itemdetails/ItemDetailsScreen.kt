package com.example.outfitai.ui.itemdetails

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.outfitai.data.model.ItemConstants
import com.example.outfitai.ui.components.DropdownSelector
import com.example.outfitai.util.mediaUrl
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun ItemDetailsRoute(
    onBack: () -> Unit,
    onItemChanged: () -> Unit,
    vm: ItemDetailsViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()
    ItemDetailsScreen(
        state = state,
        onBack = onBack,
        onRefresh = vm::load,
        onToggleEdit = vm::toggleEdit,
        onSave = { vm.save(onItemChanged) },
        onWear = { vm.wear(onItemChanged) },
        onDelete = { vm.delete(onItemChanged) },
        onCategory = vm::setCategory,
        onBrand = vm::setBrand,
        onMaterial = vm::setMaterial,
        onSeason = vm::setSeason,
        onOccasion = vm::setOccasion,
    )
}

private fun extractColorList(colorTags: Map<String, JsonElement>?, key: String): List<String> {
    val arr = (colorTags?.get(key) as? JsonArray) ?: return emptyList()
    return arr.mapNotNull { runCatching { it.jsonPrimitive.content }.getOrNull() }
}

private fun colorNameToColor(name: String, fallback: Color): Color = when (name.lowercase().trim()) {
    "black", "noir", "jet"                        -> Color(0xFF1A1A1A)
    "white", "blanc", "ivory", "cream"            -> Color(0xFFF0EFE8)
    "gray", "grey", "silver", "charcoal"          -> Color(0xFF9E9E9E)
    "navy", "navy blue"                           -> Color(0xFF1B2A4A)
    "blue", "cobalt", "sapphire"                  -> Color(0xFF3B5BDB)
    "red", "crimson", "scarlet"                   -> Color(0xFFE03131)
    "green", "olive", "forest"                    -> Color(0xFF2F9E44)
    "brown", "tan", "camel", "khaki", "beige"     -> Color(0xFFA67C52)
    "yellow", "mustard", "gold"                   -> Color(0xFFE8B400)
    "orange", "rust", "amber"                     -> Color(0xFFE8590C)
    "pink", "blush", "rose"                       -> Color(0xFFE64980)
    "purple", "violet", "lavender"                -> Color(0xFF7048E8)
    "teal", "cyan", "turquoise"                   -> Color(0xFF0CA678)
    else                                          -> fallback
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ItemDetailsScreen(
    state: ItemDetailsUiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onToggleEdit: () -> Unit,
    onSave: () -> Unit,
    onWear: () -> Unit,
    onDelete: () -> Unit,
    onCategory: (String) -> Unit,
    onBrand: (String) -> Unit,
    onMaterial: (String) -> Unit,
    onSeason: (String) -> Unit,
    onOccasion: (String) -> Unit,
) {
    var confirmDelete by remember { mutableStateOf(false) }
    val cs = MaterialTheme.colorScheme
    val shapes = MaterialTheme.shapes

    Scaffold(
        containerColor = cs.background,
        topBar = {
            Column {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = cs.surface.copy(alpha = 0.92f),
                    ),
                    navigationIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = cs.onSurface
                                )
                            }
                            Text(
                                text = "OutfitAI",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = cs.onSurface,
                            )
                        }
                    },
                    title = {},
                    actions = {
                        IconButton(
                            onClick = onRefresh,
                            enabled = !state.isLoading && !state.isBusy
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = cs.onSurface
                            )
                        }
                    }
                )
                HorizontalDivider(color = cs.outlineVariant.copy(alpha = 0.5f))
            }
        }
    ) { pad ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(pad)
        ) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                state.error != null -> Text(
                    text = state.error,
                    color = cs.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(20.dp)
                )

                state.item == null -> Text(
                    "Item not found",
                    modifier = Modifier.align(Alignment.Center)
                )

                else -> {
                    val item = state.item
                    val filename = item.imageNoBgName ?: item.imageOriginalName
                    val dominant = extractColorList(item.colorTags, "dominant")
                    val accent = extractColorList(item.colorTags, "accent")

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 48.dp)
                    ) {
                        Spacer(Modifier.height(16.dp))

                        // ── Hero image card ──
                        Surface(
                            color = cs.surfaceContainer,
                            shape = shapes.extraLarge,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(320.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = mediaUrl(filename),
                                    contentDescription = item.category,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 280.dp)
                                )
                                FilledIconButton(
                                    onClick = onToggleEdit,
                                    shape = CircleShape,
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = cs.surfaceContainerLowest.copy(alpha = 0.92f),
                                        contentColor = cs.onSurface
                                    ),
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(12.dp)
                                ) {
                                    Icon(
                                        imageVector = if (state.isEditing) Icons.Default.Close else Icons.Default.Edit,
                                        contentDescription = if (state.isEditing) "Cancel edit" else "Edit"
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(32.dp))

                        // ── Title block ──
                        Text(
                            text = item.category?.replaceFirstChar { it.uppercaseChar() } ?: "Item",
                            style = MaterialTheme.typography.headlineMedium,
                            color = cs.onSurface
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "${item.brand ?: "—"} • ${item.category ?: "—"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = cs.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Worn ${item.wearCount} times",
                            style = MaterialTheme.typography.labelLarge,
                            color = cs.onSurfaceVariant
                        )

                        Spacer(Modifier.height(20.dp))

                        // Mark Worn CTA — only in view mode
                        if (!state.isEditing) {
                            Button(
                                onClick = onWear,
                                enabled = !state.isBusy,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = cs.primary,
                                    contentColor = cs.onPrimary
                                )
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Mark Worn",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(Modifier.height(32.dp))

                        // ── Metadata cards (stacked) ──
                        InfoCard(
                            title = "Classification",
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (state.isEditing) {
                                DropdownSelector(
                                    label = "Category",
                                    selectedOption = state.category,
                                    options = ItemConstants.CATEGORIES,
                                    onOptionSelected = onCategory
                                )
                                Spacer(Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = state.brand,
                                    onValueChange = onBrand,
                                    label = { Text("Brand") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = shapes.large
                                )
                            } else {
                                InfoRow(label = "Category", value = item.category ?: "—")
                                Spacer(Modifier.height(12.dp))
                                InfoRow(label = "Brand", value = item.brand ?: "—")
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        InfoCard(
                            title = "Attributes",
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (state.isEditing) {
                                DropdownSelector(
                                    label = "Material",
                                    selectedOption = state.material,
                                    options = ItemConstants.MATERIALS,
                                    onOptionSelected = onMaterial
                                )
                                Spacer(Modifier.height(8.dp))
                                DropdownSelector(
                                    label = "Season",
                                    selectedOption = state.season,
                                    options = ItemConstants.SEASONS,
                                    onOptionSelected = onSeason
                                )
                            } else {
                                InfoRow(label = "Material", value = item.material ?: "—")
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    text = "Season",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = cs.onSurfaceVariant
                                )
                                Spacer(Modifier.height(6.dp))
                                if (item.season != null) {
                                    SeasonChip(season = item.season)
                                } else {
                                    Text("—", style = MaterialTheme.typography.bodyMedium, color = cs.onSurface)
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // ── Color Profile card ──
                        InfoCard(
                            title = "Color Profile",
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (dominant.isEmpty() && accent.isEmpty()) {
                                Text(
                                    "No color data",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = cs.onSurfaceVariant
                                )
                            } else {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    dominant.take(1).forEach { name ->
                                        ColorSwatch(
                                            name = name,
                                            role = "Dominant",
                                            size = 64.dp,
                                            color = colorNameToColor(name, cs.surfaceVariant)
                                        )
                                    }
                                    accent.take(3).forEach { name ->
                                        ColorSwatch(
                                            name = name,
                                            role = "Accent",
                                            size = 48.dp,
                                            color = colorNameToColor(name, cs.surfaceVariant)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // ── Occasion card (full width) ──
                        InfoCard(
                            title = "Occasion",
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val currentOccasion = if (state.isEditing) state.occasion else item.occasion
                                ItemConstants.OCCASIONS.forEach { option ->
                                    OccasionChip(
                                        label = option.replaceFirstChar { it.uppercaseChar() },
                                        selected = currentOccasion == option,
                                        clickable = state.isEditing,
                                        onClick = { onOccasion(option) }
                                    )
                                }
                            }
                        }

                        // ── Edit mode: Save / Cancel bar ──
                        if (state.isEditing) {
                            Spacer(Modifier.height(20.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onToggleEdit,
                                    enabled = !state.isBusy,
                                    shape = CircleShape,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp)
                                ) {
                                    Text("Cancel")
                                }
                                Button(
                                    onClick = onSave,
                                    enabled = !state.isBusy,
                                    shape = CircleShape,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = cs.primary,
                                        contentColor = cs.onPrimary
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp)
                                ) {
                                    Text("Save Changes")
                                }
                            }
                        }

                        // ── Danger zone ──
                        Spacer(Modifier.height(32.dp))
                        HorizontalDivider(color = cs.outlineVariant.copy(alpha = 0.5f))
                        Spacer(Modifier.height(16.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            TextButton(
                                onClick = { confirmDelete = true },
                                enabled = !state.isBusy,
                                shape = CircleShape,
                                colors = ButtonDefaults.textButtonColors(contentColor = cs.error)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Delete Item")
                            }
                        }
                    }
                }
            }

            if (confirmDelete) {
                val cs2 = MaterialTheme.colorScheme
                AlertDialog(
                    onDismissRequest = { confirmDelete = false },
                    title = { Text("Delete item?") },
                    text = { Text("This action cannot be undone.") },
                    confirmButton = {
                        Button(
                            onClick = { confirmDelete = false; onDelete() },
                            colors = ButtonDefaults.buttonColors(containerColor = cs2.error)
                        ) { Text("Delete") }
                    },
                    dismissButton = {
                        TextButton(onClick = { confirmDelete = false }) { Text("Cancel") }
                    }
                )
            }
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    Surface(
        color = cs.surfaceContainerLow,
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, cs.outlineVariant.copy(alpha = 0.5f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = cs.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    val cs = MaterialTheme.colorScheme
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = cs.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = cs.onSurface
        )
    }
}

@Composable
private fun SeasonChip(season: String) {
    val cs = MaterialTheme.colorScheme
    Surface(
        shape = CircleShape,
        color = cs.surfaceContainer,
        border = BorderStroke(1.dp, cs.outlineVariant.copy(alpha = 0.4f))
    ) {
        Text(
            text = season,
            style = MaterialTheme.typography.labelLarge,
            color = cs.onSurface,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun ColorSwatch(name: String, role: String, size: Dp, color: Color) {
    val cs = MaterialTheme.colorScheme
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(color)
                .border(1.dp, cs.outlineVariant, CircleShape)
        )
        Text(
            text = name.replaceFirstChar { it.uppercaseChar() },
            style = MaterialTheme.typography.labelLarge,
            color = cs.onSurface
        )
        Text(
            text = role,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
            color = cs.onSurfaceVariant
        )
    }
}

@Composable
private fun OccasionChip(
    label: String,
    selected: Boolean,
    clickable: Boolean,
    onClick: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    Surface(
        shape = CircleShape,
        color = if (selected) cs.primary else cs.surfaceContainer,
        border = if (!selected) BorderStroke(1.dp, cs.outlineVariant.copy(alpha = 0.4f)) else null,
        modifier = if (clickable) Modifier.clickable(onClick = onClick) else Modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) cs.onPrimary else cs.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}
