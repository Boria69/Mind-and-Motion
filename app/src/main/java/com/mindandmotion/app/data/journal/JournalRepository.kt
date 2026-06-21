package com.mindandmotion.app.data.journal

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Sursă unică de adevăr pentru intrările de journal.
 * Nu cunoaște nimic despre UI — JournalViewModel decide cum afișează datele.
 */
class JournalRepository(private val journalDao: JournalDao) {

    fun observeAllEntries(): Flow<List<JournalEntryEntity>> = journalDao.observeAll()

    fun observeEntryDates(): Flow<List<LocalDate>> = journalDao.observeEntryDates()

    fun observeEntryForDate(date: LocalDate): Flow<JournalEntryEntity?> =
        journalDao.observeByDate(date)

    suspend fun getEntryForDate(date: LocalDate): JournalEntryEntity? =
        journalDao.getByDate(date)

    suspend fun saveEntry(entry: JournalEntryEntity) {
        journalDao.upsert(entry.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteEntry(entry: JournalEntryEntity) {
        journalDao.delete(entry)
    }
}
