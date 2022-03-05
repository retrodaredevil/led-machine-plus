package me.retrodaredevil.led.message

import java.time.Instant

data class Message(
        val text: String,
        val timestamp: Instant
)