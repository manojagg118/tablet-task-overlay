package com.example.tablettaskoverlay.db

import androidx.room.TypeConverter
import com.example.tablettaskoverlay.data.TaskStatus

class RoomConverters {
    @TypeConverter
    fun fromStatus(status: TaskStatus): String = status.name

    @TypeConverter
    fun toStatus(value: String): TaskStatus = TaskStatus.valueOf(value)
}
