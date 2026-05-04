package com.example.outfitai.data.api

import com.example.outfitai.data.model.OutfitCreateDto
import com.example.outfitai.data.model.OutfitSavedDto
import com.example.outfitai.data.model.OutfitSuggestRequest
import com.example.outfitai.data.model.OutfitSuggestResponse
import retrofit2.http.*

interface OutfitApi {
    @POST("outfits/")
    suspend fun create(@Body body: OutfitCreateDto): OutfitSavedDto

    @GET("outfits/")
    suspend fun list(
        @Query("weather") weather: String? = null,
        @Query("style") style: String? = null,
    ): List<OutfitSavedDto>

    @GET("outfits/{id}")
    suspend fun get(@Path("id") id: Int): OutfitSavedDto

    @DELETE("outfits/{id}")
    suspend fun delete(@Path("id") id: Int)

    @POST("outfits/suggest")
    suspend fun suggest(@Body body: OutfitSuggestRequest): OutfitSuggestResponse
}
