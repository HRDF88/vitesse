package com.example.vitesse.domain.usecase.candidate.currencyConversionUseCase

import com.example.vitesse.data.repositoryInterfaces.CurrencyChangeRepositoryInterface
/**
 * Use case for converting Euros to GBP (Great British Pounds) using a given exchange rate.
 * The exchange rate is retrieved from the CurrencyChangeRepositoryInterface.
 */
class ConvertEurosToGbpUseCase(private val currencyChangeRepositoryInterface: CurrencyChangeRepositoryInterface) {
    private var exchangeRate : Double?=null

    /**
     * Converts the specified amount of Euros to GBP.
     * @param amount The amount of Euros to convert.
     * @return The converted amount in GBP.
     */
    suspend operator fun invoke(amount: Double):Double{
        if (exchangeRate==null){
            exchangeRate = currencyChangeRepositoryInterface.getExchangeRate()
        }
        return amount*exchangeRate!!
    }
}