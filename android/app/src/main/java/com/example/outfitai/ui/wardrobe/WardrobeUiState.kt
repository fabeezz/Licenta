package com.example.outfitai.ui.wardrobe

import com.example.outfitai.data.model.CollectionResponseDto
import com.example.outfitai.data.model.ItemOutDto
import com.example.outfitai.data.model.OutfitSavedDto
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

enum class WardrobeTab { Clothes, Outfits, Collections }

data class WardrobeUiState(
    val isLoading: Boolean = true,
    val items: ImmutableList<ItemOutDto> = persistentListOf(),
    val outfits: ImmutableList<OutfitSavedDto> = persistentListOf(),
    val collections: ImmutableList<CollectionResponseDto> = persistentListOf(),
    val error: String? = null,
    val selectedTab: WardrobeTab = WardrobeTab.Clothes,
    val filterBucket: CategoryBucket? = null,
    val filterColor: String? = null,
    val filterWeather: String? = null,
    val filterStyle: String? = null,
    val searchQuery: String = "",
)
