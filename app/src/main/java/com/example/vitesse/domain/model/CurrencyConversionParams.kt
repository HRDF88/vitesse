package com.example.vitesse.domain.model

/**
 * This data class represents the parameters for currency conversion.
 */
data class CurrencyConversionParams(
val baseCurrency: String = "EUR",
val foreignCurrency: String = "GBP",
val baseAmount: Int = 1
)
