package com.tsovedenski.parser

import org.junit.Test

/**
 * Created by Tsvetan Ovedenski on 06/01/2018.
 */
class Many1ParserTest {
    @Test fun `no occurence`() = assertError(many1(digit), "ABCD1234")

    @Test fun `one occurence`() = assertSuccess(many1(letter), "A1BCD1234", listOf('A'), "1BCD1234")
    @Test fun `four occurence`() = assertSuccess(many1(letter), "ABCD1234", "ABCD".toList(), "1234")
}