package com.example.outfitai.ui.itemdetails

import com.example.outfitai.data.model.ItemOutDto

data class ItemDetailsUiState(
    val isLoading: Boolean = true,
    val item: ItemOutDto? = null,

    val isEditing: Boolean = false,
    val category: String = "",
    val brand: String = "",
    val material: String = "",
    val season: String = "",
    val occasion: String = "",

    val isBusy: Boolean = false,
    val error: String? = null,
)