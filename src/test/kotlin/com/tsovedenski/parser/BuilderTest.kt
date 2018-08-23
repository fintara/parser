package com.tsovedenski.parser

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

/**
 * Created by Tsvetan Ovedenski on 08/01/18.
 */
object BuilderTest : Spek({

    describe("summing parser") {
        val sum: Parser<Double> = buildParser {
            val a = number.ev().toDouble()
            char('+').ev()
            val b = unumber.ev().toDouble()
            a + b
        }

        mapOf(
                "-3.5+4" to 0.5,
                "10+20" to 30.0
        ).forEach { input, expected ->
            it("sum '$input'") {
                assertSuccess(sum, input, expected)
            }
        }
    }

    describe("failing parser") {
        val sum: Parser<Int> = buildParser {
            val a = int.ev()
            char('+').ev()
            val b = uint.ev()

            if (b == 6) {
                fail()
            }

            a + b
        }

        it("should not fail when b != 6") {
            assertSuccess(sum, "5+7", 12)
        }

        it("should fail when b = 6") {
            assertError(sum, "5+6")
        }
    }
})