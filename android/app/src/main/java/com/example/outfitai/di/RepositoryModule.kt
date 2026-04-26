package com.example.outfitai.di

import com.example.outfitai.data.auth.AuthRepository
import com.example.outfitai.data.auth.AuthRepositoryImpl
import com.example.outfitai.data.collections.CollectionRepository
import com.example.outfitai.data.collections.CollectionRepositoryImpl
import com.example.outfitai.data.item.ItemRepository
import com.example.outfitai.data.item.ItemRepositoryImpl
import com.example.outfitai.data.outfits.OutfitRepository
import com.example.outfitai.data.outfits.OutfitRepositoryImpl
import com.example.outfitai.data.wardrobe.WardrobeRepository
import com.example.outfitai.data.wardrobe.WardrobeRepositoryImpl
import com.example.outfitai.data.weather.WeatherRepository
import com.example.outfitai.data.weather.WeatherRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindCollectionRepository(impl: CollectionRepositoryImpl): CollectionRepository

    @Binds
    @Singleton
    abstract fun bindItemRepository(impl: ItemRepositoryImpl): ItemRepository

    @Binds
    @Singleton
    abstract fun bindWardrobeRepository(impl: WardrobeRepositoryImpl): WardrobeRepository

    @Binds
    @Singleton
    abstract fun bindOutfitRepository(impl: OutfitRepositoryImpl): OutfitRepository

    @Binds
    @Singleton
    abstract fun bindWeatherRepository(impl: WeatherRepositoryImpl): WeatherRepository
}
