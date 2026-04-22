package com.example.outfitai.ui.wardrobe

import com.example.outfitai.data.model.ItemOutDto

data class WardrobeUiState(
    val isLoading: Boolean = true,
    val items: List<ItemOutDto> = emptyList(),
    val error: String? = null,
    val filterCategory: String? = null,
    val filterColor: String? = null,
    val filterSeason: String? = null,
    val filterOccasion: String? = null,
)
