package me.retrodaredevil.led.alter

import me.retrodaredevil.led.Color

class AlterPartition(
        private val partitions: List<Partition>,
) : Alter {
    override fun alterPixel(seconds: Double, pixelPosition: Position, currentColor: Color?, metadata: LedMetadata): Color? {
        val partition = partitions.firstOrNull { it.bounds.any { bound -> bound.first <= pixelPosition && pixelPosition < bound.first + bound.second }}
        if (partition == null) {
            return currentColor
        }
        return partition.alter.alterPixel(seconds, pixelPosition, currentColor, metadata)
    }

    class Partition(
            val alter: Alter,
            val bounds: List<Pair<Double, Double>>,
    )
}