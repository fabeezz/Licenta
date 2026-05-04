package com.example.outfitai.domain.usecase.outfits

import com.example.outfitai.core.common.Resource
import com.example.outfitai.data.model.OutfitSuggestRequest
import com.example.outfitai.data.model.OutfitSuggestResponse
import com.example.outfitai.data.outfits.OutfitRepository
import com.example.outfitai.data.remote.safeApiCall
import javax.inject.Inject

class SuggestOutfitUseCase @Inject constructor(
    private val repo: OutfitRepository,
) {
    suspend operator fun invoke(
        style: String? = null,
        weather: String? = null,
        modes: List<String>? = null,
    ): Resource<OutfitSuggestResponse> =
        safeApiCall { repo.suggest(OutfitSuggestRequest(style = style, weather = weather, modes = modes)) }
}
