package com.mindandmotion.app.data.task

import kotlinx.coroutines.flow.Flow

/**
 * Sursă unică de adevăr pentru task-uri (MM-11, [TU]).
 * Întoarce Flow-uri din DAO și nu cunoaște nimic despre UI.
 */
class TaskRepository(private val taskDao: TaskDao) {

    fun observeTasks(): Flow<List<TaskEntity>> = taskDao.observeAll()

    fun observeTask(id: Long): Flow<TaskEntity?> = taskDao.observeById(id)

    suspend fun getTask(id: Long): TaskEntity? = taskDao.getById(id)

    suspend fun upsert(task: TaskEntity): Long = taskDao.upsert(task)

    suspend fun setDone(id: Long, isDone: Boolean) = taskDao.setDone(id, isDone)

    suspend fun delete(task: TaskEntity) = taskDao.delete(task)
}
