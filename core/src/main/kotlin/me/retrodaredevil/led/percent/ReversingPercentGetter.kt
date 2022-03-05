package me.retrodaredevil.led.percent

class ReversingPercentGetter(
        val period: Double,
        val directionPeriod: Double,
        val reversePeriod: Double
) : PercentGetter {
    init {
        check(directionPeriod % period == 0.0)
        check(reversePeriod % period == 0.0)
    }
    override fun getPercent(seconds: Double): Double {
        val spot = seconds % (directionPeriod * 2)
        var percent = (seconds / period) % 1.0
        if (spot <= reversePeriod) {
            val a = period / reversePeriod
            val x = (spot - reversePeriod / 2) / period
            val height = reversePeriod / 4 / period
            percent = a * x * x + 1 - height
        } else if (directionPeriod <= spot && spot <= (directionPeriod + reversePeriod)) {
            val a = period / reversePeriod
            val x = (spot - directionPeriod - reversePeriod / 2) / period
            val height = reversePeriod / 4 / period
            percent = 1 - (a * x * x + 1 - height)
        } else if (spot > directionPeriod + reversePeriod) {
            percent = 1 - percent
        }
        return percent
    }
}
