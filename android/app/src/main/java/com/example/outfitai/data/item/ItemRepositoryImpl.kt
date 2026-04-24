package com.example.outfitai.data.item

import android.net.Uri
import com.example.outfitai.core.media.MediaUploader
import com.example.outfitai.data.api.ItemApi
import com.example.outfitai.data.model.ItemOutDto
import com.example.outfitai.data.model.ItemUpdateDto
import javax.inject.Inject

class ItemRepositoryImpl @Inject constructor(
    private val api: ItemApi,
    private val uploader: MediaUploader,
) : ItemRepository {

    override suspend fun uploadItem(
        uri: Uri,
        brand: String?,
        material: String?,
        season: String?,
        occasion: String?,
    ): ItemOutDto = api.createItem(
        image = uploader.part(uri, "image"),
        brand = uploader.textPartOrNull(brand),
        material = uploader.textPartOrNull(material),
        season = uploader.textPartOrNull(season),
        occasion = uploader.textPartOrNull(occasion),
    )

    override suspend fun getItem(id: Int): ItemOutDto = api.read(id)

    override suspend fun updateItem(id: Int, body: ItemUpdateDto): ItemOutDto = api.update(id, body)

    override suspend fun wearItem(id: Int): ItemOutDto = api.wear(id)

    override suspend fun deleteItem(id: Int) { api.delete(id) }
}
