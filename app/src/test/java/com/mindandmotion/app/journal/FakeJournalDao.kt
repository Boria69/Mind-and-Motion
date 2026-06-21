package com.mindandmotion.app.journal

import com.mindandmotion.app.data.journal.JournalDao
import com.mindandmotion.app.data.journal.JournalEntryEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/**
 * Fake DAO în memorie, pentru teste unitare — nu necesită Room/Robolectric,
 * rulează pur pe JVM.
 */
class FakeJournalDao : JournalDao {

    private val entries = MutableStateFlow<List<JournalEntryEntity>>(emptyList())

    override fun observeAll() = entries

    override fun observeByDate(date: LocalDate) =
        entries.map { list -> list.firstOrNull { it.date == date } }

    override suspend fun getByDate(date: LocalDate): JournalEntryEntity? =
        entries.value.firstOrNull { it.date == date }

    override fun observeEntryDates() = entries.map { list -> list.map { it.date } }

    override suspend fun upsert(entry: JournalEntryEntity): Long {
        // simulează unique index pe `date`: o intrare nouă pe aceeași zi o înlocuiește pe cea veche
        val withoutSameDate = entries.value.filterNot { it.date == entry.date }
        val id = if (entry.id != 0L) entry.id else (entries.value.maxOfOrNull { it.id } ?: 0L) + 1
        entries.value = withoutSameDate + entry.copy(id = id)
        return id
    }

    override suspend fun delete(entry: JournalEntryEntity) {
        entries.value = entries.value.filterNot { it.id == entry.id }
    }

    override suspend fun deleteById(id: Long) {
        entries.value = entries.value.filterNot { it.id == id }
    }
}
