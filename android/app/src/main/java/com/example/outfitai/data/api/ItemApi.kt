package com.example.outfitai.data.api

import com.example.outfitai.data.model.BasicStatsDto
import com.example.outfitai.data.model.ColorStatsDto
import com.example.outfitai.data.model.GapsResponseDto
import com.example.outfitai.data.model.ItemOutDto
import com.example.outfitai.data.model.ItemUpdateDto
import com.example.outfitai.data.model.WeatherStatsDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface ItemApi {
    @Multipart
    @POST("items")
    suspend fun createItem(
        @Part image: MultipartBody.Part,
        @Part("brand") brand: RequestBody? = null,
        @Part("material") material: RequestBody? = null,
        @Part("weather") weather: RequestBody? = null,
        @Part("style") style: RequestBody? = null,
    ): ItemOutDto

    @GET("items")
    suspend fun listItems(
        @Query("category") category: String? = null,
        @Query("brand") brand: String? = null,
        @Query("dominant_color") dominantColor: String? = null,
        @Query("colors") colors: List<String>? = null,
        @Query("material") material: String? = null,
        @Query("weather") weather: String? = null,
        @Query("style") style: String? = null,
        @Query("sort_by") sortBy: String = "created_at",
        @Query("sort_dir") sortDir: String = "desc",
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0,
    ): List<ItemOutDto>

    @GET("items/search")
    suspend fun searchItems(
        @Query("q") query: String,
        @Query("category") category: String? = null,
        @Query("colors") colors: List<String>? = null,
        @Query("weather") weather: String? = null,
        @Query("style") style: String? = null,
        @Query("limit") limit: Int = 50,
    ): List<ItemOutDto>

    @GET("items/gaps")
    suspend fun getGaps(): GapsResponseDto

    @GET("items/stats")
    suspend fun getStats(): BasicStatsDto

    @GET("items/stats/by-color")
    suspend fun getColorStats(): ColorStatsDto

    @GET("items/stats/by-weather")
    suspend fun getWeatherStats(): WeatherStatsDto

    @GET("items/{id}")
    suspend fun read(@Path("id") id: Int): ItemOutDto

    @PATCH("items/{id}")
    suspend fun update(@Path("id") id: Int, @Body body: ItemUpdateDto): ItemOutDto

    @POST("items/{id}/wear")
    suspend fun wear(@Path("id") id: Int): ItemOutDto

    @DELETE("items/{id}")
    suspend fun delete(@Path("id") id: Int)
}
