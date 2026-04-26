package com.example.outfitai.domain.usecase.collections

import com.example.outfitai.core.common.Resource
import com.example.outfitai.data.collections.CollectionRepository
import com.example.outfitai.data.model.CollectionCreateDto
import com.example.outfitai.data.model.CollectionResponseDto
import com.example.outfitai.data.remote.safeApiCall
import javax.inject.Inject

class CreateCollectionUseCase @Inject constructor(
    private val repo: CollectionRepository,
) {
    suspend operator fun invoke(name: String, outfitIds: List<Int>): Resource<CollectionResponseDto> = safeApiCall {
        repo.create(CollectionCreateDto(name = name, outfitIds = outfitIds))
    }
}
