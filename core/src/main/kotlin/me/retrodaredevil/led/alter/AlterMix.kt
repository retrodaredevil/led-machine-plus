package me.retrodaredevil.led.alter

import me.retrodaredevil.led.Color
import me.retrodaredevil.led.RawColor

class AlterMix(
        private val alterEntries: List<Entry>
) : Alter {
    override fun alterPixel(seconds: Double, pixelPosition: Position, currentColor: Color?, metadata: LedMetadata): Color? {
        var r = 0.0
        var g = 0.0
        var b = 0.0
        for (entry in alterEntries) {
            val alter = entry.alter
            val color = alter.alterPixel(seconds, pixelPosition, currentColor, metadata)
            if (color != null) {
                r += color.r * entry.brightnessMultiplier
                g += color.g * entry.brightnessMultiplier
                b += color.b * entry.brightnessMultiplier
            }
        }
        return RawColor(r, g, b).clamp()
    }

    class Entry(
            val alter: Alter,
            val brightnessMultiplier: Double,
    )
}
