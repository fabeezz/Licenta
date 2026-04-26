package com.example.outfitai.domain.usecase.collections

import com.example.outfitai.core.common.Resource
import com.example.outfitai.data.collections.CollectionRepository
import com.example.outfitai.data.remote.safeApiCall
import javax.inject.Inject

class DeleteCollectionUseCase @Inject constructor(
    private val repo: CollectionRepository,
) {
    suspend operator fun invoke(id: Int): Resource<Unit> = safeApiCall {
        repo.delete(id)
    }
}
