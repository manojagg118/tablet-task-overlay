package com.example.tablettaskoverlay.repo

import com.example.tablettaskoverlay.data.TaskStatus
import com.example.tablettaskoverlay.db.TaskDao
import com.example.tablettaskoverlay.db.TaskEntity
import kotlinx.coroutines.flow.Flow

class TaskRepository(
    private val dao: TaskDao
) {
    fun observeAllTasks(): Flow<List<TaskEntity>> = dao.observeAll()
    fun observeOpenTasks(): Flow<List<TaskEntity>> = dao.observeOpenTasks()

    suspend fun getOpenTasksSnapshot(): List<TaskEntity> = dao.getOpenTasksSnapshot()

    suspend fun addTask(text: String, createdAt: Long = System.currentTimeMillis()): Long {
        return dao.insert(
            TaskEntity(
                text = text.trim(),
                createdAt = createdAt,
                closedAt = null,
                status = TaskStatus.OPEN
            )
        )
    }

    suspend fun closeTaskById(taskId: Long, closedAt: Long = System.currentTimeMillis()): Boolean {
        return dao.closeTaskById(taskId, closedAt) > 0
    }

    suspend fun closeTaskByName(query: String, closedAt: Long = System.currentTimeMillis()): Boolean {
        return dao.closeTaskByText(query.trim(), closedAt) > 0
    }

    suspend fun seedFakeTasksIfNeeded() {
        if (dao.getOpenTasksSnapshot().isNotEmpty()) return
        addTask("Review sprint backlog")
        addTask("Send purchase request")
        addTask("Call vendor for stylus samples")
    }
}
