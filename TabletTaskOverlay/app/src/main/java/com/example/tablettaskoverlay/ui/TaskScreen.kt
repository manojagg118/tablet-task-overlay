package com.example.tablettaskoverlay.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tablettaskoverlay.db.TaskEntity
import com.example.tablettaskoverlay.handwriting.HandwritingRecognizer
import com.example.tablettaskoverlay.util.DateTimeFormatterUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun TaskScreen(
    uiState: TaskUiState,
    onWriteTask: () -> Unit,
    onCloseTask: (Long) -> Unit,
    onStartVoice: () -> Unit,
    onStopVoice: () -> Unit,
    isVoiceListening: Boolean,
    isDebugMode: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(2f)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Open Tasks (${uiState.openTasks.size})",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(uiState.openTasks, key = { it.id }) { task ->
                    TaskSwipeCard(task = task, onClose = { onCloseTask(task.id) })
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Controls", fontSize = 24.sp, fontWeight = FontWeight.SemiBold)

            Button(
                onClick = onWriteTask,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Text("Write a task", fontSize = 20.sp, modifier = Modifier.padding(start = 8.dp))
            }

            if (isVoiceListening) {
                OutlinedButton(
                    onClick = onStopVoice,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                ) {
                    Icon(Icons.Default.Mic, contentDescription = null)
                    Text("Stop voice", fontSize = 20.sp, modifier = Modifier.padding(start = 8.dp))
                }
            } else {
                Button(
                    onClick = onStartVoice,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                ) {
                    Icon(Icons.Default.Mic, contentDescription = null)
                    Text("Voice command", fontSize = 20.sp, modifier = Modifier.padding(start = 8.dp))
                }
            }

            Text(
                text = "Say: 'Write a task' or 'Close task 2'",
                style = MaterialTheme.typography.bodyLarge
            )
            if (uiState.voiceTranscript.isNotBlank()) {
                Text("Heard: ${uiState.voiceTranscript}", style = MaterialTheme.typography.bodyLarge)
            }
            if (uiState.lastMessage.isNotBlank()) {
                Text(uiState.lastMessage, color = MaterialTheme.colorScheme.secondary)
            }
            if (isDebugMode) {
                Text("Debug mode: lock-screen strict permissions are optional.", color = Color(0xFFB26A00))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskSwipeCard(task: TaskEntity, onClose: () -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart || value == SwipeToDismissBoxValue.StartToEnd) {
                onClose()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1B5E20))
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text("Close", color = Color.White, fontSize = 18.sp)
            }
        }
    ) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("#${task.id} ${task.text}", fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                Text("Created: ${DateTimeFormatterUtil.format(task.createdAt)}", fontSize = 16.sp)
                Text("Status: ${task.status}", fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun HandwritingDialog(
    recognizer: HandwritingRecognizer,
    onDismiss: () -> Unit,
    onRecognizedText: (String) -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        var recognizing by remember { mutableStateOf(false) }
        val strokes = remember { mutableStateListOf<MutableList<Offset>>() }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(520.dp)
                .padding(12.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                Text("Handwrite task", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("Use stylus/pen. Tap Save to convert handwriting to text.")

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.White)
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    strokes.add(mutableListOf(offset))
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    strokes.lastOrNull()?.add(change.position)
                                }
                            )
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        strokes.forEach { strokePoints ->
                            val path = Path()
                            strokePoints.firstOrNull()?.let { first ->
                                path.moveTo(first.x, first.y)
                                strokePoints.drop(1).forEach { point ->
                                    path.lineTo(point.x, point.y)
                                }
                                drawPath(path = path, color = Color.Black, style = Stroke(width = 6f))
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = {
                        strokes.clear()
                    }, modifier = Modifier.weight(1f)) {
                        Text("Clear")
                    }
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        enabled = !recognizing,
                        onClick = {
                            recognizing = true
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (recognizing) "Recognizing..." else "Save")
                    }
                }
            }
        }

        LaunchedEffect(recognizing) {
            if (!recognizing) return@LaunchedEffect
            val result = withContext(Dispatchers.Default) {
                runCatching {
                    val mapped = strokes.map { stroke -> stroke.map { it.x to it.y } }
                    recognizer.recognize(mapped)
                }.getOrDefault("")
            }
            if (result.isNotBlank()) {
                onRecognizedText(result)
            }
            onDismiss()
        }
    }
}
