package com.example.outfitai.di

import android.content.Context
import com.example.outfitai.Config
import com.example.outfitai.data.api.AuthApi
import com.example.outfitai.data.api.AuthInterceptor
import com.example.outfitai.data.api.ItemApi
import com.example.outfitai.data.api.OutfitApi
import com.example.outfitai.data.api.WardrobeApi
import com.example.outfitai.data.auth.AuthRepository
import com.example.outfitai.data.auth.AuthStore
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

  @Provides @Singleton
  fun provideJson(): Json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
  }

  @Provides @Singleton
  fun provideAuthStore(@ApplicationContext ctx: Context): AuthStore =
    AuthStore(ctx)

  @Provides @Singleton
  fun provideAuthInterceptor(authStore: AuthStore): AuthInterceptor =
    AuthInterceptor(authStore)

  @Provides @Singleton
  fun provideOkHttp(authInterceptor: AuthInterceptor): OkHttpClient =
    OkHttpClient.Builder()
      .addInterceptor(authInterceptor)
      .build()

  @Provides @Singleton
  fun provideRetrofit(okHttp: OkHttpClient, json: Json): Retrofit =
    Retrofit.Builder()
      .baseUrl(Config.BASE_URL)
      .client(okHttp)
      .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
      .build()

  @Provides @Singleton
  fun provideAuthApi(retrofit: Retrofit): AuthApi =
    retrofit.create(AuthApi::class.java)

  @Provides @Singleton
  fun provideAuthRepository(authApi: AuthApi, authStore: AuthStore): AuthRepository =
    AuthRepository(authApi, authStore)

  @Provides @Singleton
  fun provideWardrobeApi(retrofit: Retrofit): WardrobeApi =
    retrofit.create(WardrobeApi::class.java)

  @Provides @Singleton
  fun provideItemApi(retrofit: Retrofit): ItemApi =
    retrofit.create(ItemApi::class.java)

  @Provides @Singleton
  fun provideOutfitApi(retrofit: Retrofit): OutfitApi =
    retrofit.create(OutfitApi::class.java)
}
