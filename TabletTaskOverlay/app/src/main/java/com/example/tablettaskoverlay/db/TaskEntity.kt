package com.example.tablettaskoverlay.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.tablettaskoverlay.data.TaskStatus

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val createdAt: Long,
    val closedAt: Long?,
    val status: TaskStatus
)
