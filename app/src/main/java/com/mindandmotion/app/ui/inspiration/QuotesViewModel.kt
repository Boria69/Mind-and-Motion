package com.mindandmotion.app.ui.inspiration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mindandmotion.app.data.quotes.QuoteDto
import com.mindandmotion.app.data.quotes.QuotesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

data class QuotesUiState(
    val isLoading: Boolean = false,
    val featuredQuote: QuoteDto? = null,
    val quotes: List<QuoteDto> = emptyList(),
    val errorMessage: String? = null
)

class QuotesViewModel(
    private val repository: QuotesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuotesUiState(isLoading = true))
    val uiState: StateFlow<QuotesUiState> = _uiState.asStateFlow()

    init {
        loadQuotes()
    }

    fun onRefresh() {
        loadQuotes()
    }

    private fun loadQuotes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            // citat random pentru "Citatul zilei" — best-effort, nu blocăm ecranul dacă pică doar acesta
            val featured = repository.getRandomQuote().getOrNull()

            // skip aleator într-o fereastră fixă, ca "Reîncarcă" să arate cu adevărat alte citate,
            // nu aceeași primă pagină de fiecare dată
            repository.getQuotes(limit = 20, skip = Random.nextInt(0, 200))
                .onSuccess { quotes ->
                    _uiState.value = QuotesUiState(
                        isLoading = false,
                        featuredQuote = featured,
                        quotes = quotes
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Nu am putut încărca citatele. Verifică conexiunea și încearcă din nou."
                    )
                }
        }
    }
}

class QuotesViewModelFactory(
    private val repository: QuotesRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuotesViewModel::class.java)) {
            return QuotesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
    }
}
