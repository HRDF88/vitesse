package com.example.vitesse.data.webService

import com.example.vitesse.domain.model.CurrencyExchangeResponse
import retrofit2.http.GET
import retrofit2.http.QueryMap

/**
 * This interface represents the API service for currency exchange operations.
 */
interface CurrencyChangeApiService {
    @GET("currency-exchange")
    suspend fun convertEurToGbp(@QueryMap params: Map<String, Any>): CurrencyExchangeResponse
}