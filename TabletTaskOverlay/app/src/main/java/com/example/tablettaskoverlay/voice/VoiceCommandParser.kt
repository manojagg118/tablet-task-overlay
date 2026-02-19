package com.example.tablettaskoverlay.voice

object VoiceCommandParser {
    fun parse(raw: String): VoiceCommand {
        val text = raw.trim().lowercase()
        if (text.contains("write a task") || text.contains("write task")) {
            return VoiceCommand.WriteTask
        }

        if (text.startsWith("close task")) {
            val payload = text.removePrefix("close task").trim()
            val number = payload.toLongOrNull()
            if (number != null) return VoiceCommand.CloseById(number)
            if (payload.isNotBlank()) return VoiceCommand.CloseByName(payload)
        }

        return VoiceCommand.Unknown
    }
}
