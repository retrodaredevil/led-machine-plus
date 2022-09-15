package me.retrodaredevil.led.message


fun main() {
    val botToken = "your token here"
    val messageQueue = DiscordMessageQueue("MTAxODk3OTQ5MTg2MDcyOTkyNw.GX8Qxk.fXcUtfpEFPcnykANGfyRORnIgXSIVR4XL5P25M", 1015428094938595370)
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

