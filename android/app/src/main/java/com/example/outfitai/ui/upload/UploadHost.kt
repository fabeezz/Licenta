package com.example.outfitai.ui.upload

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel

class UploadLauncher(val launch: () -> Unit)

@Composable
fun rememberUploadLauncher(
    onDone: () -> Unit = {},
    vm: UploadViewModel = hiltViewModel(),
): UploadLauncher {
    val state by vm.state.collectAsState()

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? -> vm.setUri(uri) },
    )

    LaunchedEffect(state.done) {
        if (state.done) {
            onDone()
            vm.resetAfterDone()
        }
    }

    if (state.selectedUri != null) {
        UploadDialog(
            uploadState = state,
            onDismiss = { vm.setUri(null) },
            onUpload = vm::upload,
            onBrandChange = vm::setBrand,
            onMaterialChange = vm::setMaterial,
            onWeatherChange = vm::setWeather,
            onStyleChange = vm::setStyle,
        )
    }

    return remember(pickerLauncher) {
        UploadLauncher {
            pickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
    }
}
