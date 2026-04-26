package com.example.outfitai.data.item

import android.net.Uri
import com.example.outfitai.data.model.ItemOutDto
import com.example.outfitai.data.model.ItemUpdateDto

interface ItemRepository {
    suspend fun uploadItem(
        uri: Uri,
        brand: String?,
        material: String?,
        weather: List<String>?,
        occasion: String?,
    ): ItemOutDto

    suspend fun getItem(id: Int): ItemOutDto
    suspend fun updateItem(id: Int, body: ItemUpdateDto): ItemOutDto
    suspend fun wearItem(id: Int): ItemOutDto
    suspend fun deleteItem(id: Int)
}
