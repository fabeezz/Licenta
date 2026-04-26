package com.example.outfitai.data.wardrobe

import com.example.outfitai.data.api.ItemApi
import com.example.outfitai.data.api.OutfitApi
import com.example.outfitai.data.model.ItemOutDto
import com.example.outfitai.data.model.OutfitSavedDto
import javax.inject.Inject

class WardrobeRepositoryImpl @Inject constructor(
    private val api: ItemApi,
    private val outfitApi: OutfitApi,
) : WardrobeRepository {

    override suspend fun listItems(
        category: String?,
        brand: String?,
        dominantColor: String?,
        colors: List<String>?,
        material: String?,
        weather: String?,
        occasion: String?,
        sortBy: String,
        sortDir: String,
        limit: Int,
        offset: Int,
    ): List<ItemOutDto> = api.listItems(
        category = category,
        brand = brand,
        dominantColor = dominantColor,
        colors = colors,
        material = material,
        weather = weather,
        occasion = occasion,
        sortBy = sortBy,
        sortDir = sortDir,
        limit = limit,
        offset = offset,
    )

    override suspend fun listOutfits(
        weather: String?,
        occasion: String?,
    ): List<OutfitSavedDto> = outfitApi.list(
        weather = weather,
        occasion = occasion,
    )
}
