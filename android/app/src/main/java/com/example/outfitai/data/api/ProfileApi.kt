package com.example.outfitai.data.api

import com.example.outfitai.data.model.OnboardingDto
import com.example.outfitai.data.model.ProfileUpdateDto
import com.example.outfitai.data.model.UserOutDto
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.POST

interface ProfileApi {
    @POST("profile/onboarding")
    suspend fun completeOnboarding(@Body body: OnboardingDto): UserOutDto

    @PATCH("profile")
    suspend fun updateProfile(@Body body: ProfileUpdateDto): UserOutDto
}
