package com.example.outfitai.data.outfits

import com.example.outfitai.data.model.OutfitCreateDto
import com.example.outfitai.data.model.OutfitSavedDto

interface OutfitRepository {
    suspend fun create(dto: OutfitCreateDto): Int
    suspend fun list(weather: String? = null, occasion: String? = null): List<OutfitSavedDto>
    suspend fun get(id: Int): OutfitSavedDto
    suspend fun delete(id: Int)
}
