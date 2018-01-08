package com.tsovedenski.parser

import org.junit.Test

/**
 * Created by Tsvetan Ovedenski on 06/01/2018.
 */
class ManyParserTest {
    @Test fun `no occurence`() = assertSuccess(many(digit), "ABCD1234", listOf())
    @Test fun `one occurence`() = assertSuccess(many(letter), "A1BCD1234", listOf('A'))
    @Test fun `four occurence`() = assertSuccess(many(letter), "ABCD1234", "ABCD".toList())
    @Test fun `alphaNums`() = assertSuccess(many(alphaNum), "Ab12Cd34eF@3", "Ab12Cd34eF".toList())
}