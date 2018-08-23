package com.tsovedenski.parser

import org.junit.Assert

/**
 * Created by Tsvetan Ovedenski on 06/01/2018.
 */
internal fun <T> assertSuccess(parser: Parser<T>, input: String, expected: T, expectedRest: String = "") {
    val result = parse(parser, input)
    Assert.assertTrue("$input: Success", result is Success)

    result as Success
    Assert.assertEquals("$input: Expected", expected, result.value)
    Assert.assertEquals("$input: Rest", expectedRest, result.rest)
}

internal fun <T> assertError(parser: Parser<T>, input: String) {
    val result = parse(parser, input)
    Assert.assertTrue("$input: Error", result is Error)
}

internal fun <T> assertState(parser: Parser<T>, input: String, expectedState: ParserState) {
    val result = parse(parser, input)
    Assert.assertEquals(expectedState, result.state)
}