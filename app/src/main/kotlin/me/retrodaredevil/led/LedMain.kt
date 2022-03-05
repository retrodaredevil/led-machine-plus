@file:JvmName("LedMain")
package me.retrodaredevil.led

import com.diozero.ws281xj.LedDriverInterface
import com.diozero.ws281xj.rpiws281x.WS281x
import me.retrodaredevil.led.alter.AlterRainbow
import me.retrodaredevil.led.alter.LedMetadata
import me.retrodaredevil.led.config.BaseConfig
import me.retrodaredevil.led.percent.ReversingPercentGetter
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

    // My gpio pin is 18 and my count is 450
    val pixels: LedDriverInterface = WS281x(baseConfig.gpioPort, 255, baseConfig.ledCount)

    val percentGetter = ReversingPercentGetter(2.0, 10.0 * 60, 2.0)
    val ledSpread = 450 / 8.0 // divide virtual pixels by 8
    val alter = AlterRainbow(percentGetter, ledSpread)
    val metadata = LedMetadata()
    var firstUpdate = true
    pixels.use {
        while (true) {
            val time = System.currentTimeMillis() / 1000.0
            for (i in 0 until pixels.numPixels) {
                val position = i.toDouble()
                val color = alter.alterPixel(time, position, null, metadata) ?: Color.BLACK
                val color24Bit = color.to24Bit()
                if (firstUpdate || pixels.getPixelColour(i) != color24Bit) {
                    pixels.setPixelColour(i, color.to24Bit())
                }
            }
            firstUpdate = false
            pixels.render()
            Thread.sleep(3)
        }
    }
}
