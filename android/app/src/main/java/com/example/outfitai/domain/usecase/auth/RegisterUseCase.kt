package com.example.outfitai.domain.usecase.auth

import com.example.outfitai.core.common.Resource
import com.example.outfitai.data.auth.AuthRepository
import com.example.outfitai.data.model.UserOutDto
import com.example.outfitai.data.remote.safeApiCall
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val repo: AuthRepository,
) {
    suspend operator fun invoke(username: String, email: String, password: String): Resource<UserOutDto> =
        safeApiCall {
            repo.register(username, email, password)
            repo.me()
        }
}
