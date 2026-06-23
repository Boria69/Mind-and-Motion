package com.mindandmotion.app.data.task

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    /**
     * Lista completă, în ordinea cerută de ARCHITECTURE.md:
     *  1. task-urile nefinalizate înaintea celor bifate (isDone ASC),
     *  2. după severitatea priorității (HIGH > MEDIUM > LOW) — folosim un CASE
     *     pentru că enum-ul e stocat ca String și un `ORDER BY priority` ar
     *     sorta alfabetic (greșit),
     *  3. după deadline crescător, cu task-urile fără dată scadentă la final,
     *  4. createdAt ca tiebreaker stabil.
     */
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

    /** Insert pentru id = 0 (autoGenerate) sau update pentru un id existent. */
    @Upsert
    suspend fun upsert(task: TaskEntity): Long

    /** Bifare/debifare rapidă din listă, fără a reîncărca tot task-ul. */
    @Query("UPDATE tasks SET isDone = :isDone WHERE id = :id")
    suspend fun setDone(id: Long, isDone: Boolean)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: Long)
}
