package com.example.outfitai.data.api

import com.example.outfitai.data.model.ItemOutDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.*
import com.example.outfitai.data.model.ItemUpdateDto

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

    @GET("item/read/{id}")
    suspend fun read(@Path("id") id: Int): ItemOutDto
    @PATCH("item/update/{id}")
    suspend fun update(@Path("id") id: Int, @Body body: ItemUpdateDto): ItemOutDto
    @POST("item/wear/{id}")
    suspend fun wear(@Path("id") id: Int): ItemOutDto
    @DELETE("item/delete/{id}")
    suspend fun delete(@Path("id") id: Int)


}