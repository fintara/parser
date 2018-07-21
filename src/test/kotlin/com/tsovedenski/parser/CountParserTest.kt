package com.tsovedenski.parser

import org.junit.Test

/**
 * Created by Tsvetan Ovedenski on 06/01/2018.
 */
class CountParserTest {

    @Test fun emptyList() = testSuccess(upper, 0, "ABCD", listOf(), "ABCD")
    @Test fun testS001() = testSuccess(upper, 5, "ABCDE12345", "ABCDE".toList(), "12345")
    @Test fun testS002() = testSuccess(
            digit.map { it.toString().toInt() },
            3,
            "681QWE",
            listOf(6, 8, 1),
            "QWE"
    )

    @Test fun testE001() = testError(lower, 5, "ABCDE12345")
    @Test fun testE002() = testError(letter, 3, "AB123")

    private fun <T> testSuccess(parser: Parser<T>, number: Int, input: String, expected: List<T>, expectedRest: String)
            = assertSuccess(count(number, parser), input, expected, expectedRest)

    private fun <T> testError(parser: Parser<T>, number: Int, input: String)
            = assertError(count(number, parser), input)
}