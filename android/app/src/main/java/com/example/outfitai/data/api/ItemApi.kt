package com.example.outfitai.data.api

import com.example.outfitai.data.model.ItemOutDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ItemApi {
    @Multipart
    @POST("item/create")
    suspend fun createItem(
        @Part image: MultipartBody.Part,
        @Part("brand") brand: RequestBody? = null,
        @Part("material") material: RequestBody? = null,
        @Part("season") season: RequestBody? = null,
        @Part("occasion") occasion: RequestBody? = null,
    ): ItemOutDto
}