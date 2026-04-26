package com.example.outfitai.data.collections

import com.example.outfitai.data.api.CollectionApi
import com.example.outfitai.data.model.CollectionCreateDto
import com.example.outfitai.data.model.CollectionResponseDto
import com.example.outfitai.data.model.CollectionUpdateDto
import javax.inject.Inject

class CollectionRepositoryImpl @Inject constructor(
    private val api: CollectionApi,
) : CollectionRepository {
    override suspend fun list(): List<CollectionResponseDto> = api.list()
    override suspend fun get(id: Int): CollectionResponseDto = api.get(id)
    override suspend fun create(dto: CollectionCreateDto): CollectionResponseDto = api.create(dto)
    override suspend fun rename(id: Int, dto: CollectionUpdateDto): CollectionResponseDto = api.rename(id, dto)
    override suspend fun delete(id: Int) { api.delete(id) }
}
