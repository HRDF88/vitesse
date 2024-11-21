package com.example.vitesse.data.repositoryInterfaces

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