// ui/feature/currency/CurrencyViewModel.kt
package com.example.moflow.ui.feature.currency

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moflow.domain.model.ExchangeRate
import com.example.moflow.domain.usecase.currency.ConvertCurrencyUseCase
import com.example.moflow.domain.usecase.currency.GetExchangeRatesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CurrencyViewModel @Inject constructor(
    private val getExchangeRatesUseCase: GetExchangeRatesUseCase,
    private val convertCurrencyUseCase: ConvertCurrencyUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CurrencyState())
    val state: StateFlow<CurrencyState> = _state.asStateFlow()

    // Default list of common currencies in case API call fails
    private val defaultCurrencies = listOf(
        "USD", "EUR", "GBP", "JPY", "AUD", "CAD", "CHF", "CNY", "IDR", "INR", "KRW", "MYR", "SGD"
    )

    init {
        fetchExchangeRates()
    }

    private fun fetchExchangeRates() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                val result = getExchangeRatesUseCase(_state.value.fromCurrency)
                result.fold(
                    onSuccess = { exchangeRate ->
                        _state.update { state ->
                            state.copy(
                                exchangeRate = exchangeRate,
                                supportedCurrencies = (exchangeRate.rates.keys + exchangeRate.base).sorted(),
                                isLoading = false
                            )
                        }
                        convertAmount()
                    },
                    onFailure = { exception ->
                        _state.update {
                            it.copy(
                                error = "Failed to fetch exchange rates: ${exception.message}",
                                isLoading = false,
                                supportedCurrencies = defaultCurrencies
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = "An error occurred: ${e.message}",
                        isLoading = false,
                        supportedCurrencies = defaultCurrencies
                    )
                }
            }
        }
    }

    fun onAmountChange(amount: String) {
        if (amount.isEmpty() || isValidAmountInput(amount)) {
            _state.update { it.copy(amount = amount) }
            convertAmount()
        }
    }

    private fun isValidAmountInput(input: String): Boolean {
        return try {
            // Allow empty string or a valid decimal number
            input.isEmpty() || input.toDouble() >= 0
        } catch (e: NumberFormatException) {
            false
        }
    }

    fun onFromCurrencyChange(currency: String) {
        if (currency != _state.value.fromCurrency) {
            _state.update { it.copy(fromCurrency = currency) }
            fetchExchangeRates()
        }
    }

    fun onToCurrencyChange(currency: String) {
        if (currency != _state.value.toCurrency) {
            _state.update { it.copy(toCurrency = currency) }
            convertAmount()
        }
    }

    fun swapCurrencies() {
        val fromCurrency = _state.value.fromCurrency
        val toCurrency = _state.value.toCurrency

        _state.update {
            it.copy(
                fromCurrency = toCurrency,
                toCurrency = fromCurrency
            )
        }
        fetchExchangeRates()
    }

    fun refreshRates() {
        fetchExchangeRates()
    }

    private fun convertAmount() {
        val amount = _state.value.amount.toDoubleOrNull() ?: 0.0

        viewModelScope.launch {
            if (_state.value.fromCurrency == _state.value.toCurrency) {
                // Same currency, no conversion needed
                _state.update { it.copy(convertedAmount = amount, conversionRate = 1.0) }
                return@launch
            }

            try {
                val result = convertCurrencyUseCase(amount, _state.value.fromCurrency, _state.value.toCurrency)
                result.fold(
                    onSuccess = { convertedAmount ->
                        val rate = if (amount > 0) convertedAmount / amount else 0.0
                        _state.update {
                            it.copy(
                                convertedAmount = convertedAmount,
                                conversionRate = rate
                            )
                        }
                    },
                    onFailure = { exception ->
                        // If API conversion fails, try to calculate using cached rates
                        calculateConversionWithCachedRates(amount)
                    }
                )
            } catch (e: Exception) {
                calculateConversionWithCachedRates(amount)
            }
        }
    }

    private fun calculateConversionWithCachedRates(amount: Double) {
        val exchangeRate = _state.value.exchangeRate ?: return

        try {
            val rate = when {
                // Direct conversion from base currency
                exchangeRate.base == _state.value.fromCurrency ->
                    exchangeRate.rates[_state.value.toCurrency] ?: 1.0

                // Converting to base currency
                _state.value.toCurrency == exchangeRate.base ->
                    1.0 / (exchangeRate.rates[_state.value.fromCurrency] ?: 1.0)

                // Cross-currency conversion
                else -> {
                    val fromRateToBase = exchangeRate.rates[_state.value.fromCurrency] ?: 1.0
                    val toRateFromBase = exchangeRate.rates[_state.value.toCurrency] ?: 1.0

                    if (fromRateToBase > 0) {
                        toRateFromBase / fromRateToBase
                    } else 1.0
                }
            }

            val convertedAmount = amount * rate
            _state.update {
                it.copy(
                    convertedAmount = convertedAmount,
                    conversionRate = rate
                )
            }
        } catch (e: Exception) {
            _state.update {
                it.copy(error = "Conversion calculation error: ${e.message}")
            }
        }
    }
}

data class CurrencyState(
    val amount: String = "1.00",
    val fromCurrency: String = "USD",
    val toCurrency: String = "EUR",
    val convertedAmount: Double = 0.0,
    val conversionRate: Double = 0.0,
    val exchangeRate: ExchangeRate? = null,
    val supportedCurrencies: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val lastUpdated: Long = System.currentTimeMillis()
)