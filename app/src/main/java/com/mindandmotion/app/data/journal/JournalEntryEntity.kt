package com.mindandmotion.app.data.journal

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

enum class Mood {
    GREAT, GOOD, OKAY, BAD
}

@Entity(
    tableName = "journal_entries",
    indices = [Index(value = ["date"], unique = true)]
)
data class JournalEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: LocalDate,
    val mood: Mood? = null,
    val content: String,
    val updatedAt: Long = System.currentTimeMillis()
)
