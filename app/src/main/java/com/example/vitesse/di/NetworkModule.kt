package com.example.vitesse.di

import com.example.vitesse.data.webService.CurrencyChangeApiService
import com.example.vitesse.data.webService.RetrofitClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt NetworkModule for Api Class.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    fun provideCurrencyChangeApiService(): CurrencyChangeApiService{
        return RetrofitClient.currencyChangeApiService
    }
}