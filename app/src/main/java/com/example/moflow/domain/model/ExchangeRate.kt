// domain/model/ExchangeRate.kt
package com.example.moflow.domain.model

data class ExchangeRate(
    val base: String,
    val timestamp: Long,
    val rates: Map<String, Double>
)

data class CurrencyConversion(
    val fromCurrency: String,
    val toCurrency: String,
    val amount: Double,
    val convertedAmount: Double,
    val rate: Double
)