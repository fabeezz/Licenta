package com.example.outfitai.data.api

import com.example.outfitai.data.model.OutfitCreateDto
import com.example.outfitai.data.model.OutfitSavedDto
import retrofit2.http.Body
import retrofit2.http.POST

interface OutfitApi {
    @POST("outfits/")
    suspend fun create(@Body body: OutfitCreateDto): OutfitSavedDto
}
