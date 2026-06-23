package com.mindandmotion.app.data.task

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

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
