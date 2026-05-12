package com.example.outfitai.data.profile

import com.example.outfitai.data.model.OnboardingDto
import com.example.outfitai.data.model.ProfileUpdateDto
import com.example.outfitai.data.model.UserOutDto

interface ProfileRepository {
    suspend fun completeOnboarding(payload: OnboardingDto): UserOutDto
    suspend fun updateProfile(payload: ProfileUpdateDto): UserOutDto
}
