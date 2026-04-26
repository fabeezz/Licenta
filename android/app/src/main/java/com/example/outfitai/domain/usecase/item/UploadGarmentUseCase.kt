package com.example.outfitai.domain.usecase.item

import android.net.Uri
import com.example.outfitai.core.common.Resource
import com.example.outfitai.data.item.ItemRepository
import com.example.outfitai.data.model.ItemOutDto
import com.example.outfitai.data.remote.safeApiCall
import javax.inject.Inject

class UploadGarmentUseCase @Inject constructor(
    private val repo: ItemRepository,
) {
    suspend operator fun invoke(
        uri: Uri,
        brand: String?,
        material: String?,
        weather: List<String>?,
        occasion: String?,
    ): Resource<ItemOutDto> = safeApiCall {
        repo.uploadItem(uri, brand, material, weather, occasion)
    }
}
