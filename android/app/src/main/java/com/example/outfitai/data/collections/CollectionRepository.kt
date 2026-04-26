package com.example.outfitai.data.collections

import com.example.outfitai.data.model.CollectionCreateDto
import com.example.outfitai.data.model.CollectionResponseDto
import com.example.outfitai.data.model.CollectionUpdateDto

interface CollectionRepository {
    suspend fun list(): List<CollectionResponseDto>
    suspend fun get(id: Int): CollectionResponseDto
    suspend fun create(dto: CollectionCreateDto): CollectionResponseDto
    suspend fun rename(id: Int, dto: CollectionUpdateDto): CollectionResponseDto
    suspend fun delete(id: Int)
}
