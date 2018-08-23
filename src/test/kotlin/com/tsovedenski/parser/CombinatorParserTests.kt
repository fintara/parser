package com.tsovedenski.parser

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

/**
 * Created by Tsvetan Ovedenski on 07/01/2018.
 */
object CombinatorParserTests : Spek({

    describe("arithmetic expression 1") {
        val tail = chain(oneOf('+', '-', '*', '/'), int)
        val tails = many1(tail).map { it.flatten() }
        val expr = chain(int.map { listOf(it) }, tails).map { it.flatten() }

        assertSuccess(
                expr,
                "12+3*6/2-20",
                listOf(12, '+', 3, '*', 6, '/', 2, '-', 20))
    }

    describe("arithmetic expression 2") {
        val operator = oneOf(*"+-*/^".toCharArray())
        val tail = chain(operator, number)
        val tails = many1(tail).map { it.flatten() }
        val expr = chain(number.map(::listOf), tails).map { it.flatten() }

        assertSuccess(
                expr,
                "-5*20-3.5^2",
                listOf(-5, '*', 20, '-', 3.5, '^', 2)
        )
    }

    describe("floating point number") {
        val value = -87657265.666
        val input = "-8.7657265666E7"
        assertSuccess(float, input, value)
    }

    describe("phone 1") {
        val group = count(3, digit).map { it.joinToString("") }
        val dash  = char('-')
        val phone = chain(group, dash,
                          group, dash,
                          group)

        assertSuccess(
                phone,
                "123-456-789",
                listOf("123", '-', "456", '-', "789")
        )
    }

    describe("phone 2") {
        val group = count(3, digit).map { it.joinToString("") }
        val dash  = char('-')
        val phone = group sepBy1 dash

        assertSuccess(
                phone,
                "123-456-789",
                listOf("123", "456", "789")
        )
    }

    describe("andR") {
        it("fails if left fails") {
            assertError(
                    digit andR string("ABC"),
                    "xABC"
            )
        }

        it("fails if right fails") {
            assertError(
                    digit andR string("XYZ"),
                    "1ABC"
            )
        }

        it("succeeds if all succeed") {
            val expected = "!@#"
            assertSuccess(
                    digit andR string(expected),
                    "1!@#",
                    expected
            )
        }
    }

    describe("andL") {
        it("fails if left fails") {
            assertError(
                    digit andL upper,
                    "AA"
            )
        }

        it("fails if right fails") {
            assertError(
                    digit andL upper,
                    "55"
            )
        }

        it("succeeds if all succeed") {
            assertSuccess(
                    digit andL upper,
                    "5A",
                    '5'
            )
        }
    }

    describe("or") {
        it("succeeds if left succeeds") {
            assertSuccess(
                    digit or upper,
                    "1A",
                    '1',
                    "A"
            )
        }

        it("succeeds if right succeeds") {
            assertSuccess(
                    digit or upper,
                    "AB",
                    'A',
                    "B"
            )
        }

        it("accepts any type as left") {
            assertSuccess(
                    int or upper,
                    "123A",
                    123,
                    "A"
            )
        }

        it("accepts any type as right") {
            assertSuccess(
                    int or upper,
                    "A123",
                    'A',
                    "123"
            )
        }
    }

    describe("all but vowels") {
        val notVowel = noneOf(*"aeiou".toCharArray())

        it("parses non-vowels") {
            assertSuccess(notVowel, "qwerty", 'q', "werty")
        }

        it("fails at parsing vowels") {
            assertError(notVowel, "ayyy")
        }
    }

    describe("all kinds of spaces") {
        val input = "\t  \t"
        assertSuccess(count(4, space), input, input.toList())
    }

    context("integers") {
        val positives = listOf(42, 100, 999)
        val negatives = positives.map { -it }
        val all = positives + negatives

        describe("unsigned integers") {
            positives.forEach {
                it("parses '$it'") {
                    assertSuccess(uint, it.toString(), it)
                }
            }
            negatives.forEach {
                it("not parses '$it'") {
                    assertError(uint, it.toString())
                }
            }
        }

        describe("signed integers") {
            all.forEach {
                it("parses '$it'") {
                    assertSuccess(int, it.toString(), it)
                }
            }
        }
    }

    context("floating-point numbers") {
        describe("floating number with point") {
            listOf(
                    "0.1",
                    "0.12",
                    "0.123",
                    "0.1234",
                    "0.12345",
                    "0.123466",
                    "0.1234567",
                    "0.12345678",
                    "0.123456789",
                    "1.23456789",
                    "12.3456789",
                    "123.456789",
                    "1234.56789",
                    "12345.6789",
                    "123456.789",
                    "1234567.89",
                    "12345678.9"
            ).forEach { input ->
                it("parses '$input'") {
                    assertSuccess(float, input, input.toDouble())
                }
            }
        }

        describe("floating number with e-notation") {
            listOf(
                    "6.5e3"
            ).forEach { input ->
                it("parses '$input'") {
                    assertSuccess(float, input, input.toDouble())
                }
            }
        }
    }

    describe("optional") {
        it("succeeds when inner succeeds") {
            val p = optional(string("test")) andR int
            assertSuccess(p, "test42", 42)
        }

        it("succeeds when inner fails") {
            val p = optional(string("test")) andR int
            assertSuccess(p, "42test1", 42, "test1")
        }
    }

    describe("sepBy") {
        it("succeeds with ' :: '") {
            val input = "1 :: 2 :: 3"
            val p = int sepBy string(" :: ")

            assertSuccess(p, input, listOf(1, 2, 3))
        }

        it("leaves last separator") {
            val input = "1 :: 2 :: "
            val p = int sepBy string(" :: ")

            assertSuccess(p, input, listOf(1, 2), " :: ")
        }
    }

    describe("sepBy1") {
        it("fails if not separated") {
            val input = "xxx"
            val p = int sepBy1 string(" :: ")

            assertError(p, input)
        }
    }

    describe("endBy") {
        it("succeeds") {
            val input = "a;b;c;x"
            val p = lower endBy char(';')

            assertSuccess(p, input, "abc".toList(), "x")
        }

        it("does not leave last separator") {
            val input = "a;b;c;"
            val p = lower endBy char(';')

            assertSuccess(p, input, "abc".toList())
        }
    }

    describe("endBy1") {
        it("removes ending separator") {
            val input = "a?b?c?d"
            val p = lower endBy1 char('?')

            assertSuccess(p, input, "abc".toList(), "d")
        }

        it("fails when left fails") {
            val input = "X?Y?"
            val p = lower endBy1 char('?')

            assertError(p, input)
        }
    }

    describe("choice") {
        it("succeeds when first succeeds") {
            assertSuccess(
                    choice(upper, lower, digit, space),
                    "Abcd",
                    'A',
                    "bcd"
            )
        }

        it("succeeds when mid succeeds") {
            assertSuccess(
                    choice(upper, lower, digit, space),
                    "5bcd",
                    '5',
                    "bcd"
            )
        }

        it("succeeds when last succeeds") {
            assertSuccess(
                    choice(upper, lower, digit, space),
                    " bcd",
                    ' ',
                    "bcd"
            )
        }

        it("fails when all fail") {
            assertError(
                    choice(upper, lower, space),
                    "1bcd"
            )
        }
    }

    describe("between") {
        it("succeds with parens") {
            val brackets = between(char('('), char(')'), int)

            assertSuccess(
                    brackets,
                    "(42)x",
                    42,
                    "x"
            )
        }
    }
})