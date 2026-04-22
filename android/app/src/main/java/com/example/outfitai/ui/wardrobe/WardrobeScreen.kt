package com.example.outfitai.ui.wardrobe

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import com.example.outfitai.data.model.ItemConstants
import com.example.outfitai.data.model.ItemOutDto
import com.example.outfitai.ui.components.DropdownSelector
import com.example.outfitai.ui.upload.UploadUiState
import com.example.outfitai.ui.upload.UploadViewModel
import com.example.outfitai.util.mediaUrl

@Composable
fun WardrobeRoute(
    onLogout: () -> Unit,
    onItemClick: (Int) -> Unit,
    onOutfitsClick: () -> Unit,
    vm: WardrobeViewModel = hiltViewModel(),
    uploadVm: UploadViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()
    val uploadState by uploadVm.state.collectAsState()

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? -> uploadVm.setUri(uri) }
    )

    LaunchedEffect(uploadState.done) {
        if (uploadState.done) {
            vm.refresh()
            uploadVm.resetAfterDone()
        }
    }

    WardrobeScreen(
        state = state,
        uploadState = uploadState,
        onRefresh = vm::refresh,
        onLogout = onLogout,
        onItemClick = onItemClick,
        onOutfitsClick = onOutfitsClick,
        onAddClick = { 
            pickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        },
        onUploadDismiss = { uploadVm.setUri(null) },
        onUpload = uploadVm::upload,
        onBrandChange = uploadVm::setBrand,
        onMaterialChange = uploadVm::setMaterial,
        onSeasonChange = uploadVm::setSeason,
        onOccasionChange = uploadVm::setOccasion,
    )
}

@Composable
fun WardrobeScreen(
    state: WardrobeUiState,
    uploadState: UploadUiState,
    onRefresh: () -> Unit,
    onLogout: () -> Unit,
    onItemClick: (Int) -> Unit,
    onOutfitsClick: () -> Unit,
    onAddClick: () -> Unit,
    onUploadDismiss: () -> Unit,
    onUpload: () -> Unit,
    onBrandChange: (String) -> Unit,
    onMaterialChange: (String) -> Unit,
    onSeasonChange: (String) -> Unit,
    onOccasionChange: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            WardrobeTopBar(
                isLoading = state.isLoading,
                onOutfitsClick = onOutfitsClick,
                onRefresh = onRefresh,
                onLogout = onLogout,
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(50),
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Item", style = MaterialTheme.typography.labelLarge) },
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(horizontal = 20.dp),
        ) {
            // Header
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Wardrobe",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "${state.items.size} items curated for you",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(32.dp))

            // Content
            when {
                state.isLoading -> Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
                state.error != null -> Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = state.error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(20.dp),
                    )
                }
                state.items.isEmpty() -> Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No items yet. Tap Add Item to upload your first piece.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(20.dp),
                    )
                }
                else -> LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    items(state.items, key = { it.id }) { item ->
                        ItemCard(item, onClick = { onItemClick(item.id) })
                    }
                }
            }
        }

        if (uploadState.selectedUri != null) {
            UploadDialog(
                uploadState = uploadState,
                onDismiss = onUploadDismiss,
                onUpload = onUpload,
                onBrandChange = onBrandChange,
                onMaterialChange = onMaterialChange,
                onSeasonChange = onSeasonChange,
                onOccasionChange = onOccasionChange,
            )
        }
    }
}

@Composable
private fun WardrobeTopBar(
    isLoading: Boolean,
    onOutfitsClick: () -> Unit,
    onRefresh: () -> Unit,
    onLogout: () -> Unit,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.8f))
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerLow),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = "OutfitAI",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onOutfitsClick) {
                    Icon(
                        imageVector = Icons.Default.Checkroom,
                        contentDescription = "Outfits",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
                IconButton(onClick = onRefresh, enabled = !isLoading) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
                IconButton(onClick = onLogout) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Logout",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    }
}

@Composable
private fun ItemCard(item: ItemOutDto, onClick: () -> Unit) {
    val filename = item.imageNoBgName ?: item.imageOriginalName

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
    ) {
        Column {
            // Image tile
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 5f)
                    .padding(12.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(Color(0xFFF9F9F9)),
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = mediaUrl(filename),
                    contentDescription = item.category ?: "Item",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    contentScale = ContentScale.Fit,
                )
            }

            // Details
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    if (!item.brand.isNullOrBlank()) {
                        Text(
                            text = item.brand.uppercase(),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f).padding(end = 4.dp),
                        )
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "${item.wearCount}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Text(
                    text = item.category ?: "Unknown",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                val subtitle = listOfNotNull(item.material, item.category).joinToString(" • ")
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun UploadDialog(
    uploadState: UploadUiState,
    onDismiss: () -> Unit,
    onUpload: () -> Unit,
    onBrandChange: (String) -> Unit,
    onMaterialChange: (String) -> Unit,
    onSeasonChange: (String) -> Unit,
    onOccasionChange: (String) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                uploadState.selectedUri?.let { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentScale = ContentScale.Fit,
                    )
                }
                OutlinedTextField(
                    value = uploadState.brand,
                    onValueChange = onBrandChange,
                    label = { Text("Brand") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                DropdownSelector(
                    label = "Material",
                    selectedOption = uploadState.material,
                    options = ItemConstants.MATERIALS,
                    onOptionSelected = onMaterialChange,
                )
                DropdownSelector(
                    label = "Season",
                    selectedOption = uploadState.season,
                    options = ItemConstants.SEASONS,
                    onOptionSelected = onSeasonChange,
                )
                DropdownSelector(
                    label = "Occasion",
                    selectedOption = uploadState.occasion,
                    options = ItemConstants.OCCASIONS,
                    onOptionSelected = onOccasionChange,
                )
                if (uploadState.error != null) {
                    Text(uploadState.error, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onUpload,
                enabled = !uploadState.isUploading,
            ) {
                if (uploadState.isUploading) {
                    CircularProgressIndicator(Modifier.size(24.dp))
                } else {
                    Text("Upload")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
