package com.example.outfitai.domain.usecase.trips

import com.example.outfitai.core.common.Resource
import com.example.outfitai.data.model.DestinationDto
import com.example.outfitai.data.trips.TripRepository
import javax.inject.Inject

class GetDestinationsUseCase @Inject constructor(
    private val repo: TripRepository,
) {
    suspend operator fun invoke(): Resource<List<DestinationDto>> = repo.getDestinations()
}
