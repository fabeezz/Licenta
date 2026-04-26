package com.example.outfitai.ui.wardrobe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
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
import coil.compose.AsyncImage
import com.example.outfitai.core.media.mediaUrl
import com.example.outfitai.data.model.CollectionResponseDto
import com.example.outfitai.data.model.ItemMinimalDto
import com.example.outfitai.data.model.OutfitSavedDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CollectionsContent(
    state: WardrobeUiState,
    onOutfitClick: (Int) -> Unit,
    onCreateCollection: (String, List<Int>) -> Unit,
    onRenameCollection: (Int, String) -> Unit,
    onDeleteCollection: (Int) -> Unit,
    bottomPadding: Dp,
    modifier: Modifier = Modifier,
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var detailCollection by remember { mutableStateOf<CollectionResponseDto?>(null) }

    when {
        state.isLoading && state.collections.isEmpty() -> Box(
            modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }

        state.error != null && state.collections.isEmpty() -> Box(
            modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = state.error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(20.dp),
            )
        }

        else -> LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(bottom = bottomPadding + 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = modifier,
        ) {
            item(key = "__add__") {
                AddCollectionTile(onClick = { showCreateDialog = true })
            }
            items(state.collections, key = { it.id }) { collection ->
                CollectionCard(
                    collection = collection,
                    onTap = { detailCollection = collection },
                    onRename = onRenameCollection,
                    onDelete = onDeleteCollection,
                )
            }
        }
    }

    if (showCreateDialog) {
        CreateCollectionDialog(
            outfits = state.outfits,
            onCreate = { name, ids ->
                onCreateCollection(name, ids)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false },
        )
    }

    detailCollection?.let { col ->
        CollectionDetailSheet(
            collection = col,
            onOutfitClick = onOutfitClick,
            onDismiss = { detailCollection = null },
        )
    }
}

@Composable
private fun AddCollectionTile(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFF9F9F9),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(3f / 4f),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "New collection",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(40.dp),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "New collection",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CollectionCard(
    collection: CollectionResponseDto,
    onTap: () -> Unit,
    onRename: (Int, String) -> Unit,
    onDelete: (Int) -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Surface(
        onClick = onTap,
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFF9F9F9),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(3f / 4f),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                CollectionMosaic(
                    items = collection.outfits.take(4).map { it.top },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = collection.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                )
                Text(
                    text = "${collection.outfits.size} outfit${if (collection.outfits.size != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Box(modifier = Modifier.align(Alignment.TopEnd)) {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options")
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text("Rename") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = { menuExpanded = false; showRenameDialog = true },
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                        onClick = { menuExpanded = false; showDeleteConfirm = true },
                    )
                }
            }
        }
    }

    if (showRenameDialog) {
        RenameCollectionDialog(
            current = collection.name,
            onRename = { newName -> onRename(collection.id, newName); showRenameDialog = false },
            onDismiss = { showRenameDialog = false },
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete collection?") },
            text = { Text("\"${collection.name}\" will be removed. Outfits inside won't be deleted.") },
            confirmButton = {
                TextButton(onClick = { onDelete(collection.id); showDeleteConfirm = false }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun CollectionMosaic(items: List<ItemMinimalDto>, modifier: Modifier = Modifier) {
    val gap = 4.dp
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.5f)),
    ) {
        when (items.size) {
            0 -> Unit
            1 -> MosaicCell(items[0], Modifier.fillMaxSize())
            2 -> Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(gap)) {
                MosaicCell(items[0], Modifier.weight(1f).fillMaxHeight())
                MosaicCell(items[1], Modifier.weight(1f).fillMaxHeight())
            }
            3 -> Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(gap)) {
                MosaicCell(items[0], Modifier.fillMaxWidth().weight(1f))
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(gap)) {
                    MosaicCell(items[1], Modifier.weight(1f).fillMaxHeight())
                    MosaicCell(items[2], Modifier.weight(1f).fillMaxHeight())
                }
            }
            else -> Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(gap)) {
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(gap)) {
                    MosaicCell(items[0], Modifier.weight(1f).fillMaxHeight())
                    MosaicCell(items[1], Modifier.weight(1f).fillMaxHeight())
                }
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(gap)) {
                    MosaicCell(items[2], Modifier.weight(1f).fillMaxHeight())
                    MosaicCell(items[3], Modifier.weight(1f).fillMaxHeight())
                }
            }
        }
    }
}

@Composable
private fun MosaicCell(item: ItemMinimalDto, modifier: Modifier = Modifier) {
    AsyncImage(
        model = mediaUrl(item.imageNoBgName ?: item.imageOriginalName),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .padding(2.dp),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CollectionDetailSheet(
    collection: CollectionResponseDto,
    onOutfitClick: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(
            text = collection.name,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 12.dp),
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp),
        ) {
            items(collection.outfits, key = { it.id }) { outfit ->
                FitCard(outfit = outfit, onClick = { onOutfitClick(outfit.id) })
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun CreateCollectionDialog(
    outfits: List<OutfitSavedDto>,
    onCreate: (String, List<Int>) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    val selected = remember { mutableStateOf(setOf<Int>()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New collection") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                if (outfits.isEmpty()) {
                    Text(
                        "Save some outfits first to add them to a collection.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Text("Select outfits", style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.height(4.dp))
                    LazyColumn(modifier = Modifier.heightIn(max = 260.dp)) {
                        items(outfits, key = { it.id }) { outfit ->
                            val checked = outfit.id in selected.value
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Checkbox(
                                    checked = checked,
                                    onCheckedChange = { on ->
                                        selected.value = if (on)
                                            selected.value + outfit.id
                                        else
                                            selected.value - outfit.id
                                    },
                                )
                                Text(outfit.name, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(name.trim(), selected.value.toList()) },
                enabled = name.isNotBlank() && selected.value.isNotEmpty(),
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
private fun RenameCollectionDialog(
    current: String,
    onRename: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(current) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename collection") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onRename(name.trim()) },
                enabled = name.isNotBlank() && name.trim() != current,
            ) {
                Text("Rename")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
