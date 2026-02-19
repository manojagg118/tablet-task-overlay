package com.example.tablettaskoverlay.voice

sealed class VoiceCommand {
    data object WriteTask : VoiceCommand()
    data class CloseById(val id: Long) : VoiceCommand()
    data class CloseByName(val name: String) : VoiceCommand()
    data object Unknown : VoiceCommand()
}
