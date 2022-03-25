package me.retrodaredevil.led.alter

import me.retrodaredevil.led.Color

/**
 * Takes an existing color, only uses its brightness, then sets it to a random color with the same brightness.
 * The color will be reset one the current color has gone at or below the [minColor] threshold (usually 0,0,0).
 */
class AlterRandom(
        private val minColor: Color
) : Alter {
    private val colorMap = mutableMapOf<Position, Color>()
    override fun alterPixel(seconds: Double, pixelPosition: Position, currentColor: Color?, metadata: LedMetadata): Color? {
        if (currentColor == null) {
            return null
        }
        var currentOverrideColor = colorMap[pixelPosition]
        if (currentOverrideColor == null || currentColor.isAllLessThanOrEqual(minColor)) {
            // then reset it
            currentOverrideColor = getRainbow(Math.random())
            colorMap[pixelPosition] = currentOverrideColor
        }

        val ratioToScale = (currentColor.r + currentColor.g + currentColor.b) / (currentOverrideColor.r + currentOverrideColor.g + currentOverrideColor.b)
        return (currentOverrideColor * ratioToScale).clamp()
    }
    companion object {
        val INSTANCE = AlterRandom(Color(0.0, 0.0, 0.0))
    }
}
