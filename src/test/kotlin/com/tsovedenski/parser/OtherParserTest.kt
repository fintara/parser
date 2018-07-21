package com.tsovedenski.parser

import org.junit.Assert
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
    fun `skip many success`() {
        val input = "ABCD"
        val p = skipMany(digit) andR count(4, upper)

        assertSuccess(p, input, "ABCD".toList())
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

    @Test
    fun `run parser success`() {
        val result = run(int, "42A")
        Assert.assertEquals(42, result)
    }

    @Test
    fun `run parser error`() {
        val result = run(int, "AA")
        Assert.assertNull(result)
    }

    @Test
    fun `chain empty list`() {
        val parsers = listOf<Parser<*>>()
        assertSuccess(parsers.chain(), "abcd", listOf(), "abcd")
    }

    @Test
    fun `chain one parser`() {
        val parsers = listOf(digit)
        assertSuccess(parsers.chain(), "1XYZ", "1".toList(), "XYZ")
    }

    @Test
    fun `chain parsers`() {
        val parsers = listOf(digit, lower, upper, lower, char('+'))
        assertSuccess(parsers.chain(), "1aBc+XYZ", "1aBc+".toList(), "XYZ")
    }

    @Test
    fun `error message`() {
        val errorMsg = "custom error message"
        val parser = digit % errorMsg

        val result = parse(parser, "")
        Assert.assertTrue(result is Error)
        result as Error
        Assert.assertEquals(errorMsg, result.message)
    }
}