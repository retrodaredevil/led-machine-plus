package me.retrodaredevil.led.alter

import me.retrodaredevil.led.Color
import me.retrodaredevil.led.percent.PercentGetter
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

class AlterRainbow(
        private val percentGetter: PercentGetter,
        private val ledSpread: Double,
) : Alter {
    override fun alterPixel(seconds: Double, pixelPosition: Position, currentColor: Color?, metadata: LedMetadata): Color? {
        val timePercent = percentGetter.getPercent(seconds)
        val percent = (timePercent + pixelPosition / ledSpread) % 1.0
        return getRainbow(percent)
    }
}
