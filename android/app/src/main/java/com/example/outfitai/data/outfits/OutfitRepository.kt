package com.example.outfitai.data.outfits

import com.example.outfitai.data.api.OutfitApi
import com.example.outfitai.data.model.OutfitCreateDto
import javax.inject.Inject

class OutfitRepository @Inject constructor(
    private val api: OutfitApi,
) {
    suspend fun create(dto: OutfitCreateDto): Int = api.create(dto).id
}
