package com.example.outfitai.data.remote

import com.example.outfitai.core.common.Resource
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

suspend inline fun <T> safeApiCall(crossinline block: suspend () -> T): Resource<T> =
    try {
        Resource.Success(block())
    } catch (e: HttpException) {
        val msg = when (e.code()) {
            400 -> "Bad request"
            401 -> "Unauthorized — please log in again"
            403 -> "Access denied"
            404 -> "Not found"
            409 -> "Conflict — resource already exists"
            422 -> "Validation error"
            500 -> "Server error — try again later"
            else -> "HTTP ${e.code()}"
        }
        Resource.Error(msg, e, e.code())
    } catch (e: IOException) {
        Resource.Error("No network connection", e)
    } catch (e: SerializationException) {
        Resource.Error("Unexpected response format", e)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Unknown error", e)
    }
