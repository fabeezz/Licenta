package com.example.outfitai.domain.usecase.item

import com.example.outfitai.core.common.Resource
import com.example.outfitai.data.item.ItemRepository
import com.example.outfitai.data.model.ItemOutDto
import com.example.outfitai.data.remote.safeApiCall
import javax.inject.Inject

class GetItemUseCase @Inject constructor(
    private val repo: ItemRepository,
) {
    suspend operator fun invoke(id: Int): Resource<ItemOutDto> = safeApiCall { repo.getItem(id) }
}
