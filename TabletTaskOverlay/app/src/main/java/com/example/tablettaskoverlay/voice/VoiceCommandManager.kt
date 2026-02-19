package com.example.tablettaskoverlay.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class VoiceCommandManager(
    private val context: Context,
    private val onCommand: (VoiceCommand) -> Unit,
    private val onRawText: (String) -> Unit
) : RecognitionListener {

    private var recognizer: SpeechRecognizer? = null
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) return

        if (recognizer == null) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(this@VoiceCommandManager)
            }
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
        }

        _isListening.value = true
        recognizer?.startListening(intent)
    }

    fun stopListening() {
        recognizer?.stopListening()
        _isListening.value = false
    }

    fun release() {
        recognizer?.destroy()
        recognizer = null
        _isListening.value = false
    }

    override fun onReadyForSpeech(params: Bundle?) = Unit
    override fun onBeginningOfSpeech() = Unit
    override fun onRmsChanged(rmsdB: Float) = Unit
    override fun onBufferReceived(buffer: ByteArray?) = Unit
    override fun onEndOfSpeech() = Unit
    override fun onEvent(eventType: Int, params: Bundle?) = Unit

    override fun onError(error: Int) {
        _isListening.value = false
    }

    override fun onResults(results: Bundle?) {
        _isListening.value = false
        val best = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull() ?: return
        onRawText(best)
        onCommand(VoiceCommandParser.parse(best))
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val partial = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull() ?: return
        onRawText(partial)
    }
}
