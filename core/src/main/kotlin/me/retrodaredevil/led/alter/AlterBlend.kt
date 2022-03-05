package me.retrodaredevil.led.alter

import me.retrodaredevil.led.Color
import me.retrodaredevil.led.percent.PercentGetter

class AlterBlend(
        private val percentGetter: PercentGetter,
        private val alters: List<Alter>,
) : Alter {
    private fun getAlter(percent: Double): Result {
        val offset = (percent * alters.size) % alters.size
        val leftIndex = offset.toInt()
        val rightIndex = (leftIndex + 1) % alters.size
        val lerpPercent = offset % 1.0
        return Result(alters[leftIndex], alters[rightIndex], lerpPercent)
    }
    override fun alterPixel(seconds: Double, pixelPosition: Position, currentColor: Color?, metadata: LedMetadata): Color? {
        val percent = percentGetter.getPercent(seconds)
        val result = getAlter(percent)
        val leftColor = result.leftAlter.alterPixel(seconds, pixelPosition, currentColor, metadata) ?: currentColor ?: Color.BLACK
        val rightColor = result.rightAlter.alterPixel(seconds, pixelPosition, currentColor, metadata) ?: currentColor ?: Color.BLACK
        return leftColor.lerp(rightColor, result.lerpPercent)
    }

    private class Result(
            val leftAlter: Alter,
            val rightAlter: Alter,
            val lerpPercent: Double
    )
}
