package com.example.tablettaskoverlay.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity): Long

    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE status = 'OPEN' ORDER BY createdAt ASC")
    fun observeOpenTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE status = 'OPEN' ORDER BY createdAt ASC")
    suspend fun getOpenTasksSnapshot(): List<TaskEntity>

    @Query("UPDATE tasks SET status = 'CLOSED', closedAt = :closedAt WHERE id = :taskId AND status = 'OPEN'")
    suspend fun closeTaskById(taskId: Long, closedAt: Long): Int

    @Query("UPDATE tasks SET status = 'CLOSED', closedAt = :closedAt WHERE text LIKE '%' || :taskText || '%' AND status = 'OPEN'")
    suspend fun closeTaskByText(taskText: String, closedAt: Long): Int

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTask(id: Long): TaskEntity?
}
