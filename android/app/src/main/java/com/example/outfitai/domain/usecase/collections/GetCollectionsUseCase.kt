package com.example.outfitai.domain.usecase.collections

import com.example.outfitai.core.common.Resource
import com.example.outfitai.data.collections.CollectionRepository
import com.example.outfitai.data.model.CollectionResponseDto
import com.example.outfitai.data.remote.safeApiCall
import javax.inject.Inject

class GetCollectionsUseCase @Inject constructor(
    private val repo: CollectionRepository,
) {
    suspend operator fun invoke(): Resource<List<CollectionResponseDto>> = safeApiCall {
        repo.list()
    }
}
