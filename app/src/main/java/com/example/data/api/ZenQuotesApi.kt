package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET

@JsonClass(generateAdapter = true)
data class ZenQuote(
    @Json(name = "q") val q: String,
    @Json(name = "a") val a: String
)

interface ZenQuotesApi {
    @GET("random")
    suspend fun getRandomQuote(): List<ZenQuote>
}
