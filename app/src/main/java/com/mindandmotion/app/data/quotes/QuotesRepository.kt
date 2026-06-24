package com.mindandmotion.app.data.quotes

/**
 * Sursă unică de adevăr pentru citate. Nu cunoaște nimic despre UI.
 *
 * Cache opțional (Room) — nu e implementat în v1; ar fi util ca fallback când
 * dispozitivul e offline (salvezi ultima listă reușită și o arăți cu un banner
 * "date din cache"). Lăsat intenționat ca follow-up, ca să nu complicăm Epic 6
 * cu o migrare nouă de schemă Room chiar acum.
 */
class QuotesRepository(private val api: QuotesApiService) {

    suspend fun getRandomQuote(): Result<QuoteDto> = runCatching {
        api.getRandomQuote()
    }

    suspend fun getQuotes(limit: Int = 20, skip: Int = 0): Result<List<QuoteDto>> = runCatching {
        api.getQuotes(limit = limit, skip = skip).quotes
    }
}
