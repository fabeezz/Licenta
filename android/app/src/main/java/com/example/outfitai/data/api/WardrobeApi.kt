package com.example.outfitai.data.api

import com.example.outfitai.data.model.ItemOutDto
import retrofit2.http.GET
import retrofit2.http.Query
import com.example.outfitai.data.model.OutfitOutDto

interface WardrobeApi {
  @GET("wardrobe/items")
  suspend fun listItems(
    @Query("category") category: String? = null,
    @Query("brand") brand: String? = null,
    @Query("dominant_color") dominantColor: String? = null,
    @Query("colors") colors: List<String>? = null,
    @Query("material") material: String? = null,
    @Query("season") season: String? = null,
    @Query("occasion") occasion: String? = null,
    @Query("sort_by") sortBy: String = "created_at",
    @Query("sort_dir") sortDir: String = "desc",
    @Query("limit") limit: Int = 50,
    @Query("offset") offset: Int = 0
  ): List<ItemOutDto>
}