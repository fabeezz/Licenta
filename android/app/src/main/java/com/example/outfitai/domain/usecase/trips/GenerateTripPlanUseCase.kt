package com.example.outfitai.domain.usecase.trips

import com.example.outfitai.core.common.Resource
import com.example.outfitai.data.model.TripGenerateRequestDto
import com.example.outfitai.data.model.TripPlanResponseDto
import com.example.outfitai.data.trips.TripRepository
import javax.inject.Inject

class GenerateTripPlanUseCase @Inject constructor(
    private val repo: TripRepository,
) {
    suspend operator fun invoke(request: TripGenerateRequestDto): Resource<TripPlanResponseDto> =
        repo.generate(request)
}
