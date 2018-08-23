package com.tsovedenski.parser

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

/**
 * Created by Tsvetan Ovedenski on 06/01/2018.
 */
object CountParserTest : Spek({

    fun <T> testSuccess(parser: Parser<T>, number: Int, input: String, expected: List<T>, expectedRest: String)
            = assertSuccess(count(number, parser), input, expected, expectedRest)

    fun <T> testError(parser: Parser<T>, number: Int, input: String)
            = assertError(count(number, parser), input)

    context("count") {
        it("parses 0 to empty list") {
            testSuccess(upper, 0, "ABCD", listOf(), "ABCD")
        }

        it("parses 3 digits") {
            testSuccess(
                    digit.map { it.toString().toInt() },
                    3,
                    "681QWE",
                    listOf(6, 8, 1),
                    "QWE"
            )
        }

        it("parses 5 upper") {
            testSuccess(upper, 5, "ABCDE12345", "ABCDE".toList(), "12345")
        }

        it("fails when less than 5 lowers") {
            testError(lower, 5, "ABCDE12345")
        }

        it("fails when less than 3 letters") {
            testError(letter, 3, "AB123")
        }
    }
})