package com.example.outfitai.data.api

import com.example.outfitai.data.model.DestinationDto
import com.example.outfitai.data.model.TripGenerateRequestDto
import com.example.outfitai.data.model.TripPlanResponseDto
import com.example.outfitai.data.model.TripReadDto
import com.example.outfitai.data.model.TripSaveRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface TripApi {
    @GET("trips/destinations")
    suspend fun getDestinations(): List<DestinationDto>

    @POST("trips/generate")
    suspend fun generate(@Body body: TripGenerateRequestDto): TripPlanResponseDto

    @POST("trips/save")
    suspend fun save(@Body body: TripSaveRequestDto): TripReadDto
}
