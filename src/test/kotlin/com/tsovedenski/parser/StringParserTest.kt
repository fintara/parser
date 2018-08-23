package com.tsovedenski.parser

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

/**
 * Created by Tsvetan Ovedenski on 07/01/2018.
 */
object StringParserTest : Spek({
    describe("string") {
        it("parses //comment") {
            assertSuccess(string("//comment"), "//comment and else", "//comment", " and else")
        }

        it("fails when string not found") {
            assertError(string("wanted string"), "another string")
        }
    }
})