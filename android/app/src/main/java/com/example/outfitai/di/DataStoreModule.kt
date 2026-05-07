package com.example.outfitai.di

import android.content.Context
import com.example.outfitai.data.auth.AuthStore
import com.example.outfitai.data.location.DeviceLocationProvider
import com.example.outfitai.data.location.LocationStore
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideAuthStore(@ApplicationContext ctx: Context): AuthStore = AuthStore(ctx)

    @Provides
    @Singleton
    fun provideLocationStore(@ApplicationContext ctx: Context): LocationStore = LocationStore(ctx)

    @Provides
    @Singleton
    fun provideDeviceLocationProvider(
        @ApplicationContext ctx: Context,
    ): DeviceLocationProvider = DeviceLocationProvider(
        client = LocationServices.getFusedLocationProviderClient(ctx),
        context = ctx,
    )
}
