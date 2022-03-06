@file:JvmName("LedMain")
package me.retrodaredevil.led

import com.diozero.ws281xj.LedDriverInterface
import com.diozero.ws281xj.rpiws281x.WS281x
import me.retrodaredevil.led.alter.LedMetadata
import me.retrodaredevil.led.config.BaseConfig
import me.retrodaredevil.led.program.LedProgram
import org.slf4j.LoggerFactory
import sun.misc.Signal
import sun.misc.SignalHandler
import java.io.File


fun main(args: Array<String>) {
    when {
        args.isEmpty() -> {
            System.err.println("You must supply a config argument!")
            return
        }
        args.size > 1 -> {
            System.err.println("Only one argument allowed!")
            return
        }
    }
    val configFilePath = args[0]
    val configFile = File(configFilePath)
    val objectMapper = createDefaultMapper()
    val baseConfig = objectMapper.readValue(configFile, BaseConfig::class.java)
    println("Starting")
    val logger = LoggerFactory.getLogger("LedMain")
    logger.info("Starting")

    val mainThread = Thread.currentThread()
    Signal.handle(Signal("INT")) { sig: Signal ->
        logger.info("Received INT termination signal")
        mainThread.interrupt()
        try {
            @Suppress("BlockingMethodInNonBlockingContext")
            mainThread.join(500)
        } catch (ex: InterruptedException) {
            logger.error("Could not stop the main thread! It will forcibly be stopped.")
        }
    }

    logger.info("Using gpio: ${baseConfig.gpioPort} with led count: ${baseConfig.ledCount}")
    val pixels: LedDriverInterface = WS281x(baseConfig.gpioPort, 255, baseConfig.ledCount)

    val metadata = LedMetadata()
    val ledProgram = LedProgram(baseConfig)
    var firstUpdate = true
    pixels.use {
        while (!Thread.currentThread().isInterrupted) {
            val time = System.currentTimeMillis() / 1000.0
            ledProgram.update(time)
            val alter = ledProgram.getAlter()
            for (i in 0 until pixels.numPixels) {
                val position = i.toDouble()
                val color = alter.alterPixel(time, position, null, metadata) ?: Color.BLACK
                val color24Bit = color.to24Bit()
                if (firstUpdate || pixels.getPixelColour(i) != color24Bit) {
                    pixels.setPixelColour(i, color24Bit)
                }
            }
            firstUpdate = false
            pixels.render()
        }
        logger.info("Going to gracefully exit!")
    }
}
