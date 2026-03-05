package com.example.outfitai.ui.outfits

import com.example.outfitai.data.model.OutfitOutDto

data class OutfitsUiState(
    val isLoading: Boolean = true,
    val outfits: List<OutfitOutDto> = emptyList(),
    val season: String = "",
    val occasion: String = "",
    val error: String? = null,
)