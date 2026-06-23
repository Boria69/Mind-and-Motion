package com.mindandmotion.app.data.task

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query(
        """
        SELECT * FROM tasks
        ORDER BY
            isDone ASC,
            CASE priority
                WHEN 'HIGH' THEN 0
                WHEN 'MEDIUM' THEN 1
                WHEN 'LOW' THEN 2
                ELSE 3
            END ASC,
            dueDate IS NULL ASC,
            dueDate ASC,
            createdAt ASC
        """
    )
    fun observeAll(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<TaskEntity?>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): TaskEntity?

    @Upsert
    suspend fun upsert(task: TaskEntity): Long

    @Query("UPDATE tasks SET isDone = :isDone WHERE id = :id")
    suspend fun setDone(id: Long, isDone: Boolean)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: Long)
}
