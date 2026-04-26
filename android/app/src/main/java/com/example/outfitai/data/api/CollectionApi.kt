package com.example.outfitai.data.api

import com.example.outfitai.data.model.CollectionCreateDto
import com.example.outfitai.data.model.CollectionResponseDto
import com.example.outfitai.data.model.CollectionUpdateDto
import retrofit2.http.*

interface CollectionApi {
    @POST("collections/")
    suspend fun create(@Body body: CollectionCreateDto): CollectionResponseDto

    @GET("collections/")
    suspend fun list(
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 100,
    ): List<CollectionResponseDto>

    @GET("collections/{id}")
    suspend fun get(@Path("id") id: Int): CollectionResponseDto

    @PATCH("collections/{id}")
    suspend fun rename(@Path("id") id: Int, @Body body: CollectionUpdateDto): CollectionResponseDto

    @DELETE("collections/{id}")
    suspend fun delete(@Path("id") id: Int)
}
