package me.retrodaredevil.led.alter

import me.retrodaredevil.led.Color

class AlterRandom(
        private val minColor: Color
) : Alter {
    private val colorMap = mutableMapOf<Position, Color>()
    override fun alterPixel(seconds: Double, pixelPosition: Position, currentColor: Color?, metadata: LedMetadata): Color? {
        if (currentColor == null) {
            return null
        }
        var currentOverrideColor = colorMap[pixelPosition]
        if (currentOverrideColor == null || currentColor.isAllLowerThan(minColor)) {
            // then reset it
            currentOverrideColor = Color(Math.random(), Math.random(), Math.random())
            colorMap[pixelPosition] = currentOverrideColor
        }

        val ratioToScale = (currentColor.r + currentColor.g + currentColor.b) / (currentOverrideColor.r + currentOverrideColor.g + currentOverrideColor.b)
        return (currentOverrideColor * ratioToScale).clamp()
    }
    companion object {
        val INSTANCE = AlterRandom(Color(0.01, 0.01, 0.01))
    }
}
