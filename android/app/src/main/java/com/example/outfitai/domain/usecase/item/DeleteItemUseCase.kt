package com.example.outfitai.domain.usecase.item

import com.example.outfitai.core.common.Resource
import com.example.outfitai.data.item.ItemRepository
import com.example.outfitai.data.remote.safeApiCall
import javax.inject.Inject

class DeleteItemUseCase @Inject constructor(
    private val repo: ItemRepository,
) {
    suspend operator fun invoke(id: Int): Resource<Unit> = safeApiCall { repo.deleteItem(id) }
}
