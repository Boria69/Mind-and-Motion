package com.mindandmotion.app.data.task

import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {

    fun observeTasks(): Flow<List<TaskEntity>> = taskDao.observeAll()

    fun observeTask(id: Long): Flow<TaskEntity?> = taskDao.observeById(id)

    suspend fun getTask(id: Long): TaskEntity? = taskDao.getById(id)

    suspend fun upsert(task: TaskEntity): Long = taskDao.upsert(task)

    suspend fun setDone(id: Long, isDone: Boolean) = taskDao.setDone(id, isDone)

    suspend fun delete(task: TaskEntity) = taskDao.delete(task)
}
