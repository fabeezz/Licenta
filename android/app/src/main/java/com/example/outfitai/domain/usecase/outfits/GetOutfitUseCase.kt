package com.example.outfitai.domain.usecase.outfits

import com.example.outfitai.core.common.Resource
import com.example.outfitai.data.model.OutfitSavedDto
import com.example.outfitai.data.outfits.OutfitRepository
import com.example.outfitai.data.remote.safeApiCall
import javax.inject.Inject

class GetOutfitUseCase @Inject constructor(
    private val repo: OutfitRepository,
) {
    suspend operator fun invoke(id: Int): Resource<OutfitSavedDto> = safeApiCall { repo.get(id) }
}
