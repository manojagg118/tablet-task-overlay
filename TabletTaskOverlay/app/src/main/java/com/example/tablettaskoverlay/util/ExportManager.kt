package com.example.tablettaskoverlay.util

import android.content.Context
import com.example.tablettaskoverlay.db.TaskEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object ExportManager {
    suspend fun exportTasksToJson(context: Context, tasks: List<TaskEntity>): File = withContext(Dispatchers.IO) {
        val arr = JSONArray()
        tasks.forEach { task ->
            arr.put(
                JSONObject().apply {
                    put("id", task.id)
                    put("text", task.text)
                    put("created_at", task.createdAt)
                    put("closed_at", task.closedAt)
                    put("status", task.status.name)
                }
            )
        }

        val file = File(context.cacheDir, "tasks_export.json")
        file.writeText(arr.toString(2))
        file
    }
}
