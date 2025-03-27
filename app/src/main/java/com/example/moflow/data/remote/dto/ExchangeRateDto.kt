// data/remote/dto/ExchangeRateDto.kt
package com.example.moflow.data.remote.dto

import com.example.moflow.domain.model.ExchangeRate
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ExchangeRateDto(
    @Json(name = "result")
    val result: String,

    @Json(name = "documentation")
    val documentation: String,

    @Json(name = "terms_of_use")
    val termsOfUse: String,

    @Json(name = "time_last_update_unix")
    val timeLastUpdateUnix: Long,

    @Json(name = "time_last_update_utc")
    val timeLastUpdateUtc: String,

    @Json(name = "time_next_update_unix")
    val timeNextUpdateUnix: Long,

    @Json(name = "time_next_update_utc")
    val timeNextUpdateUtc: String,

    @Json(name = "base_code")
    val baseCode: String,

    @Json(name = "conversion_rates")
    val conversionRates: Map<String, Double>
) {
    fun toExchangeRate(): ExchangeRate {
        return ExchangeRate(
            base = baseCode,
            timestamp = timeLastUpdateUnix,
            rates = conversionRates
        )
    }
}