package com.tsovedenski.parser

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

/**
 * Created by Tsvetan Ovedenski on 06/01/2018.
 */
object DigitParserTest : Spek({

    context("digit") {
        describe("digits") {
            val digits = '0'..'9'
            digits.forEach {
                it("parses $it") {
                    assertSuccess(digit, it.toString(), it)
                }
            }
        }

        describe("non-digits") {
            val nonDigits = listOf((32..47).toList(), (58..126).toList()).flatten().map(Int::toChar)
            nonDigits.forEach {
                it("fails with $it") {
                    assertError(digit, it.toString())
                }
            }
        }

        describe("proper state") {
            it("fails at position 0") {
                val p = digit
                val i = "ABC"
                assertError(p, i)
                assertState(p, i, ParserState(0))
            }

            it("fails at position 1") {
                val p = letter andR digit
                val i = "ABC"
                assertError(p, i)
                assertState(p, i, ParserState(1))
            }

            it("fails at position 5") {
                val p = string("test") andR digit
                val i = "testABC"
                assertError(p, i)
                assertState(p, i, ParserState(4))
            }
        }
    }
})