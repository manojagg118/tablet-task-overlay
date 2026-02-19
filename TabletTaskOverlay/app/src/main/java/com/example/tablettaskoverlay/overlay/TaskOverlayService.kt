package com.example.tablettaskoverlay.overlay

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import com.example.tablettaskoverlay.TaskOverlayApp
import com.example.tablettaskoverlay.db.TaskEntity
import com.example.tablettaskoverlay.ui.theme.TabletTaskOverlayTheme
import com.example.tablettaskoverlay.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TaskOverlayService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val openTasksState = MutableStateFlow<List<TaskEntity>>(emptyList())

    private lateinit var windowManager: WindowManager
    private var composeView: ComposeView? = null

    override fun onCreate() {
        super.onCreate()
        // Foreground service keeps process alive for persistent overlay behavior.
        startForeground(
            NotificationHelper.OVERLAY_NOTIFICATION_ID,
            NotificationHelper.buildOverlayServiceNotification(this)
        )

        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createOverlayView()

        val repo = (application as TaskOverlayApp).repository
        scope.launch {
            repo.observeOpenTasks().collect {
                openTasksState.value = it
            }
        }
    }

    private fun createOverlayView() {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP
        }

        composeView = ComposeView(this).apply {
            setContent {
                TabletTaskOverlayTheme {
                    val openTasks by openTasksState.asStateFlow().collectAsState()
                    OverlayTaskPanel(openTasks)
                }
            }
        }

        windowManager.addView(composeView, params)
    }

    override fun onDestroy() {
        composeView?.let { windowManager.removeView(it) }
        composeView = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

@androidx.compose.runtime.Composable
private fun OverlayTaskPanel(openTasks: List<TaskEntity>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.background(Color(0xEE101820)).padding(12.dp)) {
            Text("Open tasks", color = Color.White, style = MaterialTheme.typography.titleLarge)
            LazyColumn {
                items(openTasks.take(6)) { task ->
                    Text(
                        text = "• ${task.id}. ${task.text}",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }
    }
}
