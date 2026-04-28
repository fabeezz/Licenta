package com.example.outfitai.domain.usecase.trips

import com.example.outfitai.core.common.Resource
import com.example.outfitai.data.model.TripReadDto
import com.example.outfitai.data.model.TripSaveRequestDto
import com.example.outfitai.data.trips.TripRepository
import javax.inject.Inject

class SaveTripUseCase @Inject constructor(
    private val repo: TripRepository,
) {
    suspend operator fun invoke(request: TripSaveRequestDto): Resource<TripReadDto> =
        repo.save(request)
}
