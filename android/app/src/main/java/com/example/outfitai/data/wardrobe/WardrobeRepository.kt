package com.example.outfitai.data.wardrobe

import com.example.outfitai.data.api.WardrobeApi
import com.example.outfitai.data.model.ItemOutDto
import javax.inject.Inject

class WardrobeRepository @Inject constructor(
    private val api: WardrobeApi
) {
    suspend fun listItems(
        limit: Int = 50,
        offset: Int = 0,
    ): List<ItemOutDto> = api.listItems(limit = limit, offset = offset)
}