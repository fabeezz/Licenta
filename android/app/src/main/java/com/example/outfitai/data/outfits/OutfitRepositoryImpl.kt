package com.example.outfitai.data.outfits

import com.example.outfitai.data.api.OutfitApi
import com.example.outfitai.data.model.OutfitCreateDto
import com.example.outfitai.data.model.OutfitSavedDto
import javax.inject.Inject

class OutfitRepositoryImpl @Inject constructor(
    private val api: OutfitApi,
) : OutfitRepository {

    override suspend fun create(dto: OutfitCreateDto): Int = api.create(dto).id

    override suspend fun list(season: String?, occasion: String?): List<OutfitSavedDto> =
        api.list(season = season, occasion = occasion)

    override suspend fun get(id: Int): OutfitSavedDto = api.get(id)

    override suspend fun delete(id: Int) { api.delete(id) }
}
