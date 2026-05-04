package com.example.outfitai.data.trips

import com.example.outfitai.core.common.Resource
import com.example.outfitai.data.api.TripApi
import com.example.outfitai.data.model.DestinationDto
import com.example.outfitai.data.model.TripGenerateRequestDto
import com.example.outfitai.data.model.TripPlanResponseDto
import com.example.outfitai.data.model.TripReadDto
import com.example.outfitai.data.model.TripSaveRequestDto
import com.example.outfitai.data.remote.safeApiCall
import javax.inject.Inject

class TripRepositoryImpl @Inject constructor(
    private val api: TripApi,
) : TripRepository {
    override suspend fun getDestinations(): Resource<List<DestinationDto>> =
        safeApiCall { api.getDestinations() }

    override suspend fun generate(request: TripGenerateRequestDto): Resource<TripPlanResponseDto> =
        safeApiCall { api.generate(request) }

    override suspend fun save(request: TripSaveRequestDto): Resource<TripReadDto> =
        safeApiCall { api.save(request) }
}
