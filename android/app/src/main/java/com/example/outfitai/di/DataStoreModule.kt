package com.example.outfitai.di

import android.content.Context
import com.example.outfitai.data.auth.AuthStore
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
}
