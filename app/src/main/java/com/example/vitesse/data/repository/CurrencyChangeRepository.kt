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
            // Récupérer la clé API via BuildConfig
            val apiKeyFromBuildConfig = BuildConfig.API_KEY


            // Utilisation de la clé API dans la requête
            val response = currencyChangeApiService.convertEurToGbp(
                apiKey = apiKeyFromBuildConfig,
                base = CurrencyConstants.BASE_CURRENCY,
                foreign = CurrencyConstants.FOREIGN_CURRENCY
            )

            Log.d("CurrencyExchange", "Réponse API : $response")

            // Vérifier si le taux de change est valide
            val exchangeRate = response.foreign.rate

            if (exchangeRate.isNaN() || exchangeRate <= 0) {
                Log.e("CurrencyExchange", "Taux de change invalide : $exchangeRate")
                throw RuntimeException("Le taux de change récupéré est invalide")
            }

            Log.d("CurrencyExchange", "Taux de change récupéré : $exchangeRate")
            return exchangeRate

        } catch (e: Exception) {
            Log.e("CurrencyExchange", "Erreur lors de la récupération du taux de change", e)
            throw RuntimeException("Échec de la récupération du taux de change", e)
        }
    }
}