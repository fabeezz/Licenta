package com.example.outfitai.data.outfits

import com.example.outfitai.data.model.OutfitCreateDto
import com.example.outfitai.data.model.OutfitSavedDto
import com.example.outfitai.data.model.OutfitSuggestRequest
import com.example.outfitai.data.model.OutfitSuggestResponse

interface OutfitRepository {
    suspend fun create(dto: OutfitCreateDto): Int
    suspend fun list(weather: String? = null, style: String? = null): List<OutfitSavedDto>
    suspend fun get(id: Int): OutfitSavedDto
    suspend fun delete(id: Int)
    suspend fun suggest(request: OutfitSuggestRequest): OutfitSuggestResponse
}
