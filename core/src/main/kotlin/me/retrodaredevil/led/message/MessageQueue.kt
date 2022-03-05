package me.retrodaredevil.led.message

interface MessageQueue : AutoCloseable {
    fun popNewMessages(): List<Message>
}