package com.example.outfitai.domain.usecase.outfits

import com.example.outfitai.core.common.Resource
import com.example.outfitai.data.model.OutfitCreateDto
import com.example.outfitai.data.outfits.OutfitRepository
import com.example.outfitai.data.remote.safeApiCall
import javax.inject.Inject

class CreateOutfitUseCase @Inject constructor(
    private val repo: OutfitRepository,
) {
    suspend operator fun invoke(dto: OutfitCreateDto): Resource<Int> =
        safeApiCall { repo.create(dto) }
}
