package com.tsovedenski.parser

import org.junit.Assert

/**
 * Created by Tsvetan Ovedenski on 06/01/2018.
 */
fun <T> assertSuccess(parser: Parser<T>, input: String, expected: T) {
    val message = "Parsing '$input'"

    val result = parse(parser, input)
    Assert.assertTrue(message, result is Success)

    result as Success
    Assert.assertEquals(message, expected, result.value)
}

fun <T> assertError(parser: Parser<T>, input: String) {
    val message = "Parsing '$input'"

    val result = parse(parser, input)
    Assert.assertTrue(message, result is Error)
}