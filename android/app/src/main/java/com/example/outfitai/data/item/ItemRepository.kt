package com.example.outfitai.data.item

import android.content.Context
import android.net.Uri
import com.example.outfitai.data.api.ItemApi
import com.example.outfitai.data.model.ItemOutDto
import com.example.outfitai.data.model.ItemUpdateDto
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.BufferedSink
import okio.source
import javax.inject.Inject

class ItemRepository @Inject constructor(
    private val api: ItemApi,
    @ApplicationContext private val context: Context
) {
    suspend fun uploadItem(
        uri: Uri,
        brand: String?,
        material: String?,
        season: String?,
        occasion: String?,
    ): ItemOutDto {
        val cr = context.contentResolver

        val mime = cr.getType(uri) ?: "image/*"
        val fileName = "upload.${mime.substringAfter('/', "jpg")}"

        val imageBody = object : RequestBody() {
            override fun contentType() = mime.toMediaType()
            override fun writeTo(sink: BufferedSink) {
                cr.openInputStream(uri)?.use { input ->
                    sink.writeAll(input.source())
                } ?: error("Cannot open input stream for uri=$uri")
            }
        }

        val imagePart = MultipartBody.Part.createFormData(
            name = "image",
            filename = fileName,
            body = imageBody
        )

        fun textPartOrNull(v: String?): RequestBody? =
            v?.trim()?.takeIf { it.isNotBlank() }?.toRequestBody("text/plain".toMediaType())

        return api.createItem(
            image = imagePart,
            brand = textPartOrNull(brand),
            material = textPartOrNull(material),
            season = textPartOrNull(season),
            occasion = textPartOrNull(occasion),
        )
    }

    suspend fun getItem(id: Int) = api.read(id)

    suspend fun updateItem(id: Int, body: ItemUpdateDto) = api.update(id, body)

    suspend fun wearItem(id: Int) = api.wear(id)

    suspend fun deleteItem(id: Int) { api.delete(id) }
}