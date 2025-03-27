// data/remote/service/ExchangeRateService.kt
package com.example.moflow.data.remote.service

import com.example.moflow.data.remote.api.ExchangeRateApi
import com.example.moflow.data.remote.dto.ExchangeRateDto
import javax.inject.Inject

class ExchangeRateService @Inject constructor(
    private val api: ExchangeRateApi
) {
    suspend fun getLatestRates(baseCurrency: String): ExchangeRateDto {
        return api.getLatestRates(baseCurrency)
    }
}