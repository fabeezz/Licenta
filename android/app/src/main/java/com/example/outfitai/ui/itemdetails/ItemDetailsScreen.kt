package com.example.outfitai.ui.itemdetails

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.outfitai.util.mediaUrl

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

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Item details") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                },
                actions = {
                    TextButton(onClick = onRefresh, enabled = !state.isLoading && !state.isBusy) { Text("Refresh") }
                }
            )
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
                    state.error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
                state.item == null -> Text("Item not found", modifier = Modifier.align(Alignment.Center))
                else -> {
                    val item = state.item
                    val filename = item.imageNoBgName ?: item.imageOriginalName

                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                            val imageHeight = maxHeight / 2  // jumătate din spațiul disponibil
                            AsyncImage(
                                model = mediaUrl(filename),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(imageHeight)
                            )
                        }

                        Text("Worn: ${item.wearCount}")

                        if (state.isEditing) {
                            OutlinedTextField(state.category, onCategory, label = { Text("Category") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(state.brand, onBrand, label = { Text("Brand") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(state.material, onMaterial, label = { Text("Material") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(state.season, onSeason, label = { Text("Season") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(state.occasion, onOccasion, label = { Text("Occasion") }, modifier = Modifier.fillMaxWidth())
                        } else {
                            Text("Category: ${item.category ?: "-"}")
                            Text("Brand: ${item.brand ?: "-"}")
                            Text("Material: ${item.material ?: "-"}")
                            Text("Season: ${item.season ?: "-"}")
                            Text("Occasion: ${item.occasion ?: "-"}")
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(onClick = onWear, enabled = !state.isBusy) { Text("Mark worn") }

                            OutlinedButton(onClick = onToggleEdit, enabled = !state.isBusy) {
                                Text(if (state.isEditing) "Cancel" else "Edit")
                            }

                            if (state.isEditing) {
                                Button(onClick = onSave, enabled = !state.isBusy) { Text("Save") }
                            }
                        }

                        OutlinedButton(
                            onClick = { confirmDelete = true },
                            enabled = !state.isBusy,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) { Text("Delete") }
                    }
                }
            }

            if (confirmDelete) {
                AlertDialog(
                    onDismissRequest = { confirmDelete = false },
                    title = { Text("Delete item?") },
                    text = { Text("Acțiunea este ireversibilă.") },
                    confirmButton = {
                        Button(onClick = { confirmDelete = false; onDelete() }) { Text("Delete") }
                    },
                    dismissButton = {
                        TextButton(onClick = { confirmDelete = false }) { Text("Cancel") }
                    }
                )
            }
        }
    }
}