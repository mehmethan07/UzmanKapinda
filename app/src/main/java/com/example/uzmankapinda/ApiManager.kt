package com.example.uzmankapinda

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// API'den dönen kur verisini karşılayan model
data class ExchangeResponse(
    val rates: Map<String, Double>
)

interface ExchangeApi {

    @GET("latest")
    suspend fun getRates(
        @Query("from") from: String = "TRY",
        @Query("to") to: String = "USD"
    ): ExchangeResponse
}

object ApiManager {
    private const val BASE_URL = "https://api.frankfurter.app/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val exchangeService: ExchangeApi = retrofit.create(ExchangeApi::class.java)
}