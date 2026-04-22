package com.example.outfitai.ui.outfits

import com.example.outfitai.data.model.ItemOutDto

data class SlotItems(
    val items: List<ItemOutDto> = emptyList(),
    val index: Int = 0,
) {
    val current: ItemOutDto? get() = items.getOrNull(index)
}

data class OutfitStudioUiState(
    val isLoading: Boolean = true,
    val username: String = "",
    val includeOuter: Boolean = false,
    val top: SlotItems = SlotItems(),
    val bottom: SlotItems = SlotItems(),
    val outer: SlotItems = SlotItems(),
    val shoes: SlotItems = SlotItems(),
    val isSaving: Boolean = false,
    val savedOutfitId: Int? = null,
    val snackbarMessage: String? = null,
    val error: String? = null,
)
