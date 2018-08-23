package com.tsovedenski.parser

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

/**
 * Created by Tsvetan Ovedenski on 06/01/2018.
 */
object ManyParserTest : Spek({

    describe("many") {
        it("succeeds with 0 occurences") {
            assertSuccess(many(digit), "ABCD1234", listOf(), "ABCD1234")
        }

        it("succeeds with 1 occurence") {
            assertSuccess(many(letter), "A1BCD1234", listOf('A'), "1BCD1234")
        }

        it("succeeds with 4 occurences") {
            assertSuccess(many(letter), "ABCD1234", "ABCD".toList(), "1234")
        }

        it("succeeds with alphaNum") {
            assertSuccess(many(alphaNum), "Ab12Cd34eF@3", "Ab12Cd34eF".toList(), "@3")
        }
    }

    describe("many1") {
        it("fails with 0 occurences") {
            assertError(many1(digit), "ABCD1234")
        }

        it("succeeds with 1 occurence") {
            assertSuccess(many1(letter), "A1BCD1234", listOf('A'), "1BCD1234")
        }

        it("succeeds with 4 occurences") {
            assertSuccess(many1(letter), "ABCD1234", "ABCD".toList(), "1234")
        }
    }
})