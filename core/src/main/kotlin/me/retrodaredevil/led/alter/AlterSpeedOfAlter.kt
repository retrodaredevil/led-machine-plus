package me.retrodaredevil.led.alter

import me.retrodaredevil.led.Color

class AlterSpeedOfAlter(
        private val alter: Alter,
        private val timeMultiplierGetter: () -> Double,
) : Alter {
    override fun alterPixel(seconds: Double, pixelPosition: Position, currentColor: Color?, metadata: LedMetadata): Color? {
        return alter.alterPixel(seconds * timeMultiplierGetter(), pixelPosition, currentColor, metadata)
    }
}