package com.example.vitesse.data.repository

import android.util.Log
import com.example.vitesse.BuildConfig
import com.example.vitesse.data.repositoryInterfaces.CurrencyChangeRepositoryInterface
import com.example.vitesse.data.webservice.CurrencyChangeApiService
import com.example.vitesse.data.webservice.CurrencyConstants
import javax.inject.Inject

/**
 * This class implements the CurrencyChangeRepositoryInterface and is responsible for handling currency exchange operations.
 */
class CurrencyChangeRepository @Inject constructor(private val currencyChangeApiService: CurrencyChangeApiService) :
    CurrencyChangeRepositoryInterface {

    /**
     * This method retrieves the current exchange rate.
     *
     * @return the current exchange rate (as a double).
     */
    override suspend fun getExchangeRate(): Double {
        try {
            //Retrieve the API key via BuildConfig
            val apiKeyFromBuildConfig = BuildConfig.API_KEY


            // Using the API key in the request
            val response = currencyChangeApiService.convertEurToGbp(
                apiKey = apiKeyFromBuildConfig,
                base = CurrencyConstants.BASE_CURRENCY,
                foreign = CurrencyConstants.FOREIGN_CURRENCY
            )



            // Check if the exchange rate is valid.
            val exchangeRate = response.foreign.rate

            if (exchangeRate.isNaN() || exchangeRate <= 0) {
                throw RuntimeException("The retrieved exchange rate is invalid")
            }


            return exchangeRate

        } catch (e: Exception) {
            throw RuntimeException("Failed to recover exchange rate", e)
        }
    }
}