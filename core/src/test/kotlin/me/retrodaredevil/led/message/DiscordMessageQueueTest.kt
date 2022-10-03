package me.retrodaredevil.led.message


fun main() {
    val botToken = "your token here"
    val channelId: Long = 1015428094938595370 // put your channel ID here
    val messageQueue = DiscordMessageQueue(botToken, channelId)
    messageQueue.use {
        for (i in 1..100) {
            Thread.sleep(1000)
            val messages = messageQueue.popNewMessages()
            for (message in messages) {
                println(message.text)
            }
        }
    }
}

