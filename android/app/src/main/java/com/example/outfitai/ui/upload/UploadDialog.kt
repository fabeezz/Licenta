package com.example.outfitai.ui.upload

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.outfitai.data.model.ItemConstants
import com.example.outfitai.ui.components.LoomButton
import com.example.outfitai.ui.components.LoomDropdownSelector
import com.example.outfitai.ui.components.LoomTextField
import com.example.outfitai.ui.theme.Spacing

@Composable
fun UploadDialog(
    uploadState: UploadUiState,
    onDismiss: () -> Unit,
    onUpload: () -> Unit,
    onBrandChange: (String) -> Unit,
    onMaterialChange: (String) -> Unit,
    onWeatherChange: (List<String>) -> Unit,
    onStyleChange: (List<String>) -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    tonalElevation = 0.dp,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.sm, vertical = Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                        Text(
                            text = "Add New Item",
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(Modifier.size(48.dp))
                    }
                }
            },
            bottomBar = {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    tonalElevation = 0.dp,
                    modifier = Modifier.imePadding(),
                ) {
                    LoomButton(
                        text = "Upload to Wardrobe",
                        onClick = onUpload,
                        isLoading = uploadState.isUploading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = Spacing.xl, vertical = Spacing.lg),
                    )
                }
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Spacer(Modifier.height(8.dp))

                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 5f),
                ) {
                    AsyncImage(
                        model = uploadState.selectedUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                LoomTextField(
                    label = "Brand",
                    value = uploadState.brand,
                    onValueChange = onBrandChange,
                    modifier = Modifier.fillMaxWidth(),
                )

                LoomDropdownSelector(
                    label = "Material",
                    selectedOption = uploadState.material,
                    options = ItemConstants.MATERIALS,
                    onOptionSelected = onMaterialChange,
                    modifier = Modifier.fillMaxWidth(),
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Weather (optional)",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ItemConstants.WEATHER_TAGS.forEach { tag ->
                            val selected = tag in uploadState.weather
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    val updated = if (selected) uploadState.weather - tag
                                               else uploadState.weather + tag
                                    onWeatherChange(updated)
                                },
                                label = { Text(tag.replaceFirstChar { it.uppercase() }) },
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Style (optional)",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ItemConstants.STYLES.forEach { tag ->
                            val selected = tag in uploadState.style
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    val updated = if (selected) uploadState.style - tag
                                               else uploadState.style + tag
                                    onStyleChange(updated)
                                },
                                label = { Text(tag.replaceFirstChar { it.uppercase() }) },
                            )
                        }
                    }
                }

                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Row(
                        modifier = Modifier.padding(Spacing.lg),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            "AI will automatically figure out the details of your product if no info is provided.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                if (uploadState.error != null) {
                    Text(
                        uploadState.error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
