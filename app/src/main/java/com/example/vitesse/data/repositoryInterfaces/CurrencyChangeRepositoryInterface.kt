package com.example.vitesse.data.repositoryInterfaces

import com.example.vitesse.domain.model.CurrencyExchangeResponse

/**
 * This interface represents the repository responsible for managing currency exchange operations.
 */
interface CurrencyChangeRepositoryInterface {
    /**
     * This method retrieves the current exchange rate.
     *
     * @return the current exchange rate (as a double).
     */
    suspend fun getExchangeRate(): Double
}