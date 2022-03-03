@file:JvmName("LedMain")
package me.retrodaredevil.led

import com.diozero.ws281xj.LedDriverInterface
import com.diozero.ws281xj.rpiws281x.WS281x
import kotlin.math.PI
import kotlin.math.cos

fun getRainbow(percent: Double): Color {
    val spot = (percent * 6).toInt()
    val sub = (percent * 6) % 1
    val cosineAdjust = 1 - (cos(sub * PI) + 1) / 2.0
    val amount = (sub + cosineAdjust) / 2.0
    return when (spot) {
        0 -> Color(amount, 1.0, 0.0)
        1 -> Color(1.0, 1.0 - amount, 0.0)
        2 -> Color(1.0, 0.0, amount)
        3 -> Color(1 - amount, 0.0, 1.0)
        4 -> Color(0.0, amount, 1.0)
        else -> Color(0.0, 1.0, 1.0 - amount)
    }
}

fun main() {
    // All hard coded for now while I test :)
    val pixels: LedDriverInterface = WS281x(18, 255, 450)

    val percentGetter = ReversingPercentGetter(2.0, 10.0 * 60, 2.0)
    val ledSpread = 450 / 8.0 // divide virtual pixels by 8
    pixels.use {
        while (true) {
            val time = System.currentTimeMillis() / 1000.0
            val timePercent = percentGetter.getPercent(time)
            for (i in 0 until pixels.numPixels) {
                val percent = (timePercent + i / ledSpread) % 1.0
                val color = getRainbow(percent)
                pixels.setPixelColour(i, color.to24Bit())
            }
            pixels.render()
            Thread.sleep(3)
        }
    }
}
