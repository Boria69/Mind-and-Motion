package com.mindandmotion.app.data.quotes

/** Răspuns pentru GET quotes/random */
data class QuoteDto(
    val id: Int,
    val quote: String,
    val author: String
)

/** Răspuns pentru GET quotes?limit=&skip= */
data class QuotesResponseDto(
    val quotes: List<QuoteDto>,
    val total: Int,
    val skip: Int,
    val limit: Int
)
