package me.retrodaredevil.led.alter

import me.retrodaredevil.led.Color

object AlterNothing : Alter {
    override fun alterPixel(seconds: Double, pixelPosition: Position, currentColor: Color?, metadata: LedMetadata): Color? {
        return currentColor
    }
}