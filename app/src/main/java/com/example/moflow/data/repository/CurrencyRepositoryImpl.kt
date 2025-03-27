// data/repository/CurrencyRepositoryImpl.kt
package com.example.moflow.data.repository

import com.example.moflow.data.remote.service.ExchangeRateService
import com.example.moflow.domain.model.ExchangeRate
import com.example.moflow.domain.repository.CurrencyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class CurrencyRepositoryImpl @Inject constructor(
    private val exchangeRateService: ExchangeRateService
) : CurrencyRepository {

    override suspend fun getLatestExchangeRates(baseCurrency: String): Result<ExchangeRate> {
        return try {
            val response = exchangeRateService.getLatestRates(baseCurrency)
            Result.success(response.toExchangeRate())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getSupportedCurrencies(): Flow<List<String>> = flow {
        try {
            val response = exchangeRateService.getLatestRates("USD")
            emit(response.conversionRates.keys.toList() + response.baseCode)
        } catch (e: Exception) {
            emit(listOf("USD", "EUR", "JPY", "GBP", "AUD", "CAD", "CHF", "CNY", "IDR"))
        }
    }

    override suspend fun convertCurrency(amount: Double, from: String, to: String): Result<Double> {
        return try {
            val rates = exchangeRateService.getLatestRates(from)
            val rate = rates.conversionRates[to] ?: 1.0
            Result.success(amount * rate)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}