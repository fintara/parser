package com.tsovedenski.parser

import org.junit.Test

/**
 * Created by Tsvetan Ovedenski on 07/01/2018.
 */
class SymbolParserTest {

    @Test fun testS001() = assertSuccess(symbol("//comment"), "//comment and else", "//comment")

    @Test fun testE001() = assertError(symbol("wanted string"), "another string")
}