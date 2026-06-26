package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class QuotableQuote(
    @Json(name = "_id") val id: String?,
    @Json(name = "content") val content: String,
    @Json(name = "author") val author: String,
    @Json(name = "tags") val tags: List<String>?
)

interface QuotableApi {
    @GET("random")
    suspend fun getRandomQuote(
        @Query("tags") tags: String? = null
    ): QuotableQuote
}
