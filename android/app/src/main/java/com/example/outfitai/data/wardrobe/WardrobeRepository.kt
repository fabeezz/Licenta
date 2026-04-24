package com.example.outfitai.data.wardrobe

import com.example.outfitai.data.model.ItemOutDto
import com.example.outfitai.data.model.OutfitSavedDto

interface WardrobeRepository {
    suspend fun listItems(
        category: String? = null,
        brand: String? = null,
        dominantColor: String? = null,
        colors: List<String>? = null,
        material: String? = null,
        season: String? = null,
        occasion: String? = null,
        sortBy: String = "created_at",
        sortDir: String = "desc",
        limit: Int = 50,
        offset: Int = 0,
    ): List<ItemOutDto>

    suspend fun listOutfits(
        season: String? = null,
        occasion: String? = null,
    ): List<OutfitSavedDto>
}
