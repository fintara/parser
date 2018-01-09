package com.tsovedenski.parser

import org.junit.Test

/**
 * Created by Tsvetan Ovedenski on 09/01/18.
 */
class OtherParserTest {

    @Test
    fun `eof success`() {
        val input = "A1"
        val p = upper and digit and eof

        assertSuccess(p, input, input.toList())
    }

    @Test
    fun `eof error`() {
        val input = "A1str"
        val p = upper and digit and eof

        assertError(p, input)
    }

    @Test
    fun `skip spaces`() {
        val input = "  A    B CD"
        val p = buildParser {
            skipSpaces.ev()
            val a = upper.ev()
            skipSpaces.ev()
            val b = upper.ev()
            skipSpaces.ev()
            val c = upper.ev()
            skipSpaces.ev()
            val d = upper.ev()
            "$a$b$c$d"
        }

        assertSuccess(p, input, "ABCD")
    }

    @Test
    fun `skip many1 error`() {
        val input = "ABCD"
        val p = skipMany1(digit) andR count(4, upper)

        assertError(p, input)
    }

    @Test
    fun `skip many1 success`() {
        val input = "1234ABCD"
        val p = skipMany1(digit) andR count(4, upper)

        assertSuccess(p, input, "ABCD".toList())
    }
}