package me.retrodaredevil.led.alter

import me.retrodaredevil.led.Color
import me.retrodaredevil.led.percent.PercentGetter

class AlterBlock(
        private val blockList: List<Block>,
        private val percentGetter: PercentGetter,
        private val fadeWidth: Double = 1.0
) : Alter {
    private val totalWidth: Double = blockList.sumOf { it.width }

    init {
        check(fadeWidth >= 0.0)
    }

    private fun getColor(position: Position): Color? {
        var offset = 0.0

        for (block in blockList) {
            if (position < block.width + offset) {
                return block.color
            }
            offset += block.width
        }
        throw AssertionError("This shouldn't happen! position must be out of bounds! position: $position")
    }

    override fun alterPixel(seconds: Double, pixelPosition: Position, currentColor: Color?, metadata: LedMetadata): Color? {
        val percent = percentGetter.getPercent(seconds)
        val offset = percent * totalWidth
        val pixelToGet = (pixelPosition + offset) % totalWidth
        val lowPixel = getColor(pixelToGet) ?: currentColor
        val highPixel = getColor((pixelToGet + fadeWidth) % totalWidth) ?: currentColor
        // TODO There may be a bug if fadeWidth is not 0 or is not 1
        val lerpPercent = if (fadeWidth == 0.0) 0.0 else (pixelToGet % fadeWidth) / fadeWidth

        if (lowPixel == null && highPixel == null) {
            return null
        }
        return (lowPixel ?: Color.BLACK).lerp(highPixel ?: Color.BLACK, lerpPercent)
    }

    class Block(
            val color: Color?,
            val width: Double,
    )
}
