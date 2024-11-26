package com.example.vitesse.domain.usecase.candidate.currencyConversionUseCase

import android.util.Log
import com.example.vitesse.data.repositoryInterfaces.CurrencyChangeRepositoryInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Use case for converting Euros to GBP (Great British Pounds) using a given exchange rate.
 * The exchange rate is retrieved from the CurrencyChangeRepositoryInterface.
 */
class ConvertEurosToGbpUseCase @Inject constructor(
    private val currencyChangeRepositoryInterface: CurrencyChangeRepositoryInterface
) {

    private var cachedExchangeRate: Double? = null
    private var lastFetchedTime: Long = 0

    // cache duration, parameters in CacheDurationConstant
    private val cacheDurationMillis = CacheDurationConstant.DURATION_CACHE

    /**
     * Converts the specified amount from Euros to GBP.
     * @param amount The amount in euros to convert.
     * @return The amount converted to GBP.
     */
    suspend operator fun invoke(amount: Double): Double {

        // If the cache is expired or empty, reload the exchange rate
        if (cachedExchangeRate == null || isCacheExpired()) {
            cachedExchangeRate = loadExchangeRate()
        }

        // If the rate is valid, perform the conversion
        return cachedExchangeRate?.let { amount * it } ?: 0.0
    }

    /**
     * Checks if the exchange rate cache has expired (duration: 24 hours).
     */
    private fun isCacheExpired(): Boolean {
        return System.currentTimeMillis() - lastFetchedTime > cacheDurationMillis
    }

    /**
     * Loads the exchange rate from the API and updates the cache time.
     */
    private suspend fun loadExchangeRate(): Double? {
        return withContext(Dispatchers.IO) {
            try {
                val rate = currencyChangeRepositoryInterface.getExchangeRate()
                lastFetchedTime = System.currentTimeMillis() // Update cache time
                rate
            } catch (e: Exception) {
                Log.e("CurrencyExchange", "Error retrieving exchange rate", e)
                null
            }
        }
    }
}
