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
import com.example.outfitai.ui.components.DropdownSelector
import com.example.outfitai.core.ui.color.colorNameToComposeColor
import com.example.outfitai.core.media.mediaUrl
import com.example.outfitai.util.capitalizeFirst
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
        onSave = { vm.save(onItemChanged) },
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
    var showOverflow by remember { mutableStateOf(false) }
    var showDominantDialog by remember { mutableStateOf(false) }
    var showAccentDialog by remember { mutableStateOf(false) }
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
                title = {
                    Text(
                        text = state.item?.category?.capitalizeFirst() ?: "Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = cs.onSurface,
                    )
                },
                actions = {
                    Box {
                        IconButton(onClick = { showOverflow = true }, enabled = !state.isLoading) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options", tint = cs.onSurface)
                        }
                        DropdownMenu(
                            expanded = showOverflow,
                            onDismissRequest = { showOverflow = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("Delete item", color = cs.error) },
                                leadingIcon = {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = cs.error)
                                },
                                onClick = { showOverflow = false; confirmDelete = true },
                                enabled = !state.isBusy,
                            )
                        }
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

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 24.dp),
                    ) {
                        Spacer(Modifier.height(8.dp))

                        // Hero
                        Surface(
                            color = cs.surfaceContainerLowest,
                            shape = shapes.extraLarge,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(220.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                AsyncImage(
                                    model = mediaUrl(filename),
                                    contentDescription = item.category,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxWidth().heightIn(max = 180.dp),
                                )
                                Column(
                                    modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    FilledIconButton(
                                        onClick = onToggleEdit,
                                        shape = CircleShape,
                                        colors = IconButtonDefaults.filledIconButtonColors(
                                            containerColor = cs.surfaceContainerLowest.copy(alpha = 0.92f),
                                            contentColor = cs.onSurface,
                                        ),
                                    ) {
                                        Icon(
                                            imageVector = if (state.isEditing) Icons.Default.Close else Icons.Default.Edit,
                                            contentDescription = if (state.isEditing) "Cancel edit" else "Edit",
                                        )
                                    }
                                    if (!state.isEditing) {
                                        FilledIconButton(
                                            onClick = onWear,
                                            enabled = !state.isBusy,
                                            shape = CircleShape,
                                            colors = IconButtonDefaults.filledIconButtonColors(
                                                containerColor = cs.surfaceContainerLowest.copy(alpha = 0.92f),
                                                contentColor = cs.onSurface,
                                            ),
                                        ) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = "Mark worn")
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Title block
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = item.category?.capitalizeFirst() ?: "Item",
                                    style = MaterialTheme.typography.displayLarge,
                                    color = cs.onSurface,
                                )
                                if (!item.brand.isNullOrBlank()) {
                                    Text(
                                        text = item.brand,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = cs.onSurfaceVariant,
                                    )
                                }
                            }
                            Column(
                                horizontalAlignment = Alignment.End,
                                modifier = Modifier.padding(bottom = 4.dp),
                            ) {
                                Text(
                                    text = "${item.wearCount}×",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = cs.onSurfaceVariant,
                                )
                                Text(
                                    text = relativeTime(item.lastWornAt),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = cs.onSurfaceVariant,
                                )
                            }
                        }

                        // Color strip
                        if (dominant.isNotEmpty() || accent.isNotEmpty() || state.isEditing) {
                            Spacer(Modifier.height(16.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
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
                        }

                        Spacer(Modifier.height(16.dp))

                        // Grouped list card
                        GroupedListCard(Modifier.fillMaxWidth()) {
                            if (state.isEditing) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    DropdownSelector(
                                        label = "Category",
                                        selectedOption = state.category,
                                        options = ItemConstants.CATEGORIES,
                                        onOptionSelected = onCategory,
                                    )
                                    DropdownSelector(
                                        label = "Material",
                                        selectedOption = state.material,
                                        options = ItemConstants.MATERIALS,
                                        onOptionSelected = onMaterial,
                                    )
                                    OutlinedTextField(
                                        value = state.brand,
                                        onValueChange = onBrand,
                                        label = { Text("Brand") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = shapes.large,
                                        singleLine = true,
                                    )
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = "WEATHER",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = cs.onSurfaceVariant,
                                        )
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp),
                                        ) {
                                            ItemConstants.WEATHER_TAGS.forEach { tag ->
                                                val selected = tag in state.weather
                                                OccasionChip(
                                                    label = tag.capitalizeFirst(),
                                                    selected = selected,
                                                    clickable = true,
                                                    onClick = {
                                                        onWeather(
                                                            if (selected) state.weather - tag
                                                            else state.weather + tag
                                                        )
                                                    },
                                                )
                                            }
                                        }
                                    }
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = "STYLE",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = cs.onSurfaceVariant,
                                        )
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp),
                                        ) {
                                            ItemConstants.STYLES.forEach { option ->
                                                val sel = option in state.style
                                                OccasionChip(
                                                    label = option.capitalizeFirst(),
                                                    selected = sel,
                                                    clickable = true,
                                                    onClick = {
                                                        val updated = state.style.toMutableList().apply {
                                                            if (sel) remove(option) else add(option)
                                                        }
                                                        onStyle(updated)
                                                    },
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                GroupedRow("Material") {
                                    Text(
                                        text = item.material?.capitalizeFirst() ?: "—",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = cs.onSurface,
                                    )
                                }
                                HorizontalDivider(
                                    color = cs.outlineVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.padding(start = 16.dp),
                                )
                                GroupedRow("Brand") {
                                    Text(
                                        text = item.brand ?: "—",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = cs.onSurface,
                                    )
                                }
                                HorizontalDivider(
                                    color = cs.outlineVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.padding(start = 16.dp),
                                )
                                GroupedRow("Category") {
                                    Text(
                                        text = item.category?.capitalizeFirst() ?: "—",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = cs.onSurface,
                                    )
                                }
                                HorizontalDivider(
                                    color = cs.outlineVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.padding(start = 16.dp),
                                )
                                GroupedChipSection("Weather") {
                                    if (item.weather.isEmpty()) {
                                        Text(
                                            text = "—",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = cs.onSurface,
                                        )
                                    } else {
                                        WeatherChips(item.weather)
                                    }
                                }
                                HorizontalDivider(
                                    color = cs.outlineVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.padding(start = 16.dp),
                                )
                                GroupedChipSection("Style") {
                                    if (item.style.isEmpty()) {
                                        Text(
                                            text = "—",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = cs.onSurface,
                                        )
                                    } else {
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            item.style.forEach { s ->
                                                OccasionChip(
                                                    label = s.capitalizeFirst(),
                                                    selected = true,
                                                    clickable = false,
                                                    onClick = {},
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
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
