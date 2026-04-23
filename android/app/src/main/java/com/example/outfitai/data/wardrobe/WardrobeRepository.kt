package com.example.outfitai.data.wardrobe

import com.example.outfitai.data.api.OutfitApi
import com.example.outfitai.data.api.WardrobeApi
import com.example.outfitai.data.model.ItemOutDto
import com.example.outfitai.data.model.OutfitSavedDto
import javax.inject.Inject

class WardrobeRepository @Inject constructor(
    private val api: WardrobeApi,
    private val outfitApi: OutfitApi,
) {
    suspend fun listItems(
        category: String? = null,
        dominantColor: String? = null,
        season: String? = null,
        occasion: String? = null,
        limit: Int = 50,
        offset: Int = 0,
    ): List<ItemOutDto> = api.listItems(
        category = category,
        dominantColor = dominantColor,
        season = season,
        occasion = occasion,
        limit = limit,
        offset = offset,
    )

    suspend fun listOutfits(
        season: String? = null,
        occasion: String? = null,
    ): List<OutfitSavedDto> = outfitApi.list(
        season = season,
        occasion = occasion,
    )
}
