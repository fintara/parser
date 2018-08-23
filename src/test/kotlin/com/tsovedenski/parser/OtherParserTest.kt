package com.tsovedenski.parser

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.Assert

/**
 * Created by Tsvetan Ovedenski on 09/01/18.
 */
object OtherParserTest : Spek({

    describe("eof") {
        it("succeeds when no more input") {
            val input = "A1"
            val p = upper andF digit andF eof

            assertSuccess(p, input, input.toList())
        }

        it("fails when more input") {
            val input = "A1str"
            val p = upper and digit and eof

            assertError(p, input)
        }
    }

    describe("skipMany") {
        it("skips spaces") {
            val input = "  A    B CD"
            val p = buildParser {
                skipSpaces.ev()
                val a = upper.ev()
                skipSpaces.ev()
                val b = upper.ev()
                skipSpaces.ev()
                val c = upper.ev()
                skipSpaces.ev()
                val d = upper.ev()
                "$a$b$c$d"
            }

            assertSuccess(p, input, "ABCD")
        }

        it("succeeds with many") {
            val input = "ABCD"
            val p = skipMany(digit) andR count(4, upper)

            assertSuccess(p, input, "ABCD".toList())
        }
    }

    describe("skipMany1") {
        it("fails when 0 occurences") {
            val input = "ABCD"
            val p = skipMany1(digit) andR count(4, upper)

            assertError(p, input)
        }

        it("succeeds when >0 occurences") {
            val input = "1234ABCD"
            val p = skipMany1(digit) andR count(4, upper)

            assertSuccess(p, input, "ABCD".toList())
        }
    }

    describe("run parser") {
        it("is not null when succeeds") {
            val result = run(int, "42A")
            Assert.assertEquals(42, result)
        }

        it("is null when fails") {
            val result = run(int, "AA")
            Assert.assertNull(result)
        }
    }

    describe("chain") {
        it("succeeds with empty list") {
            val parsers = listOf<Parser<*>>()
            assertSuccess(parsers.chain(), "abcd", listOf(), "abcd")
        }

        it("succeeds with one parser") {
            val parsers = listOf(digit)
            assertSuccess(parsers.chain(), "1XYZ", "1".toList(), "XYZ")
        }

        it("succeeds with parsers") {
            val parsers = listOf(digit, lower, upper, lower, char('+'))
            assertSuccess(parsers.chain(), "1aBc+XYZ", "1aBc+".toList(), "XYZ")
        }
    }

    describe("error message") {
        val errorMsg = "custom error message"
        val parser = digit % errorMsg

        val result = parse(parser, "")
        it("has new error message") {
            Assert.assertTrue(result is Error)

            result as Error
            Assert.assertEquals(errorMsg, result.message)
        }
    }

    describe("version extract") {
        it("succeeds with many1") {
            val input = "cask: 12.345.678,9000 (auto_update)"
            val expected = "12.345.678,9000"

            val parser = string("cask: ") andR many1(noneOf(' ')).map { it.joinToString("") }
            assertSuccess(parser, input, expected, " (auto_update)")
        }

        it("succeeds with skipSpaces") {
            val input = "cask: 12.345.678,9000 (auto_update)"
            val expected = "12.345.678,9000"

            val parser = many1(noneOf(' ')) andR skipSpaces andR many1(noneOf(' ')).map { it.joinToString("") }
            assertSuccess(parser, input, expected, " (auto_update)")
        }
    }

    describe("lookahead") {
        val parser = lookahead(string("test"))

        it("does not consume input when success") {
            val input = "testing"
            assertSuccess(parser, input, "test", input)
        }

        it("does not consume input when fail") {
            val input = "something"
            assertSuccess(parser.recoverWith { just("FAILED") }, input, "FAILED", input)
        }
    }
})