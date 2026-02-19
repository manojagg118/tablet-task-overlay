package com.example.tablettaskoverlay.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.content.ContextCompat
import com.example.tablettaskoverlay.BuildConfig
import com.example.tablettaskoverlay.handwriting.HandwritingRecognizer
import com.example.tablettaskoverlay.overlay.TaskOverlayService
import com.example.tablettaskoverlay.ui.theme.TabletTaskOverlayTheme
import com.example.tablettaskoverlay.util.ExportManager
import com.example.tablettaskoverlay.util.PermissionHelper
import com.example.tablettaskoverlay.voice.VoiceCommandManager
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: TaskViewModel by viewModels()
    private lateinit var handwritingRecognizer: HandwritingRecognizer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handwritingRecognizer = HandwritingRecognizer(this)

        setContent {
            TabletTaskOverlayTheme {
                val state by viewModel.uiState.collectAsState()

                val micPermissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { }
                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { }

                val voiceManager = remember {
                    VoiceCommandManager(
                        context = this@MainActivity,
                        onCommand = viewModel::handleVoiceCommand,
                        onRawText = viewModel::setVoiceTranscript
                    )
                }
                val listening by voiceManager.isListening.collectAsState()
                var notificationAsked by remember { mutableStateOf(false) }
                val scope = rememberCoroutineScope()

                DisposableEffect(Unit) {
                    onDispose { voiceManager.release() }
                }

                Scaffold(
                    topBar = {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(onClick = {
                                if (!PermissionHelper.canDrawOverlays(this@MainActivity)) {
                                    startActivity(PermissionHelper.overlayPermissionIntent(this@MainActivity))
                                } else {
                                    ContextCompat.startForegroundService(
                                        this@MainActivity,
                                        Intent(this@MainActivity, TaskOverlayService::class.java)
                                    )
                                }
                            }) {
                                Text("Start Overlay")
                            }

                            Button(onClick = {
                                startActivity(Intent(this@MainActivity, LockScreenTaskActivity::class.java))
                            }) {
                                Text("Lock Screen Mode")
                            }

                            Button(onClick = {
                                stopService(Intent(this@MainActivity, TaskOverlayService::class.java))
                            }) {
                                Text("Stop Overlay")
                            }

                            Button(onClick = {
                                scope.launch {
                                    val file = ExportManager.exportTasksToJson(
                                        this@MainActivity,
                                        state.tasks
                                    )
                                    val uri = FileProvider.getUriForFile(
                                        this@MainActivity,
                                        "${BuildConfig.APPLICATION_ID}.provider",
                                        file
                                    )
                                    val share = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/json"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    startActivity(Intent.createChooser(share, "Export tasks JSON"))
                                }
                            }) {
                                Text("Export JSON")
                            }
                        }
                    }
                ) { padding ->
                    TaskScreen(
                        uiState = state,
                        onWriteTask = viewModel::onWriteTaskTapped,
                        onCloseTask = viewModel::closeTask,
                        onStartVoice = {
                            if (ContextCompat.checkSelfPermission(
                                    this@MainActivity,
                                    Manifest.permission.RECORD_AUDIO
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                micPermissionLauncher.launch(PermissionHelper.micPermission())
                            } else {
                                voiceManager.startListening()
                            }
                        },
                        onStopVoice = { voiceManager.stopListening() },
                        isVoiceListening = listening,
                        isDebugMode = !BuildConfig.ENABLE_LOCKSCREEN_STRICT,
                        modifier = Modifier.padding(padding)
                    )
                }

                LaunchedEffect(Unit) {
                    if (!notificationAsked &&
                        PermissionHelper.requiresNotificationPermission() &&
                        ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            PermissionHelper.notificationPermission()
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        notificationAsked = true
                        notificationPermissionLauncher.launch(PermissionHelper.notificationPermission())
                    }
                }

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
