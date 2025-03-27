// data/remote/api/ExchangeRateApi.kt
package com.example.moflow.data.remote.api

import com.example.moflow.data.remote.dto.ExchangeRateDto
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Path

interface ExchangeRateApi {
    @GET("latest/{baseCurrency}")
    suspend fun getLatestRates(
        @Path("baseCurrency") baseCurrency: String
    ): ExchangeRateDto
}