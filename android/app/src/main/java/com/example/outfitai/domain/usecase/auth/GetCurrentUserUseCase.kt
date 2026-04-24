package com.example.outfitai.domain.usecase.auth

import com.example.outfitai.core.common.Resource
import com.example.outfitai.data.auth.AuthRepository
import com.example.outfitai.data.auth.AuthStore
import com.example.outfitai.data.model.UserOutDto
import com.example.outfitai.data.remote.safeApiCall
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val authStore: AuthStore,
    private val repo: AuthRepository,
) {
    suspend operator fun invoke(): Resource<UserOutDto?> {
        val token = authStore.token.first()
        if (token.isNullOrBlank()) return Resource.Success(null)
        return when (val res = safeApiCall { repo.me() }) {
            is Resource.Success -> res
            is Resource.Error -> {
                if (res.httpCode == 401) authStore.clear()
                Resource.Error(res.message, res.cause, res.httpCode)
            }
            Resource.Loading -> Resource.Loading
        }
    }
}
