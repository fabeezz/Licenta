package com.example.outfitai.ui.wardrobe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.outfitai.core.media.mediaUrl
import com.example.outfitai.ui.components.LoomImage
import com.example.outfitai.data.model.CollectionResponseDto
import com.example.outfitai.data.model.ItemMinimalDto
import com.example.outfitai.data.model.OutfitSavedDto
import com.example.outfitai.ui.components.LoomConfirmDialog
import com.example.outfitai.ui.components.LoomInputDialog
import com.example.outfitai.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CollectionsContent(
    state: WardrobeUiState,
    onOutfitClick: (Int) -> Unit,
    onNewCollection: () -> Unit,
    onRenameCollection: (Int, String) -> Unit,
    onDeleteCollection: (Int) -> Unit,
    bottomPadding: Dp,
    modifier: Modifier = Modifier,
) {
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
                modifier = Modifier.padding(Spacing.xl),
            )
        }

        else -> LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(bottom = bottomPadding + Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            modifier = modifier,
        ) {
            item(key = "__add__") {
                AddCollectionTile(onClick = onNewCollection)
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
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
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
                imageVector = Icons.Outlined.Add,
                contentDescription = "New collection",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(Spacing.xxxl),
            )
            Spacer(Modifier.height(Spacing.sm))
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
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(3f / 4f),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.md)) {
                val mosaicItems = remember(collection.outfits) { collection.outfits.take(4).map { it.top } }
                CollectionMosaic(
                    items = mosaicItems,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
                Spacer(Modifier.height(Spacing.sm))
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
                    Icon(Icons.Outlined.MoreVert, contentDescription = "Options")
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text("Rename") },
                        leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                        onClick = { menuExpanded = false; showRenameDialog = true },
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null) },
                        onClick = { menuExpanded = false; showDeleteConfirm = true },
                    )
                }
            }
        }
    }

    if (showRenameDialog) {
        LoomInputDialog(
            title = "Rename collection",
            label = "Name",
            initialValue = collection.name,
            confirmText = "Rename",
            onConfirm = { newName -> onRename(collection.id, newName); showRenameDialog = false },
            onDismiss = { showRenameDialog = false },
        )
    }

    if (showDeleteConfirm) {
        LoomConfirmDialog(
            title = "Delete collection?",
            message = "\"${collection.name}\" will be removed. Outfits inside won't be deleted.",
            confirmText = "Delete",
            destructive = true,
            onConfirm = { onDelete(collection.id); showDeleteConfirm = false },
            onDismiss = { showDeleteConfirm = false },
        )
    }
}

@Composable
private fun CollectionMosaic(items: List<ItemMinimalDto>, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
    ) {
        when (items.size) {
            0 -> Unit
            1 -> MosaicCell(items[0], Modifier.fillMaxSize())
            2 -> Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                MosaicCell(items[0], Modifier.weight(1f).fillMaxHeight())
                MosaicCell(items[1], Modifier.weight(1f).fillMaxHeight())
            }
            3 -> Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                MosaicCell(items[0], Modifier.fillMaxWidth().weight(1f))
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    MosaicCell(items[1], Modifier.weight(1f).fillMaxHeight())
                    MosaicCell(items[2], Modifier.weight(1f).fillMaxHeight())
                }
            }
            else -> Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    MosaicCell(items[0], Modifier.weight(1f).fillMaxHeight())
                    MosaicCell(items[1], Modifier.weight(1f).fillMaxHeight())
                }
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    MosaicCell(items[2], Modifier.weight(1f).fillMaxHeight())
                    MosaicCell(items[3], Modifier.weight(1f).fillMaxHeight())
                }
            }
        }
    }
}

@Composable
private fun MosaicCell(item: ItemMinimalDto, modifier: Modifier = Modifier) {
    LoomImage(
        model = mediaUrl(item.imageNoBgName ?: item.imageOriginalName),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .padding(Spacing.xs / 2),
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
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .padding(horizontal = Spacing.xl)
                .padding(bottom = Spacing.md),
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = Spacing.xl, vertical = Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp),
        ) {
            items(collection.outfits, key = { it.id }) { outfit ->
                FitCard(outfit = outfit, onClick = { onOutfitClick(outfit.id) })
            }
        }
        Spacer(Modifier.height(Spacing.xxl))
    }
}

