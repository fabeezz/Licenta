package com.example.outfitai.data.api

import com.example.outfitai.data.model.InspirationResponseDto
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface InspirationApi {
    @Multipart
    @POST("outfits/from-image")
    suspend fun fromImage(
        @Part image: MultipartBody.Part,
    ): InspirationResponseDto
}
