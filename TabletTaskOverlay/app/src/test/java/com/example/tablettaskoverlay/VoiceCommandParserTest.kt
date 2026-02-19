package com.example.tablettaskoverlay

import com.example.tablettaskoverlay.voice.VoiceCommand
import com.example.tablettaskoverlay.voice.VoiceCommandParser
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceCommandParserTest {
    @Test
    fun parsesWriteTask() {
        assertTrue(VoiceCommandParser.parse("write a task") is VoiceCommand.WriteTask)
    }

    @Test
    fun parsesCloseById() {
        assertTrue(VoiceCommandParser.parse("close task 12") is VoiceCommand.CloseById)
    }

    @Test
    fun parsesCloseByName() {
        assertTrue(VoiceCommandParser.parse("close task vendor") is VoiceCommand.CloseByName)
    }
}
