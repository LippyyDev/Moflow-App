// domain/repository/CurrencyRepository.kt
package com.example.moflow.domain.repository

import com.example.moflow.domain.model.ExchangeRate
import kotlinx.coroutines.flow.Flow

interface CurrencyRepository {
    suspend fun getLatestExchangeRates(baseCurrency: String): Result<ExchangeRate>
    fun getSupportedCurrencies(): Flow<List<String>>
    suspend fun convertCurrency(amount: Double, from: String, to: String): Result<Double>
}