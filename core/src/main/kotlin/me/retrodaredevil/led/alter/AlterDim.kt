package me.retrodaredevil.led.alter

import me.retrodaredevil.led.Color

class AlterDim(
        private val dim: Double
) : Alter {
    init {
        check(dim in 0.0..1.0)
    }
    override fun alterPixel(seconds: Double, pixelPosition: Position, currentColor: Color?, metadata: LedMetadata): Color? {
        if (currentColor == null) {
            return null
        }
        return currentColor.scale(dim)
    }
}