package com.tsovedenski.parser

import org.junit.Test

/**
 * Created by Tsvetan Ovedenski on 07/01/2018.
 */
class SymbolParserTest {

    @Test fun testS001() = assertSuccess(string("//comment"), "//comment and else", "//comment", " and else")

    @Test fun testE001() = assertError(string("wanted string"), "another string")
}