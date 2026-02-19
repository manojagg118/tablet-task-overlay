package com.example.tablettaskoverlay.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import com.example.tablettaskoverlay.BuildConfig
import com.example.tablettaskoverlay.handwriting.HandwritingRecognizer
import com.example.tablettaskoverlay.ui.theme.TabletTaskOverlayTheme
import com.example.tablettaskoverlay.voice.VoiceCommandManager

class LockScreenTaskActivity : ComponentActivity() {
    private val viewModel: TaskViewModel by viewModels()
    private lateinit var handwritingRecognizer: HandwritingRecognizer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        enableEdgeToEdge()

        handwritingRecognizer = HandwritingRecognizer(this)

        setContent {
            TabletTaskOverlayTheme {
                val state by viewModel.uiState.collectAsState()
                val voiceManager = remember {
                    VoiceCommandManager(
                        context = this@LockScreenTaskActivity,
                        onCommand = viewModel::handleVoiceCommand,
                        onRawText = viewModel::setVoiceTranscript
                    )
                }
                val listening by voiceManager.isListening.collectAsState()
                DisposableEffect(Unit) {
                    onDispose { voiceManager.release() }
                }
                TaskScreen(
                    uiState = state,
                    onWriteTask = viewModel::onWriteTaskTapped,
                    onCloseTask = viewModel::closeTask,
                    onStartVoice = {
                        if (ContextCompat.checkSelfPermission(
                                this@LockScreenTaskActivity,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            voiceManager.startListening()
                        } else {
                            viewModel.setMessage("Microphone permission is required for voice commands.")
                        }
                    },
                    onStopVoice = { voiceManager.stopListening() },
                    isVoiceListening = listening,
                    isDebugMode = !BuildConfig.ENABLE_LOCKSCREEN_STRICT
                )
                if (state.showHandwritingDialog) {
                    HandwritingDialog(
                        recognizer = handwritingRecognizer,
                        onDismiss = viewModel::onHandwritingDismiss,
                        onRecognizedText = viewModel::addTask
                    )
                }
            }
        }
    }
}
