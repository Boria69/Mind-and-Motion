package com.mindandmotion.app.journal

import com.mindandmotion.app.data.journal.JournalRepository
import com.mindandmotion.app.data.journal.Mood
import com.mindandmotion.app.ui.journal.JournalViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNull

class JournalViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var dao: FakeJournalDao
    private lateinit var repository: JournalRepository
    private lateinit var viewModel: JournalViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        dao = FakeJournalDao()
        repository = JournalRepository(dao)
        viewModel = JournalViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `selecting a date with no entry leaves draft empty`() = runTest {
        viewModel.onDateSelected(LocalDate.of(2026, 6, 21))
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("", state.draftContent)
        assertNull(state.selectedEntry)
    }

    @Test
    fun `saving an entry persists content and mood for that date`() = runTest {
        val date = LocalDate.of(2026, 6, 21)
        viewModel.onDateSelected(date)
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.onContentChanged("Zi bună de lucru")
        viewModel.onMoodChanged(Mood.GOOD)
        viewModel.onSaveEntry()
        dispatcher.scheduler.advanceUntilIdle()

        val saved = repository.getEntryForDate(date)
        assertEquals("Zi bună de lucru", saved?.content)
        assertEquals(Mood.GOOD, saved?.mood)
    }

    @Test
    fun `deleting the selected entry removes it from the repository`() = runTest {
        val date = LocalDate.of(2026, 6, 21)
        viewModel.onDateSelected(date)
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.onContentChanged("De șters")
        viewModel.onSaveEntry()
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.onDeleteEntry()
        dispatcher.scheduler.advanceUntilIdle()

        assertNull(repository.getEntryForDate(date))
    }
}
