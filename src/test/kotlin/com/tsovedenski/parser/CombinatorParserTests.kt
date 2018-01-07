package com.tsovedenski.parser

import org.junit.Test
import kotlin.math.exp

/**
 * Created by Tsvetan Ovedenski on 07/01/2018.
 */
class CombinatorParserTests {

    @Test
    fun `arithmetic expression`() {
        val tail = oneOf('+', '-', '*', '/') and integer
        val tails = many1(tail).map { it.flatten() }
        val expr = integer and tails

        assertSuccess(
                expr,
                "12+3*6/2-20",
                listOf(12, '+', 3, '*', 6, '/', 2, '-', 20))
    }

    @Test
    fun `phone`() {
        val group = count(3, digit).map { it.joinToString("") }
        val dash  = char('-')
        val phone = group and
                    dash and group and
                    dash and group

        assertSuccess(
                phone,
                "123-456-789",
                listOf("123", '-', "456", '-', "789")
        )
    }

    @Test
    fun `then success`() {
        val expected = "!@#"
        assertSuccess(
                digit then string(expected),
                "1!@#",
                expected
        )
    }

    @Test
    fun `then first fails`() {
        assertError(
                digit then string("ABC"),
                "xABC"
        )
    }

    @Test
    fun `or first`() {
        assertSuccess(
                digit or upper,
                "1A",
                '1'
        )
    }

    @Test
    fun `or second`() {
        assertSuccess(
                digit or upper,
                "AB",
                'A'
        )
    }

    @Test
    fun `or type first`() {
        assertSuccess(
                integer or upper,
                "123A",
                123
        )
    }

    @Test
    fun `or type second`() {
        assertSuccess(
                integer or upper,
                "A123",
                'A'
        )
    }
}