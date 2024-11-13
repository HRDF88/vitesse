package com.example.vitesse.domain.model

/**
 * This data class represents the response for currency exchange.
 */
data class CurrencyExchangeResponse(
    val last_update_unix: Long,
    val last_update_utc: String,
    val next_update_unix: Long,
    val next_update_utc: String,
    val conversion: String,
    val base: BaseCurrency,
    val foreign: ForeignCurrency
)

data class BaseCurrency(
    val code: String,
    val symbol: String,
    val amount: Double
)

data class ForeignCurrency(
    val code: String,
    val symbol: String,
    val rate: Double,
    val amount: Double
)

