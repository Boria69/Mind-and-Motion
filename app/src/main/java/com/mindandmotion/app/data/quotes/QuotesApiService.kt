package com.mindandmotion.app.data.quotes

import retrofit2.http.GET
import retrofit2.http.Query

interface QuotesApiService {

    /** Un singur citat aleator — folosit ca "citatul zilei" pe InspirationScreen. */
    @GET("quotes/random")
    suspend fun getRandomQuote(): QuoteDto

    /** Listă paginată de citate — folosită pentru lista scrollabilă. */
    @GET("quotes")
    suspend fun getQuotes(
        @Query("limit") limit: Int = 20,
        @Query("skip") skip: Int = 0
    ): QuotesResponseDto
}
