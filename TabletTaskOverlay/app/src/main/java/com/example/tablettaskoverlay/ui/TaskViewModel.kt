package com.example.tablettaskoverlay.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tablettaskoverlay.BuildConfig
import com.example.tablettaskoverlay.TaskOverlayApp
import com.example.tablettaskoverlay.data.TaskStatus
import com.example.tablettaskoverlay.db.TaskEntity
import com.example.tablettaskoverlay.repo.TaskRepository
import com.example.tablettaskoverlay.voice.VoiceCommand
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TaskRepository = (application as TaskOverlayApp).repository

    private val localState = MutableStateFlow(TaskUiState())

    val uiState: StateFlow<TaskUiState> = combine(
        repository.observeAllTasks(),
        repository.observeOpenTasks(),
        localState
    ) { allTasks, openTasks, local ->
        local.copy(tasks = allTasks, openTasks = openTasks)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TaskUiState())

    init {
        if (BuildConfig.DEBUG) {
            viewModelScope.launch {
                repository.seedFakeTasksIfNeeded()
            }
        }
    }

    fun onWriteTaskTapped() {
        localState.update { it.copy(showHandwritingDialog = true) }
    }

    fun onHandwritingDismiss() {
        localState.update { it.copy(showHandwritingDialog = false) }
    }

    fun addTask(text: String) {
        if (text.trim().isEmpty()) return
        viewModelScope.launch {
            repository.addTask(text)
            localState.update {
                it.copy(
                    showHandwritingDialog = false,
                    lastMessage = "Task created at ${System.currentTimeMillis()}"
                )
            }
        }
    }

    fun closeTask(taskId: Long) {
        viewModelScope.launch {
            val closed = repository.closeTaskById(taskId)
            localState.update {
                it.copy(lastMessage = if (closed) "Task closed" else "Task not found")
            }
        }
    }

    fun closeTaskByName(name: String) {
        viewModelScope.launch {
            val closed = repository.closeTaskByName(name)
            localState.update {
                it.copy(lastMessage = if (closed) "Matching task closed" else "No matching open task")
            }
        }
    }

    fun setVoiceTranscript(transcript: String) {
        localState.update { it.copy(voiceTranscript = transcript) }
    }

    fun handleVoiceCommand(command: VoiceCommand) {
        when (command) {
            VoiceCommand.WriteTask -> onWriteTaskTapped()
            is VoiceCommand.CloseById -> closeTask(command.id)
            is VoiceCommand.CloseByName -> closeTaskByName(command.name)
            VoiceCommand.Unknown -> localState.update { it.copy(lastMessage = "Voice command not recognized") }
        }
    }

    fun openTasksWithStatus(): List<TaskEntity> {
        return uiState.value.tasks.filter { it.status == TaskStatus.OPEN }
    }

    fun setMessage(message: String) {
        localState.update { it.copy(lastMessage = message) }
    }
}
