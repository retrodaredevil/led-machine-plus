@file:JvmName("LedMain")
package me.retrodaredevil.led

import com.diozero.ws281xj.LedDriverInterface
import com.diozero.ws281xj.StripType
import com.diozero.ws281xj.rpiws281x.WS281x
import com.diozero.ws281xj.rpiws281x.WS281xNative
import com.diozero.ws281xj.spi.WS281xSpi
import me.retrodaredevil.led.alter.LedMetadata
import me.retrodaredevil.led.config.BaseConfig
import me.retrodaredevil.led.config.ColorOrder
import me.retrodaredevil.led.program.LedProgram
import org.slf4j.LoggerFactory
import sun.misc.Signal
import sun.misc.SignalHandler
import java.io.File
import java.io.IOException
import kotlin.system.exitProcess

private fun orderToStripType(colorOrder: ColorOrder): StripType = when(colorOrder) {
    ColorOrder.GRB -> StripType.WS2811_GRB
    ColorOrder.RGB -> StripType.WS2811_RGB
}

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
    println("Starting")
    val logger = LoggerFactory.getLogger("LedMain")
    logger.info("Starting")

    val objectMapper = createDefaultMapper()
    val baseConfig: BaseConfig
    try {
        baseConfig = objectMapper.readValue(configFile, BaseConfig::class.java)
    } catch (ex: IOException) {
        logger.error("Could not read configuration", ex)
        return
    }

    val mainThread = Thread.currentThread()
    val signalHandler = SignalHandler { sig: Signal ->
        logger.info("Received ${sig.name} termination signal")
        mainThread.interrupt()
        try {
            mainThread.join(100)
        } catch (ex: InterruptedException) {
            logger.error("Could not stop the main thread! It will forcibly be stopped.")
        }
    }
    Signal.handle(Signal("INT"), signalHandler) // CTRL+C handling
    Signal.handle(Signal("TERM"), signalHandler) // systemd stop handling

    logger.info("Using gpio: ${baseConfig.gpioPort} with led count: ${baseConfig.ledCount}")
    val stripType = orderToStripType(baseConfig.order)
    val pixels: LedDriverInterface = if (baseConfig.spi) {
        val controller = when (val gpio = baseConfig.gpioPort) {
            7, 8, 9, 10, 11 -> 0
            16, 17, 18, 19, 20, 21 -> 1
            else -> throw IllegalArgumentException("Gpio: $gpio does not support SPI!")
        }
        // chip select is hard coded to 0 for now
        WS281xSpi(controller, 0, WS281xSpi.Protocol.PROTOCOL_800KHZ, stripType, baseConfig.ledCount, 255)
    } else WS281x(800_000, 5, baseConfig.gpioPort, 255, baseConfig.ledCount, stripType, 0)
    logger.info("pixels is of type: ${pixels.javaClass}")

    val metadata = LedMetadata()
    val ledProgram = LedProgram(baseConfig)
    var firstUpdate = true
    try {
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
            try {
                Thread.sleep(1)
            } catch (ex: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
        logger.info("Going to gracefully exit!")
    } finally {
        logger.debug("Going to close")
        if (pixels is WS281x) {
            // The close method of WS281x really isn't that great. This is actually better as it doesn't turn all the lights off (why does it do that)
            WS281xNative.terminate()
        } else {
            pixels.close()
        }
        logger.debug("Successfully closed")
    }
    exitProcess(0)
}
