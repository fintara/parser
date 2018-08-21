package com.tsovedenski.parser

import org.junit.Test

/**
 * Created by Tsvetan Ovedenski on 07/01/2018.
 */
class CombinatorParserTests {

    @Test
    fun `arithmetic expression`() {
        val tail = chain(oneOf('+', '-', '*', '/'), int)
        val tails = many1(tail).map { it.flatten() }
        val expr = chain(int.map { listOf(it) }, tails).map { it.flatten() }

        assertSuccess(
                expr,
                "12+3*6/2-20",
                listOf(12, '+', 3, '*', 6, '/', 2, '-', 20))
    }

    @Test
    fun `arithmetic expression 2`() {
        val operator = oneOf(*"+-*/^".toCharArray())
        val tail = chain(operator, number)
        val tails = many1(tail).map { it.flatten() }
        val expr = chain(number.map(::listOf), tails).map { it.flatten() }

        assertSuccess(
                expr,
                "-5*20-3.5^2",
                listOf(-5, '*', 20, '-', 3.5, '^', 2)
        )
    }

    @Test
    fun `floating point number`() {
        val value = -87657265.666
        val input = "-8.7657265666E7"
        assertSuccess(float, input, value)
    }

    @Test
    fun `phone`() {
        val group = count(3, digit).map { it.joinToString("") }
        val dash  = char('-')
        val phone = chain(group, dash,
                          group, dash,
                          group)

        assertSuccess(
                phone,
                "123-456-789",
                listOf("123", '-', "456", '-', "789")
        )
    }

    @Test
    fun `phone 2`() {
        val group = count(3, digit).map { it.joinToString("") }
        val dash  = char('-')
        val phone = group sepBy1 dash

        assertSuccess(
                phone,
                "123-456-789",
                listOf("123", "456", "789")
        )
    }

    @Test
    fun `andR first fails`() {
        assertError(
                digit andR string("ABC"),
                "xABC"
        )
    }

    @Test
    fun `andR success`() {
        val expected = "!@#"
        assertSuccess(
                digit andR string(expected),
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
    fun `or first`() = assertSuccess(
            digit or upper,
            "1A",
            '1',
            "A"
    )

    @Test
    fun `or second`() = assertSuccess(
            digit or upper,
            "AB",
            'A',
            "B"
    )

    @Test
    fun `or type first`() = assertSuccess(
            int or upper,
            "123A",
            123,
            "A"
    )

    @Test
    fun `or type second`() = assertSuccess(
            int or upper,
            "A123",
            'A',
            "123"
    )

    @Test
    fun `all but vowels`() {
        val notVowel = noneOf(*"aeiou".toCharArray())
        assertSuccess(notVowel, "qwerty", 'q', "werty")
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

    @Test
    fun `optional success`() {
        val p = optional(string("test")) andR int
        assertSuccess(p, "test42", 42)
    }

    @Test
    fun `optional not found success`() {
        val p = optional(string("test")) andR int
        assertSuccess(p, "42test1", 42, "test1")
    }

    @Test
    fun `sepBy success`() {
        val input = "1 :: 2 :: 3"
        val p = int sepBy string(" :: ")

        assertSuccess(p, input, listOf(1, 2, 3))
    }

    @Test
    fun `sepBy leaves last separator`() {
        val input = "1 :: 2 :: "
        val p = int sepBy string(" :: ")

        assertSuccess(p, input, listOf(1, 2), " :: ")
    }

    @Test
    fun `sepBy1 error`() {
        val input = "xxx"
        val p = int sepBy1 string(" :: ")

        assertError(p, input)
    }

    @Test
    fun `endBy success`() {
        val input = "a;b;c;x"
        val p = lower endBy char(';')

        assertSuccess(p, input, "abc".toList(), "x")
    }

    @Test
    fun `endBy does not leave last separator`() {
        val input = "a;b;c;"
        val p = lower endBy char(';')

        assertSuccess(p, input, "abc".toList())
    }

    @Test
    fun `endBy1 success`() {
        val input = "a?b?c?d"
        val p = lower endBy1 char('?')

        assertSuccess(p, input, "abc".toList(), "d")
    }

    @Test
    fun `endBy1 error`() {
        val input = "X?Y?"
        val p = lower endBy1 char('?')

        assertError(p, input)
    }

    @Test
    fun `choice success first`() = assertSuccess(
            choice(upper, lower, digit, space),
            "Abcd",
            'A',
            "bcd"
    )

    @Test
    fun `choice success mid`() = assertSuccess(
            choice(upper, lower, digit, space),
            "5bcd",
            '5',
            "bcd"
    )

    @Test
    fun `choice success last`() = assertSuccess(
            choice(upper, lower, digit, space),
            " bcd",
            ' ',
            "bcd"
    )

    @Test
    fun `choice error`() = assertError(
            choice(upper, lower, space),
            "1bcd"
    )

    @Test
    fun `between success brackets`() {
        val brackets = between(char('('), char(')'), int)

        assertSuccess(
                brackets,
                "(42)x",
                42,
                "x"
        )
    }
}