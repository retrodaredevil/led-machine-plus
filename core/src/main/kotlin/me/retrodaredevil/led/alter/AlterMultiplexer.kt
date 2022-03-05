package me.retrodaredevil.led.alter

import me.retrodaredevil.led.Color

class AlterMultiplexer(
        private val alters: List<Alter>
) : Alter {
    override fun alterPixel(seconds: Double, pixelPosition: Position, currentColor: Color?, metadata: LedMetadata): Color? {
        var finalColor = currentColor
        for (alter in alters) {
            finalColor = alter.alterPixel(seconds, pixelPosition, currentColor, metadata)
        }
        return finalColor
    }
}