package com.mindandmotion.app.ui.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mindandmotion.app.data.journal.JournalEntryEntity
import com.mindandmotion.app.data.journal.JournalRepository
import com.mindandmotion.app.data.journal.Mood
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

data class JournalUiState(
    val visibleMonth: YearMonth = YearMonth.now(),
    val markedDates: Set<LocalDate> = emptySet(),
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedEntry: JournalEntryEntity? = null,
    val draftContent: String = "",
    val draftMood: Mood? = null
)

class JournalViewModel(
    private val repository: JournalRepository
) : ViewModel() {

    private val visibleMonth = MutableStateFlow(YearMonth.now())
    private val selectedDate = MutableStateFlow(LocalDate.now())
    private val draftContent = MutableStateFlow("")
    private val draftMood = MutableStateFlow<Mood?>(null)

    private val markedDates: StateFlow<Set<LocalDate>> = repository.observeEntryDates()
        .map { it.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    private val selectedEntry: StateFlow<JournalEntryEntity?> = selectedDate
        .flatMapLatest { date -> repository.observeEntryForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val calendarState = combine(visibleMonth, markedDates) { month, marked ->
        month to marked
    }

    private val draftState = combine(selectedDate, draftContent, draftMood) { date, content, mood ->
        Triple(date, content, mood)
    }

    val uiState: StateFlow<JournalUiState> = combine(
        calendarState, draftState, selectedEntry
    ) { (month, marked), (date, content, mood), entry ->
        JournalUiState(
            visibleMonth = month,
            markedDates = marked,
            selectedDate = date,
            selectedEntry = entry,
            draftContent = content,
            draftMood = mood
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), JournalUiState())

    fun onPreviousMonth() {
        visibleMonth.value = visibleMonth.value.minusMonths(1)
    }

    fun onNextMonth() {
        visibleMonth.value = visibleMonth.value.plusMonths(1)
    }

    fun onDateSelected(date: LocalDate) {
        selectedDate.value = date
        viewModelScope.launch {
            val existing = repository.getEntryForDate(date)
            draftContent.value = existing?.content ?: ""
            draftMood.value = existing?.mood
        }
    }

    fun onContentChanged(text: String) {
        draftContent.value = text
    }

    fun onMoodChanged(mood: Mood?) {
        draftMood.value = mood
    }

    fun onSaveEntry() {
        val content = draftContent.value
        if (content.isBlank()) return
        val date = selectedDate.value
        val existing = selectedEntry.value
        viewModelScope.launch {
            repository.saveEntry(
                JournalEntryEntity(
                    id = existing?.id ?: 0,
                    date = date,
                    mood = draftMood.value,
                    content = content
                )
            )
        }
    }

    fun onDeleteEntry() {
        val existing = selectedEntry.value ?: return
        viewModelScope.launch {
            repository.deleteEntry(existing)
            draftContent.value = ""
            draftMood.value = null
        }
    }
}

/**
 * Factory manuală (fără Hilt, conform ARCHITECTURE.md) — instanțiată din AppContainer ([TU]).
 */
class JournalViewModelFactory(
    private val repository: JournalRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JournalViewModel::class.java)) {
            return JournalViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
    }
}
