package com.example.outfitai.ui.itemdetails

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.outfitai.data.model.ItemConstants
import com.example.outfitai.core.ui.color.colorNameToComposeColor
import com.example.outfitai.core.media.mediaUrl
import com.example.outfitai.ui.components.LoomButton
import com.example.outfitai.ui.components.LoomButtonVariant
import com.example.outfitai.ui.components.LoomCard
import com.example.outfitai.ui.components.LoomConfirmDialog
import com.example.outfitai.ui.components.LoomImage
import com.example.outfitai.ui.components.LoomTopBarWithBack
import com.example.outfitai.ui.theme.Spacing
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ItemDetailsRoute(
    onBack: () -> Unit,
    onItemChanged: () -> Unit,
    onItemMutatedInPlace: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    vm: ItemDetailsViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    ItemDetailsScreen(
        state                   = state,
        onBack                  = onBack,
        onToggleEdit            = vm::toggleEdit,
        onSave                  = { vm.save(onItemMutatedInPlace) },
        onWear                  = { vm.wear(onItemMutatedInPlace) },
        onDelete                = { vm.delete(onItemChanged) },
        onCategory              = vm::setCategory,
        onBrand                 = vm::setBrand,
        onMaterial              = vm::setMaterial,
        onWeather               = vm::setWeather,
        onStyle                 = vm::setStyle,
        onDominantColor         = vm::setDominantColor,
        onAccentColor           = vm::toggleAccentColor,
        sharedTransitionScope   = sharedTransitionScope,
        animatedVisibilityScope = animatedVisibilityScope,
    )
}

private fun extractColorList(colorTags: Map<String, JsonElement>?, key: String): List<String> {
    val arr = (colorTags?.get(key) as? JsonArray) ?: return emptyList()
    return arr.mapNotNull { runCatching { it.jsonPrimitive.content }.getOrNull() }
}

private fun relativeTime(isoString: String?): String {
    if (isoString == null) return "never"
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    val date = try { sdf.parse(isoString) } catch (e: Exception) { return "—" } ?: return "—"
    val days = (System.currentTimeMillis() - date.time) / (1000L * 60 * 60 * 24)
    return when {
        days < 1   -> "today"
        days == 1L -> "yesterday"
        days < 7   -> "${days}d ago"
        days < 30  -> "${days / 7}w ago"
        else       -> "${days / 30}mo ago"
    }
}

