package com.example.outfitai.ui.wardrobe

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.outfitai.data.model.ItemOutDto
import com.example.outfitai.util.mediaUrl
import androidx.compose.runtime.collectAsState
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.layout.ContentScale
import com.example.outfitai.ui.upload.UploadUiState
import com.example.outfitai.ui.upload.UploadViewModel

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
        contract = ActivityResultContracts.GetContent(),
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
        onAddClick = { pickerLauncher.launch("image/*") },
        onUploadDismiss = { uploadVm.setUri(null) },
        onUpload = uploadVm::upload,
        onBrandChange = uploadVm::setBrand,
        onMaterialChange = uploadVm::setMaterial,
        onSeasonChange = uploadVm::setSeason,
        onOccasionChange = uploadVm::setOccasion,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
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
            TopAppBar(
                title = { Text("My wardrobe") },
                actions = {
                    TextButton(onClick = onOutfitsClick) { Text("Outfits") }
                    TextButton(onClick = onRefresh, enabled = !state.isLoading) { Text("Refresh") }
                    TextButton(onClick = onLogout) { Text("Logout") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "Add item")
            }
        }
    ) { pad ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
        ) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.error != null -> Text(
                    text = state.error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(20.dp)
                )
                state.items.isEmpty() -> Text(
                    text = "Nu ai încă iteme. Adaugă prima haină din butonul de upload (pasul următor).",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(20.dp)
                )
                else -> LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
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
private fun ItemCard(item: ItemOutDto, onClick: () -> Unit) {
    val filename = item.imageNoBgName ?: item.imageOriginalName

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column {
            AsyncImage(
                model = mediaUrl(filename),
                contentDescription = item.category ?: "Item",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )

            Column(Modifier.padding(12.dp)) {
                Text(
                    text = item.category ?: "Unknown",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val subtitle = listOfNotNull(item.brand, item.material, item.season, item.occasion)
                    .take(2)
                    .joinToString(" • ")
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "Worn: ${item.wearCount}",
                    style = MaterialTheme.typography.bodySmall
                )
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
        title = { Text("Adaugă item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                uploadState.selectedUri?.let { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                OutlinedTextField(
                    value = uploadState.brand,
                    onValueChange = onBrandChange,
                    label = { Text("Brand") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = uploadState.material,
                    onValueChange = onMaterialChange,
                    label = { Text("Material") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = uploadState.season,
                    onValueChange = onSeasonChange,
                    label = { Text("Sezon") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = uploadState.occasion,
                    onValueChange = onOccasionChange,
                    label = { Text("Ocazie") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                if (uploadState.error != null) {
                    Text(uploadState.error, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onUpload,
                enabled = !uploadState.isUploading
            ) {
                if (uploadState.isUploading) {
                    CircularProgressIndicator(Modifier.size(24.dp))
                } else {
                    Text("Upload")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Anulare") }
        }
    )
}