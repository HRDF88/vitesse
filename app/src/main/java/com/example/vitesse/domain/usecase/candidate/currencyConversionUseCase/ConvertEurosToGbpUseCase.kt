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

    // Durée de cache de 24 heures (en millisecondes)
    private val cacheDurationMillis = 24 * 60 * 60 * 1000 // 24 heures

    /**
     * Convertit le montant spécifié d'euros en GBP.
     * @param amount Le montant en euros à convertir.
     * @return Le montant converti en GBP.
     */
    suspend operator fun invoke(amount: Double): Double {
        // Si le cache est expiré ou vide, recharger le taux de change
        if (cachedExchangeRate == null || isCacheExpired()) {
            cachedExchangeRate = loadExchangeRate()
        }

        // Si le taux est valide, effectuer la conversion
        return cachedExchangeRate?.let { amount * it } ?: 0.0
    }

    /**
     * Vérifie si le cache du taux de change a expiré (durée : 24 heures).
     */
    private fun isCacheExpired(): Boolean {
        return System.currentTimeMillis() - lastFetchedTime > cacheDurationMillis
    }

    /**
     * Charge le taux de change depuis l'API et met à jour le temps de cache.
     */
    private suspend fun loadExchangeRate(): Double? {
        return withContext(Dispatchers.IO) {
            try {
                val rate = currencyChangeRepositoryInterface.getExchangeRate()
                lastFetchedTime = System.currentTimeMillis() // Mettre à jour le temps de cache
                rate
            } catch (e: Exception) {
                Log.e("CurrencyExchange", "Erreur lors de la récupération du taux de change", e)
                null
            }
        }
    }
}
