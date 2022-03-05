package me.retrodaredevil.led.alter

import me.retrodaredevil.led.Color

class AlterSolid(
        private val color: Color
) : Alter {
    override fun alterPixel(seconds: Double, pixelPosition: Position, currentColor: Color?, metadata: LedMetadata): Color? {
        return color
    }

    override fun toString(): String {
        return "AlterSolid(color=$color)"
    }

}
