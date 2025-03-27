// domain/usecase/currency/ConvertCurrencyUseCase.kt
package com.example.moflow.domain.usecase.currency

import com.example.moflow.domain.repository.CurrencyRepository
import javax.inject.Inject

class ConvertCurrencyUseCase @Inject constructor(
    private val repository: CurrencyRepository
) {
    suspend operator fun invoke(amount: Double, from: String, to: String): Result<Double> {
        return repository.convertCurrency(amount, from, to)
    }
}