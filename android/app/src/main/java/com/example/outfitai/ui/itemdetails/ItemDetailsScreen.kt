package com.example.outfitai.ui.itemdetails

import androidx.compose.foundation.background
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.outfitai.data.model.ItemConstants
import com.example.outfitai.core.ui.color.colorNameToComposeColor
import com.example.outfitai.core.media.mediaUrl
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Composable
fun ItemDetailsRoute(
    onBack: () -> Unit,
    onItemChanged: () -> Unit,
    onItemMutatedInPlace: () -> Unit,
    vm: ItemDetailsViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()
    ItemDetailsScreen(
        state = state,
        onBack = onBack,
        onToggleEdit = vm::toggleEdit,
        onSave = { vm.save(onItemMutatedInPlace) },
        onWear = { vm.wear(onItemMutatedInPlace) },
        onDelete = { vm.delete(onItemChanged) },
        onCategory = vm::setCategory,
        onBrand = vm::setBrand,
        onMaterial = vm::setMaterial,
        onWeather = vm::setWeather,
        onStyle = vm::setStyle,
        onDominantColor = vm::setDominantColor,
        onAccentColor = vm::toggleAccentColor,
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
) {
    var confirmDelete by remember { mutableStateOf(false) }
    var showDominantDialog by remember { mutableStateOf(false) }
    var showAccentDialog by remember { mutableStateOf(false) }
    var activeSheet by remember { mutableStateOf(EditSheet.NONE) }
    val cs = MaterialTheme.colorScheme
    val shapes = MaterialTheme.shapes

    // Color picker dialogs (unchanged)
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

    // Edit sheets
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
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = cs.background),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = cs.onSurface)
                    }
                },
                title = {},
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
                                .padding(horizontal = 20.dp, vertical = 12.dp)
                                .navigationBarsPadding(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            OutlinedButton(
                                onClick = onToggleEdit,
                                enabled = !state.isBusy,
                                shape = CircleShape,
                                modifier = Modifier.weight(1f).height(48.dp),
                            ) {
                                Text("Cancel")
                            }
                            Button(
                                onClick = onSave,
                                enabled = !state.isBusy,
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = cs.primary,
                                    contentColor = cs.onPrimary,
                                ),
                                modifier = Modifier.weight(1f).height(48.dp),
                            ) {
                                if (state.isBusy) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = cs.onPrimary,
                                        strokeWidth = 2.dp,
                                    )
                                } else {
                                    Text("Save Changes")
                                }
                            }
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
                    modifier = Modifier.align(Alignment.Center).padding(20.dp),
                )

                state.item == null -> Text(
                    text = "Item not found",
                    modifier = Modifier.align(Alignment.Center),
                )

                else -> {
                    val item = state.item
                    val filename = item.imageNoBgName ?: item.imageOriginalName
                    val dominant = if (state.isEditing) state.dominantColors
                                   else extractColorList(item.colorTags, "dominant")
                    val accent = if (state.isEditing) state.accentColors
                                 else extractColorList(item.colorTags, "accent")

                    BoxWithConstraints(Modifier.fillMaxSize()) {
                    val heroHeight = maxHeight * 0.46f

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 24.dp),
                    ) {
                        Spacer(Modifier.height(8.dp))

                        // Hero — responsive, ~46% of screen height
                        Surface(
                            color = cs.surfaceContainerLowest,
                            shape = shapes.extraLarge,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(heroHeight),
                                contentAlignment = Alignment.Center,
                            ) {
                                AsyncImage(
                                    model = mediaUrl(filename),
                                    contentDescription = item.category,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Color swatches (left) + wear stats (right)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                                    IconButton(
                                        onClick = { showAccentDialog = true },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(cs.surfaceContainerHigh),
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = "Add accent",
                                            modifier = Modifier.size(16.dp),
                                        )
                                    }
                                }
                            }

                            // Wear stats — right aligned
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${item.wearCount}×",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = cs.onSurfaceVariant,
                                )
                                Text(
                                    text = relativeTime(item.lastWornAt),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = cs.onSurfaceVariant,
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Info pill grid
                        Surface(
                            color = cs.surfaceContainerLow,
                            shape = shapes.large,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                // 3-col row: Category + Material + Brand
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    InfoValueTile(
                                        label = "Category",
                                        value = if (state.isEditing) state.category else item.category,
                                        modifier = Modifier.weight(1f),
                                        editable = state.isEditing,
                                        onClick = if (state.isEditing) ({ activeSheet = EditSheet.CATEGORY }) else null,
                                    )
                                    InfoValueTile(
                                        label = "Material",
                                        value = if (state.isEditing) state.material else item.material,
                                        modifier = Modifier.weight(1f),
                                        editable = state.isEditing,
                                        onClick = if (state.isEditing) ({ activeSheet = EditSheet.MATERIAL }) else null,
                                    )
                                    InfoValueTile(
                                        label = "Brand",
                                        value = if (state.isEditing) state.brand else item.brand,
                                        modifier = Modifier.weight(1f),
                                        editable = state.isEditing,
                                        onClick = if (state.isEditing) ({ activeSheet = EditSheet.BRAND }) else null,
                                    )
                                }

                                // 2-col row: Weather + Style
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    InfoChipsTile(
                                        label = "Weather",
                                        items = if (state.isEditing) state.weather else item.weather,
                                        modifier = Modifier.weight(1f),
                                        editable = state.isEditing,
                                        onClick = if (state.isEditing) ({ activeSheet = EditSheet.WEATHER }) else null,
                                    )

                                    InfoChipsTile(
                                        label = "Style",
                                        items = if (state.isEditing) state.style else item.style,
                                        modifier = Modifier.weight(1f),
                                        editable = state.isEditing,
                                        onClick = if (state.isEditing) ({ activeSheet = EditSheet.STYLE }) else null,
                                    )
                                }
                            }
                        }
                    }
                    } // BoxWithConstraints
                }
            }

            if (confirmDelete) {
                AlertDialog(
                    onDismissRequest = { confirmDelete = false },
                    title = { Text("Delete item?") },
                    text = { Text("This action cannot be undone.") },
                    confirmButton = {
                        Button(
                            onClick = { confirmDelete = false; onDelete() },
                            colors = ButtonDefaults.buttonColors(containerColor = cs.error),
                        ) { Text("Delete") }
                    },
                    dismissButton = {
                        TextButton(onClick = { confirmDelete = false }) { Text("Cancel") }
                    },
                )
            }
        }
    }
}
