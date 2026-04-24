package com.example.outfitai.domain.usecase.wardrobe

import com.example.outfitai.core.common.Resource
import com.example.outfitai.data.model.OutfitSavedDto
import com.example.outfitai.data.remote.safeApiCall
import com.example.outfitai.data.wardrobe.WardrobeRepository
import javax.inject.Inject

class GetWardrobeOutfitsUseCase @Inject constructor(
    private val repo: WardrobeRepository,
) {
    suspend operator fun invoke(
        season: String? = null,
        occasion: String? = null,
    ): Resource<List<OutfitSavedDto>> = safeApiCall {
        repo.listOutfits(season = season, occasion = occasion)
    }
}
