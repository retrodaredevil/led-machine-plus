package me.retrodaredevil.led.alter

import me.retrodaredevil.led.Color
import me.retrodaredevil.led.percent.PercentGetter

class AlterFade(
        private val percentGetter: PercentGetter,
        private val alters: List<Alter>,
        private val ledSpread: Double,
) : Alter {
    override fun alterPixel(seconds: Double, pixelPosition: Position, currentColor: Color?, metadata: LedMetadata): Color? {
        val percent = 0.0
        val offset = (percent * alters.size) % alters.size
        val leftIndex = offset.toInt()
        val rightIndex = (leftIndex + 1) % alters.size
        val lerpPercent = offset % 1.0
        val leftColor = alters[leftIndex].alterPixel(seconds, pixelPosition, currentColor, metadata)
        val rightColor = alters[rightIndex].alterPixel(seconds, pixelPosition, currentColor, metadata)

        if (leftColor == null && rightColor == null) {
            return null
        }

        return (leftColor ?: Color.BLACK).lerp(rightColor ?: Color.BLACK, lerpPercent)
    }
}
