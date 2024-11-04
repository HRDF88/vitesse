package com.example.vitesse.domain.model

/**
 * This data class represents the response for currency exchange.
 */
data class CurrencyExchangeResponse(
    val last_update_unix: Long,
    val last_update_utc: String,
    val next_update_unix: Long,
    val next_update_utc: String,
    val base_code: String,
    val base_symbol: String,
    val base_amount: String,
    val foreign_code: String,
    val foreign_symbol: String,
    val foreign_rate: Double,
    val foreign_amount: Double
)

