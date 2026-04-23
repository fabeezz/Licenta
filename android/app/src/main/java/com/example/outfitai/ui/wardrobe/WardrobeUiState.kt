package com.example.outfitai.ui.wardrobe

import com.example.outfitai.data.model.ItemOutDto
import com.example.outfitai.data.model.OutfitSavedDto

enum class WardrobeTab { Pieces, Fits }

data class WardrobeUiState(
    val isLoading: Boolean = true,
    val items: List<ItemOutDto> = emptyList(),
    val outfits: List<OutfitSavedDto> = emptyList(),
    val error: String? = null,
    val selectedTab: WardrobeTab = WardrobeTab.Pieces,
    val filterCategory: String? = null,
    val filterColor: String? = null,
    val filterSeason: String? = null,
    val filterOccasion: String? = null,
)
