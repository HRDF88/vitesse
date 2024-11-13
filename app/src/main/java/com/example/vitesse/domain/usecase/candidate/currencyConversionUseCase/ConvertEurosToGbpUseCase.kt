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

    private var exchangeRate: Double? = null

    /**
     * Converts the specified amount of Euros to GBP.
     * @param amount The amount of Euros to convert.
     * @return The converted amount in GBP.
     */
    suspend operator fun invoke(amount: Double): Double {
        // Vérifier si le taux de change est déjà chargé
        if (exchangeRate == null) {
            // Charger le taux de change dans un contexte IO pour les appels réseau
            exchangeRate = withContext(Dispatchers.IO) {
                try {
                    currencyChangeRepositoryInterface.getExchangeRate()
                } catch (e: Exception) {
                    // Log de l'erreur si le taux de change échoue
                    Log.e("CurrencyExchange", "Erreur lors de la récupération du taux de change", e)
                    null
                }
            }
        }

        // Si un taux de change a été récupéré, on effectue la conversion
        return exchangeRate?.let { amount * it } ?: 0.0
    }
}