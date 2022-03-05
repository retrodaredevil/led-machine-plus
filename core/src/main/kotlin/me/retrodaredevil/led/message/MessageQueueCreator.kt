package me.retrodaredevil.led.message

fun interface MessageQueueCreator {
    fun createMessageQueue(): MessageQueue
}
