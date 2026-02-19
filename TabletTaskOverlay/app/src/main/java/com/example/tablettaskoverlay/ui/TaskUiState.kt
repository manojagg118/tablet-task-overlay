package com.example.tablettaskoverlay.ui

import com.example.tablettaskoverlay.db.TaskEntity

data class TaskUiState(
    val tasks: List<TaskEntity> = emptyList(),
    val openTasks: List<TaskEntity> = emptyList(),
    val voiceTranscript: String = "",
    val showHandwritingDialog: Boolean = false,
    val lastMessage: String = ""
)
