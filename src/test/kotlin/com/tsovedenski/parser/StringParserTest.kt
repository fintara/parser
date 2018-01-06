package com.tsovedenski.parser

import org.junit.Test

/**
 * Created by Tsvetan Ovedenski on 07/01/2018.
 */
class StringParserTest {

    @Test fun testS001() = assertSuccess(string("//comment"), "//comment and else", "//comment")

    @Test fun testE001() = assertError(string("wanted com.tsovedenski.parser.string"), "another com.tsovedenski.parser.string")
}