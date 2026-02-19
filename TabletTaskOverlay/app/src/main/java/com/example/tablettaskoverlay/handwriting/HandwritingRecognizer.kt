package com.example.tablettaskoverlay.handwriting

import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModelManager
import android.content.Context
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.vision.digitalink.DigitalInkRecognition
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModel
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModelIdentifier
import com.google.mlkit.vision.digitalink.Ink
import com.google.mlkit.vision.digitalink.RecognitionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class HandwritingRecognizer(context: Context) {
    private val modelIdentifier = DigitalInkRecognitionModelIdentifier.fromLanguageTag("en-US")
        ?: throw IllegalStateException("No en-US handwriting model available")

    private val model = DigitalInkRecognitionModel.builder(modelIdentifier).build()
    private val recognizer = DigitalInkRecognition.getClient(
        com.google.mlkit.vision.digitalink.DigitalInkRecognizerOptions.builder(model).build()
    )
    private val modelManager = DigitalInkRecognitionModelManager.getInstance()

    init {
        recognizer
    }

    suspend fun ensureModelDownloaded() = withContext(Dispatchers.IO) {
        val isDownloaded = modelManager.isModelDownloaded(model).await()
        if (!isDownloaded) {
            // Model download is one-time and cached by ML Kit on device.
            modelManager.download(model, DownloadConditions.Builder().build()).await()
        }
    }

    suspend fun recognize(strokes: List<List<Pair<Float, Float>>>): String = withContext(Dispatchers.Default) {
        ensureModelDownloaded()

        val inkBuilder = Ink.builder()
        strokes.forEach { points ->
            if (points.size < 2) return@forEach
            val stroke = Ink.Stroke.builder()
            // ML Kit expects time-ordered points; synthetic timing is sufficient for recognition.
            var time = System.currentTimeMillis()
            points.forEach { (x, y) ->
                stroke.addPoint(Ink.Point.create(x, y, time))
                time += 16
            }
            inkBuilder.addStroke(stroke.build())
        }

        val recognitionResult: RecognitionResult = recognizer.recognize(inkBuilder.build()).await()
        recognitionResult.candidates.firstOrNull()?.text.orEmpty()
    }
}
