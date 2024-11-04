package com.example.vitesse.data.repository

import com.example.vitesse.data.repositoryInterfaces.CurrencyChangeRepositoryInterface
import com.example.vitesse.data.webService.CurrencyChangeApiService
import com.example.vitesse.domain.model.CurrencyConversionParams
import javax.inject.Inject

/**
 * This class implements the CurrencyChangeRepositoryInterface and is responsible for handling currency exchange operations.
 */
class CurrencyChangeRepository @Inject constructor(private val currencyChangeApiService: CurrencyChangeApiService) : CurrencyChangeRepositoryInterface{

    /**
     * This method retrieves the current exchange rate.
     *
     * @return the current exchange rate (as a double).
     */
    override suspend fun getExchangeRate(): Double {
        val conversionParams = CurrencyConversionParams()

        val queryParams = mapOf(
            "base" to conversionParams.baseCurrency,
            "foreign" to conversionParams.foreignCurrency,
            "base_amount" to conversionParams.baseAmount
        )

        val response = currencyChangeApiService.convertEurToGbp(queryParams)
        val exchangeRate = response.foreign_rate

        return exchangeRate
    }
}