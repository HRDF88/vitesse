package com.example.vitesse.data.webservice

import com.example.vitesse.domain.model.CurrencyExchangeResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * This interface represents the API service for currency exchange operations.
 */
interface CurrencyChangeApiService {
    @GET("api/currency-exchange")
    suspend fun convertEurToGbp(
        @Query("key") apiKey: String,
        @Query("base") base: String,
        @Query("foreign") foreign: String,
        @Query("base_amount") baseAmount: String = "1" // default value
    ): CurrencyExchangeResponse
}