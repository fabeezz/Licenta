package com.example.outfitai.data.trips

import com.example.outfitai.core.common.Resource
import com.example.outfitai.data.model.DestinationDto
import com.example.outfitai.data.model.TripGenerateRequestDto
import com.example.outfitai.data.model.TripPlanResponseDto
import com.example.outfitai.data.model.TripReadDto
import com.example.outfitai.data.model.TripSaveRequestDto

interface TripRepository {
    suspend fun getDestinations(): Resource<List<DestinationDto>>
    suspend fun generate(request: TripGenerateRequestDto): Resource<TripPlanResponseDto>
    suspend fun save(request: TripSaveRequestDto): Resource<TripReadDto>
}
