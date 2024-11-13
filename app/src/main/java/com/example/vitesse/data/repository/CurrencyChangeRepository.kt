package com.example.vitesse.data.repository

import android.util.Log
import com.example.vitesse.BuildConfig
import com.example.vitesse.data.repositoryInterfaces.CurrencyChangeRepositoryInterface
import com.example.vitesse.data.webservice.CurrencyChangeApiService
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
            // Définir la clé API en dur
            val apiKeyInHardCode = "sk-ek3E67288afd60804445"

            // Récupérer la clé API via BuildConfig
            val apiKeyFromBuildConfig = BuildConfig.API_KEY

            // Comparer les deux clés API
            if (apiKeyInHardCode == apiKeyFromBuildConfig) {
                Log.d("CurrencyExchange", "Les clés API correspondent : $apiKeyInHardCode")
            } else {
                Log.e("CurrencyExchange", "Les clés API ne correspondent pas. En dur : $apiKeyInHardCode, BuildConfig : $apiKeyFromBuildConfig")
                Log.e("CurrencyExchange", "Clé API BuildConfig : ${BuildConfig.API_KEY}")
            }

            // Utilisation de la clé API dans la requête
            val response = currencyChangeApiService.convertEurToGbp(
                apiKey = apiKeyFromBuildConfig, // Utiliser la clé de BuildConfig
                base = "EUR",
                foreign = "GBP"
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