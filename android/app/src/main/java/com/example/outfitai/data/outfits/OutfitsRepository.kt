package com.example.outfitai.data.outfits

import com.example.outfitai.data.api.WardrobeApi
import com.example.outfitai.data.model.OutfitOutDto
import javax.inject.Inject

class OutfitsRepository @Inject constructor(
    private val api: WardrobeApi
) {
    suspend fun getOutfits(
        season: String? = null,
        occasion: String? = null,
        limit: Int = 6
    ): List<OutfitOutDto> = api.outfits(season = season, occasion = occasion, limit = limit)
}