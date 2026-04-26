package com.example.outfitai.ui.upload

import android.net.Uri

data class UploadUiState(
    val selectedUri: Uri? = null,
    val brand: String = "",
    val material: String = "",
    val weather: List<String> = emptyList(),
    val occasion: String = "",
    val isUploading: Boolean = false,
    val error: String? = null,
    val done: Boolean = false,
)