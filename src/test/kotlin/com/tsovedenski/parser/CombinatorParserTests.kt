package com.tsovedenski.parser

import org.junit.Test

/**
 * Created by Tsvetan Ovedenski on 07/01/2018.
 */
class CombinatorParserTests {

    @Test
    fun `arithmetic expression`() {
        val tail = oneOf('+', '-', '*', '/') and int
        val tails = many1(tail).map { it.flatten() }
        val expr = int and tails

        assertSuccess(
                expr,
                "12+3*6/2-20",
                listOf(12, '+', 3, '*', 6, '/', 2, '-', 20))
    }

    @Test
    fun `arithmetic expression 2`() {
        val operator = oneOf(*"+-*/^".toCharArray())
        val tail = operator and number
        val tails = many1(tail).map { it.flatten() }
        val expr = number and tails

        assertSuccess(
                expr,
                "-5*20-3.5^2",
                listOf(-5, '*', 20, '-', 3.5, '^', 2)
        )
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
    fun `andR first fails`() {
        assertError(
                digit andR symbol("ABC"),
                "xABC"
        )
    }

    @Test
    fun `andR success`() {
        val expected = "!@#"
        assertSuccess(
                digit andR symbol(expected),
                "1!@#",
                expected
        )
    }

    @Test
    fun `andL first fails`() = assertError(
            digit andL upper,
            "AA"
    )

    @Test
    fun `andL second fails`() = assertError(
            digit andL upper,
            "55"
    )

    @Test
    fun `andL success`() = assertSuccess(
            digit andL upper,
            "5A",
            '5'
    )

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
                int or upper,
                "123A",
                123
        )
    }

    @Test
    fun `or type second`() {
        assertSuccess(
                int or upper,
                "A123",
                'A'
        )
    }

    @Test
    fun `all but vowels`() {
        val notVowel = noneOf(*"aeiou".toCharArray())
        assertSuccess(notVowel, "qwerty", 'q')
        assertError(notVowel, "ayyy")
    }

    @Test
    fun `all kinds of spaces`() {
        val input = "\t  \t"
        assertSuccess(count(4, space), input, input.toList())
    }

    @Test
    fun `negative integer`() {
        val input = "-42"
        assertSuccess(int, input, -42)
    }

    @Test
    fun `floating number with point`() {
        val input = "12.345"
        assertSuccess(float, input, 12.345)
    }

    @Test
    fun `floating number with e-notation`() {
        val input = "6.5e3"
        assertSuccess(float, input, 6500.0)
    }
}