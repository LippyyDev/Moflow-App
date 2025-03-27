// domain/usecase/currency/GetExchangeRatesUseCase.kt
package com.example.moflow.domain.usecase.currency

import com.example.moflow.domain.model.ExchangeRate
import com.example.moflow.domain.repository.CurrencyRepository
import javax.inject.Inject

class GetExchangeRatesUseCase @Inject constructor(
    private val repository: CurrencyRepository
) {
    suspend operator fun invoke(baseCurrency: String): Result<ExchangeRate> {
        return repository.getLatestExchangeRates(baseCurrency)
    }
}