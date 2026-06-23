package com.mindandmotion.app.data.task

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * Prioritatea unui task. Ordinea declarării (HIGH=0, MEDIUM=1, LOW=2) este și
 * ordinea de sortare cerută în ARCHITECTURE.md.
 *
 * NOTĂ: enum-ul e stocat în DB ca String (prin Converters), deci un simplu
 * `ORDER BY priority` ar sorta alfabetic (HIGH, LOW, MEDIUM). Sortarea corectă
 * după severitate se face cu un CASE în TaskDao.observeAll().
 */
enum class Priority {
    HIGH, MEDIUM, LOW
}

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val priority: Priority = Priority.MEDIUM,
    val dueDate: LocalDate? = null,
    val isDone: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
