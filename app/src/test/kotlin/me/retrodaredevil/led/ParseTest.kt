package me.retrodaredevil.led

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class ParseTest {
    @Test
    fun test() {
        assertEquals(listOf(Color.RED, Color.GREEN, Color.BLUE), Parse.parseColors("red green blue"))
        assertEquals(listOf(Color.RED, Color.GREEN, Color.BLUE, Color.PURPLE, Color.fromRgb(255, 0, 70)), Parse.parseColors("red green blue purple deep purple"))
    }
}