private enum class EditSheet { CATEGORY, MATERIAL, BRAND, WEATHER, STYLE, NONE }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun ItemDetailsScreen(
    state: ItemDetailsUiState,
    onBack: () -> Unit,
    onToggleEdit: () -> Unit,
    onSave: () -> Unit,
    onWear: () -> Unit,
    onDelete: () -> Unit,
    onCategory: (String) -> Unit,
    onBrand: (String) -> Unit,
    onMaterial: (String) -> Unit,
    onWeather: (List<String>) -> Unit,
    onStyle: (List<String>) -> Unit,
    onDominantColor: (String) -> Unit,
    onAccentColor: (String) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    var confirmDelete by remember { mutableStateOf(false) }
    var showDominantDialog by remember { mutableStateOf(false) }
    var showAccentDialog by remember { mutableStateOf(false) }
    var activeSheet by remember { mutableStateOf(EditSheet.NONE) }
    val cs = MaterialTheme.colorScheme
    val shapes = MaterialTheme.shapes

    if (showDominantDialog) {
        ColorSelectionDialog(
            title = "Dominant Color",
            currentSelected = state.dominantColors,
            onDismiss = { showDominantDialog = false },
            onSelect = { onDominantColor(it); showDominantDialog = false },
            isMultiple = false,
        )
    }
    if (showAccentDialog) {
        ColorSelectionDialog(
            title = "Accent Colors",
            currentSelected = state.accentColors,
            onDismiss = { showAccentDialog = false },
            onSelect = onAccentColor,
            isMultiple = true,
        )
    }

    if (activeSheet == EditSheet.CATEGORY) {
        SingleSelectSheet(
            title = "Category",
            options = ItemConstants.CATEGORIES,
            selected = state.category,
            onSelect = onCategory,
            onDismiss = { activeSheet = EditSheet.NONE },
        )
    }
    if (activeSheet == EditSheet.MATERIAL) {
        SingleSelectSheet(
            title = "Material",
            options = ItemConstants.MATERIALS,
            selected = state.material,
            onSelect = onMaterial,
            onDismiss = { activeSheet = EditSheet.NONE },
        )
    }
    if (activeSheet == EditSheet.BRAND) {
        BrandEditSheet(
            initial = state.brand,
            onSave = onBrand,
            onDismiss = { activeSheet = EditSheet.NONE },
        )
    }
    if (activeSheet == EditSheet.WEATHER) {
        MultiSelectSheet(
            title = "Weather",
            options = ItemConstants.WEATHER_TAGS,
            selected = state.weather,
            onChange = onWeather,
            onDismiss = { activeSheet = EditSheet.NONE },
        )
    }
    if (activeSheet == EditSheet.STYLE) {
        MultiSelectSheet(
            title = "Style",
            options = ItemConstants.STYLES,
            selected = state.style,
            onChange = onStyle,
            onDismiss = { activeSheet = EditSheet.NONE },
        )
    }

    Scaffold(
        containerColor = cs.background,
        topBar = {
            LoomTopBarWithBack(
                title = "",
                onBack = onBack,
                actions = {
                    if (!state.isEditing) {
                        IconButton(onClick = onWear, enabled = !state.isBusy) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Mark worn", tint = cs.onSurface)
                        }
                    }
                    IconButton(onClick = onToggleEdit, enabled = !state.isLoading) {
                        Icon(
                            imageVector = if (state.isEditing) Icons.Default.Close else Icons.Default.Edit,
                            contentDescription = if (state.isEditing) "Cancel edit" else "Edit",
                            tint = cs.onSurface,
                        )
                    }
                    IconButton(onClick = { confirmDelete = true }, enabled = !state.isBusy) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete item", tint = cs.error)
                    }
                },
            )
        },
        bottomBar = {
            if (state.isEditing) {
                Column {
                    HorizontalDivider(color = cs.outlineVariant.copy(alpha = 0.4f))
                    Surface(color = cs.surfaceContainerLow) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Spacing.xl, vertical = Spacing.md)
                                .navigationBarsPadding(),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                        ) {
                            LoomButton(
                                text = "Cancel",
                                onClick = onToggleEdit,
                                enabled = !state.isBusy,
                                variant = LoomButtonVariant.Secondary,
                                modifier = Modifier.weight(1f),
                            )
                            LoomButton(
                                text = "Save Changes",
                                onClick = onSave,
                                enabled = !state.isBusy,
                                isLoading = state.isBusy,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        },
    ) { pad ->
        Box(Modifier.fillMaxSize().padding(pad)) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                state.error != null -> Text(
                    text = state.error,
                    color = cs.error,
                    modifier = Modifier.align(Alignment.Center).padding(Spacing.xl),
                )

                state.item == null -> Text(
                    text = "Item not found",
                    modifier = Modifier.align(Alignment.Center),
                )

                else -> {
                    val item = state.item
                    val filename = item.imageNoBgName ?: item.imageOriginalName
                    val dominant = if (state.isEditing) state.dominantColors
                                   else remember(item.id, item.colorTags) { extractColorList(item.colorTags, "dominant") }
                    val accent = if (state.isEditing) state.accentColors
                                 else remember(item.id, item.colorTags) { extractColorList(item.colorTags, "accent") }
                    val lastWornText = remember(item.lastWornAt) { relativeTime(item.lastWornAt) }

                    BoxWithConstraints(Modifier.fillMaxSize()) {
                    val heroHeight = maxHeight * 0.46f

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = Spacing.xl)
                            .padding(bottom = Spacing.xxl),
                    ) {
                        Spacer(Modifier.height(Spacing.sm))

                        // Hero
                        Surface(
                            color = cs.surfaceContainerLowest,
                            shape = shapes.extraLarge,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(heroHeight),
                                contentAlignment = Alignment.Center,
                            ) {
                                with(sharedTransitionScope) {
                                    LoomImage(
                                        model = mediaUrl(filename),
                                        contentDescription = item.category,
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .sharedElement(
                                                rememberSharedContentState(key = "item-image-${item.id}"),
                                                animatedVisibilityScope = animatedVisibilityScope,
                                            ),
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(Spacing.lg))

                        // Color swatches (left) + wear stats (right)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                if (state.isEditing && dominant.isEmpty()) {
                                    ColorSwatch(
                                        name = "Pick",
                                        role = "",
                                        size = 44.dp,
                                        color = cs.surfaceContainerHigh,
                                        clickable = true,
                                        onClick = { showDominantDialog = true },
                                    )
                                } else {
                                    dominant.take(1).forEach { name ->
                                        ColorSwatch(
                                            name = name,
                                            role = "",
                                            size = 44.dp,
                                            color = colorNameToComposeColor(name, cs.surfaceVariant),
                                            clickable = state.isEditing,
                                            onClick = { showDominantDialog = true },
                                        )
                                    }
                                }
                                if (state.isEditing && accent.isEmpty()) {
                                    ColorSwatch(
                                        name = "Add",
                                        role = "",
                                        size = 36.dp,
                                        color = cs.surfaceContainerHigh,
                                        clickable = true,
                                        onClick = { showAccentDialog = true },
                                    )
                                } else {
                                    accent.take(3).forEach { name ->
                                        ColorSwatch(
                                            name = name,
                                            role = "",
                                            size = 36.dp,
                                            color = colorNameToComposeColor(name, cs.surfaceVariant),
                                            clickable = state.isEditing,
                                            onClick = { showAccentDialog = true },
                                        )
                                    }
                                }
                                if (state.isEditing && accent.isNotEmpty() && accent.size < 5) {
                                    Surface(
                                        shape = CircleShape,
                                        color = cs.surfaceContainerHigh,
                                        onClick = { showAccentDialog = true },
                                        modifier = Modifier.size(32.dp),
                                    ) {
                                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                            Icon(
                                                Icons.Default.Add,
                                                contentDescription = "Add accent",
                                                modifier = Modifier.size(16.dp),
                                            )
                                        }
                                    }
                                }
                            }

                            // Wear stats
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${item.wearCount}×",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontFeatureSettings = "tnum",
                                    ),
                                    color = cs.onSurface,
                                )
                                Text(
                                    text = lastWornText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = cs.onSurfaceVariant,
                                )
                            }
                        }

                        Spacer(Modifier.height(Spacing.lg))

                        // Attribute list
                        LoomCard(modifier = Modifier.fillMaxWidth()) {
                            DetailRow(
                                label = "Category",
                                editable = state.isEditing,
                                onClick = if (state.isEditing) ({ activeSheet = EditSheet.CATEGORY }) else null,
                            ) {
                                DetailValueText(if (state.isEditing) state.category else item.category)
                            }
                            HorizontalDivider(color = cs.outlineVariant)
                            DetailRow(
                                label = "Material",
                                editable = state.isEditing,
                                onClick = if (state.isEditing) ({ activeSheet = EditSheet.MATERIAL }) else null,
                            ) {
                                DetailValueText(if (state.isEditing) state.material else item.material)
                            }
                            HorizontalDivider(color = cs.outlineVariant)
                            DetailRow(
                                label = "Brand",
                                editable = state.isEditing,
                                onClick = if (state.isEditing) ({ activeSheet = EditSheet.BRAND }) else null,
                            ) {
                                DetailValueText(if (state.isEditing) state.brand else item.brand)
                            }
                            HorizontalDivider(color = cs.outlineVariant)
                            DetailRow(
                                label = "Weather",
                                editable = state.isEditing,
                                onClick = if (state.isEditing) ({ activeSheet = EditSheet.WEATHER }) else null,
                            ) {
                                DetailChipsRow(if (state.isEditing) state.weather else item.weather)
                            }
                            HorizontalDivider(color = cs.outlineVariant)
                            DetailRow(
                                label = "Style",
                                editable = state.isEditing,
                                onClick = if (state.isEditing) ({ activeSheet = EditSheet.STYLE }) else null,
                            ) {
                                DetailChipsRow(if (state.isEditing) state.style else item.style)
                            }
                        }
                    }
                    } // BoxWithConstraints
                }
            }

            if (confirmDelete) {
                val categoryLabel = state.item?.category
                    ?.replaceFirstChar { it.uppercase() }
                    ?: "Item"
                LoomConfirmDialog(
                    title = "Delete item?",
                    message = "$categoryLabel will be removed from your wardrobe. This can't be undone.",
                    confirmText = "Delete",
                    destructive = true,
                    onConfirm = { confirmDelete = false; onDelete() },
                    onDismiss = { confirmDelete = false },
                )
            }
        }
    }
}
