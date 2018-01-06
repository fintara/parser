package com.tsovedenski.parser

import org.junit.Test

/**
 * Created by Tsvetan Ovedenski on 06/01/2018.
 */
class DigitParserTest {

    @Test
    fun digits() {
        val digits = '0'..'9'
        digits.forEach {
            assertSuccess(digit, it.toString(), it)
        }
    }

    @Test
    fun `non-digits`() {
        val nonDigits = listOf((32..47).toList(), (58..126).toList()).flatten().map(Int::toChar)
        nonDigits.forEach {
            assertError(digit, it.toString())
        }
    }
}