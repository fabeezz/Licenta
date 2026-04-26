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
        onWeather = vm::setWeather,
        onOccasion = vm::setOccasion,
        onDominantColor = vm::setDominantColor,
        onAccentColor = vm::toggleAccentColor,
    )
}

private fun extractColorList(colorTags: Map<String, JsonElement>?, key: String): List<String> {
    val arr = (colorTags?.get(key) as? JsonArray) ?: return emptyList()
    return arr.mapNotNull { runCatching { it.jsonPrimitive.content }.getOrNull() }
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
    onWeather: (List<String>) -> Unit,
    onOccasion: (String) -> Unit,
    onDominantColor: (String) -> Unit,
    onAccentColor: (String) -> Unit,
) {
    var confirmDelete by remember { mutableStateOf(false) }
    var showDominantDialog by remember { mutableStateOf(false) }
    var showAccentDialog by remember { mutableStateOf(false) }
    val cs = MaterialTheme.colorScheme
    val shapes = MaterialTheme.shapes

    if (showDominantDialog) {
        ColorSelectionDialog(
            title = "Dominant Color",
            currentSelected = state.dominantColors,
            onDismiss = { showDominantDialog = false },
            onSelect = {
                onDominantColor(it)
                showDominantDialog = false
            },
            isMultiple = false
        )
    }

    if (showAccentDialog) {
        ColorSelectionDialog(
            title = "Accent Colors",
            currentSelected = state.accentColors,
            onDismiss = { showAccentDialog = false },
            onSelect = onAccentColor,
            isMultiple = true
        )
    }

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

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 48.dp)
                    ) {
                        Spacer(Modifier.height(16.dp))

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

                        Text(
                            text = item.category?.capitalizeFirst() ?: "Item",
                            style = MaterialTheme.typography.headlineMedium,
                            color = cs.onSurface
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "${item.brand ?: "—"} • ${item.category?.capitalizeFirst() ?: "—"}",
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

                        InfoCard(title = "Classification", modifier = Modifier.fillMaxWidth()) {
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
                                InfoRow(label = "Category", value = item.category?.capitalizeFirst() ?: "—")
                                Spacer(Modifier.height(12.dp))
                                InfoRow(label = "Brand", value = item.brand ?: "—")
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        InfoCard(title = "Attributes", modifier = Modifier.fillMaxWidth()) {
                            if (state.isEditing) {
                                DropdownSelector(
                                    label = "Material",
                                    selectedOption = state.material,
                                    options = ItemConstants.MATERIALS,
                                    onOptionSelected = onMaterial
                                )
                                Spacer(Modifier.height(8.dp))
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Weather",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = cs.onSurfaceVariant,
                                )
                                Spacer(Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    ItemConstants.WEATHER_TAGS.forEach { tag ->
                                        val selected = tag in state.weather
                                        FilterChip(
                                            selected = selected,
                                            onClick = {
                                                val updated = if (selected) state.weather - tag
                                                           else state.weather + tag
                                                onWeather(updated)
                                            },
                                            label = { Text(tag.capitalizeFirst()) },
                                        )
                                    }
                                }
                            } else {
                                InfoRow(label = "Material", value = item.material?.capitalizeFirst() ?: "—")
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    text = "Weather",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = cs.onSurfaceVariant
                                )
                                Spacer(Modifier.height(6.dp))
                                if (item.weather.isNotEmpty()) {
                                    WeatherChips(tags = item.weather)
                                } else {
                                    Text("—", style = MaterialTheme.typography.bodyMedium, color = cs.onSurface)
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        InfoCard(title = "Color Profile", modifier = Modifier.fillMaxWidth()) {
                            val dominant = if (state.isEditing) state.dominantColors else extractColorList(item.colorTags, "dominant")
                            val accent = if (state.isEditing) state.accentColors else extractColorList(item.colorTags, "accent")

                            if (!state.isEditing && dominant.isEmpty() && accent.isEmpty()) {
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
                                    if (state.isEditing && dominant.isEmpty()) {
                                        ColorSwatch(
                                            name = "Pick",
                                            role = "Dominant",
                                            size = 64.dp,
                                            color = cs.surfaceContainerHigh,
                                            clickable = true,
                                            onClick = { showDominantDialog = true }
                                        )
                                    } else {
                                        dominant.take(1).forEach { name ->
                                            ColorSwatch(
                                                name = name,
                                                role = "Dominant",
                                                size = 64.dp,
                                                color = colorNameToComposeColor(name, cs.surfaceVariant),
                                                clickable = state.isEditing,
                                                onClick = { showDominantDialog = true }
                                            )
                                        }
                                    }

                                    if (state.isEditing && accent.isEmpty()) {
                                        ColorSwatch(
                                            name = "Add",
                                            role = "Accent",
                                            size = 48.dp,
                                            color = cs.surfaceContainerHigh,
                                            clickable = true,
                                            onClick = { showAccentDialog = true }
                                        )
                                    } else {
                                        accent.take(3).forEach { name ->
                                            ColorSwatch(
                                                name = name,
                                                role = "Accent",
                                                size = 48.dp,
                                                color = colorNameToComposeColor(name, cs.surfaceVariant),
                                                clickable = state.isEditing,
                                                onClick = { showAccentDialog = true }
                                            )
                                        }
                                        if (state.isEditing && accent.isNotEmpty() && accent.size < 5) {
                                            IconButton(
                                                onClick = { showAccentDialog = true },
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .background(cs.surfaceContainerHigh)
                                            ) {
                                                Icon(Icons.Default.Add, contentDescription = "Add accent", modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        InfoCard(title = "Occasion", modifier = Modifier.fillMaxWidth()) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val currentOccasion = if (state.isEditing) state.occasion else item.occasion
                                ItemConstants.OCCASIONS.forEach { option ->
                                    OccasionChip(
                                        label = option.capitalizeFirst(),
                                        selected = currentOccasion == option,
                                        clickable = state.isEditing,
                                        onClick = { onOccasion(option) }
                                    )
                                }
                            }
                        }

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
                AlertDialog(
                    onDismissRequest = { confirmDelete = false },
                    title = { Text("Delete item?") },
                    text = { Text("This action cannot be undone.") },
                    confirmButton = {
                        Button(
                            onClick = { confirmDelete = false; onDelete() },
                            colors = ButtonDefaults.buttonColors(containerColor = cs.error)
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
