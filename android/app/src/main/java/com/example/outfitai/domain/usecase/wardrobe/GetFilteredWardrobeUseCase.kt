package com.example.outfitai.domain.usecase.wardrobe

import com.example.outfitai.core.common.Resource
import com.example.outfitai.data.model.ItemOutDto
import com.example.outfitai.data.remote.safeApiCall
import com.example.outfitai.data.wardrobe.WardrobeRepository
import javax.inject.Inject

class GetFilteredWardrobeUseCase @Inject constructor(
    private val repo: WardrobeRepository,
) {
    suspend operator fun invoke(
        category: String? = null,
        brand: String? = null,
        dominantColor: String? = null,
        colors: List<String>? = null,
        material: String? = null,
        weather: String? = null,
        occasion: String? = null,
        sortBy: String = "created_at",
        sortDir: String = "desc",
        limit: Int = 50,
        offset: Int = 0,
    ): Resource<List<ItemOutDto>> = safeApiCall {
        repo.listItems(
            category = category,
            brand = brand,
            dominantColor = dominantColor,
            colors = colors,
            material = material,
            weather = weather,
            occasion = occasion,
            sortBy = sortBy,
            sortDir = sortDir,
            limit = limit,
            offset = offset,
        )
    }
}
