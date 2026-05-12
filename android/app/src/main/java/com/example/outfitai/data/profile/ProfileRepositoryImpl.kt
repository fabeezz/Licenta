package com.example.outfitai.data.profile

import com.example.outfitai.data.api.ProfileApi
import com.example.outfitai.data.model.OnboardingDto
import com.example.outfitai.data.model.ProfileUpdateDto
import com.example.outfitai.data.model.UserOutDto
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val api: ProfileApi,
) : ProfileRepository {
    override suspend fun completeOnboarding(payload: OnboardingDto): UserOutDto =
        api.completeOnboarding(payload)

    override suspend fun updateProfile(payload: ProfileUpdateDto): UserOutDto =
        api.updateProfile(payload)
}
