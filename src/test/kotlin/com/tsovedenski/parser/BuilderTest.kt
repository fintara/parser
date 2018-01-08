package com.tsovedenski.parser

import org.junit.Test

/**
 * Created by Tsvetan Ovedenski on 08/01/18.
 */
class BuilderTest {

    @Test
    fun `summing parser`() {
        val sum: Parser<Double> = buildParser {
            val a = number.ev().toDouble()
            char('+').ev()
            val b = unumber.ev().toDouble()
            a + b
        }

        assertSuccess(sum, "-3.5+4", 0.5)
    }

}