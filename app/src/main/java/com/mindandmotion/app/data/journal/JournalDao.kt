package com.mindandmotion.app.data.journal

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface JournalDao {

    @Query("SELECT * FROM journal_entries ORDER BY date DESC")
    fun observeAll(): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entries WHERE date = :date LIMIT 1")
    fun observeByDate(date: LocalDate): Flow<JournalEntryEntity?>

    @Query("SELECT * FROM journal_entries WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: LocalDate): JournalEntryEntity?

    @Query("SELECT date FROM journal_entries")
    fun observeEntryDates(): Flow<List<LocalDate>>

    // unique index pe `date` => un INSERT cu REPLACE pe o zi care are deja o intrare
    // o suprascrie automat, chiar dacă id-ul diferă. E exact comportamentul de
    // "o intrare pe zi" cerut în ARCHITECTURE.md.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: JournalEntryEntity): Long

    @Delete
    suspend fun delete(entry: JournalEntryEntity)

    @Query("DELETE FROM journal_entries WHERE id = :id")
    suspend fun deleteById(id: Long)
}
