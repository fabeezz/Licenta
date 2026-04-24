package com.example.outfitai.core.common

sealed interface Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>
    data class Error(
        val message: String,
        val cause: Throwable? = null,
        val httpCode: Int? = null,
    ) : Resource<Nothing>
    data object Loading : Resource<Nothing>
}
