package com.tsovedenski.parser

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.lang.Math.pow
import kotlin.math.roundToInt

/**
 * Created by Tsvetan Ovedenski on 20/08/2018.
 */
object ExpressionParserTest : Spek({

    describe("proper arithmetic") {
        val table: OperatorTable<Number> = listOf(
                listOf(
                        prefix("-") { x -> -x.toDouble() }
                ),
                listOf(
                        binary("^", Assoc.Left) { x, y -> pow(x.toDouble(), y.toDouble()) }
                ),
                listOf(
                        binary("*", Assoc.Left) { x, y -> x.toDouble() * y.toDouble() },
                        binary("/", Assoc.Left) { x, y -> x.toDouble() / y.toDouble() }
                ),
                listOf(
                        binary("+", Assoc.Left) { x, y -> x.toDouble() + y.toDouble() },
                        binary("-", Assoc.Left) { x, y -> x.toDouble() - y.toDouble() }
                )
        )

        val exprP = buildExpressionParser(table, unumber, parens = true)

        val cases = mapOf(
                "1+2*3" to 7.0,
                "2^3"   to 8.0,

                "-1^2" to 1.0,
                "-1^3" to -1.0,

                "7-4+2"   to 5.0,
                "(7-4)+2" to 5.0,
                "7-(4+2)" to 1.0,

                "10+(2*2)^3-8/2" to 70.0
        )

        cases.forEach { input, expected ->
            it("solves $input = $expected") {
                assertSuccess(exprP, input, expected)
            }
        }
    }

    describe("simple arithmetic") {
        val table: OperatorTable<Int> = listOf(
                listOf(
                        prefix("-") { -it },
                        postfix("++") { it + 1 }
                ),
                listOf(
                        binary("mod", Assoc.Left) { x, y -> x % y }
                ),
                listOf( binary("^", Assoc.Left) { x, y -> pow(x.toDouble(), y.toDouble()).roundToInt() } ),
                listOf( binary("*", Assoc.Left) { x, y -> x * y } ),
                listOf(
                        binary("+", Assoc.Left) { x, y -> x + y },
                        binary("-", Assoc.Left) { x, y -> x - y }
                )
        )

        val exprP = buildExpressionParser(table, uint, parens = true)

        val cases = mapOf(
                "1+2^2*2++" to 13,
                "-2++" to -1,
                "10mod3" to 1,
                "(((((5)))))" to 5
        )

        cases.forEach { input, expected ->
            it("solves $input = $expected") {
                assertSuccess(exprP, input, expected)
            }
        }
    }

    describe("boolean expression") {
        fun <T> binary(symbol: String, f: (T, T) -> T)
                = Infix(between(skipSpaces, skipSpaces, string(symbol)).flatMap { just(f) }, Assoc.Left)
        fun <T> prefix(symbol: String, f: (T) -> T)
                = Prefix(between(skipSpaces, skipSpaces, string(symbol)).flatMap { just(f) })

        val trueP: Parser<Boolean> = oneOf('t', 'T', '1').map { true }
        val falseP: Parser<Boolean> = oneOf('f', 'F', '0').map { false }
        val termP: Parser<Boolean> = between(skipSpaces, skipSpaces, trueP or falseP)

        val table: OperatorTable<Boolean> = listOf(
            listOf( prefix("!") { !it } ),
            listOf( binary("&&") { x, y -> x && y } ),
            listOf( binary("||") { x, y -> x || y } )
        )

        val exprP = buildExpressionParser(table, termP, parens = true)

        // helpers
        fun applyValues(formula: String, map: Map<Char, Boolean>) = map.toList().fold(formula) { acc, (from, to) ->
            acc.replace(from, if (to) 'T' else 'F')
        }

        fun withExpr(formula: String, map: Map<List<Boolean>, Boolean>): Map<String, Boolean>
                = map.mapKeys { (bs, _) -> applyValues(formula, ('a'..'z').zip(bs).toMap()) }

        fun Map<String, Boolean>.testAll() = forEach { input, expected ->
            it("evals $input == $expected") {
                assertSuccess(exprP, input, expected)
            }
        }

        // test cases
        withExpr("a && b", mapOf(
                listOf(false, false) to false,
                listOf(false, true) to false,
                listOf(true, false) to false,
                listOf(true, true) to true
        )).testAll()

        withExpr("a || b", mapOf(
                listOf(false, false) to false,
                listOf(false, true) to true,
                listOf(true, false) to true,
                listOf(true, true) to true
        )).testAll()

        withExpr("a && !b", mapOf(
                listOf(false, false) to false,
                listOf(false, true) to false,
                listOf(true, false) to true,
                listOf(true, true) to false
        )).testAll()

        withExpr("!(!a || !b)", mapOf(
                listOf(false, false) to false,
                listOf(false, true) to false,
                listOf(true, false) to false,
                listOf(true, true) to true
        )).testAll()

        // simplifies to a || b
        withExpr("!a && (a || b) || (b || a && a) && (a || !b)", mapOf(
                listOf(false, false) to false,
                listOf(false, true) to true,
                listOf(true, false) to true,
                listOf(true, true) to true
        )).testAll()

        // simplifies to True
        withExpr("a || !(b && a)", mapOf(
                listOf(false, false) to true,
                listOf(false, true) to true,
                listOf(true, false) to true,
                listOf(true, true) to true
        )).testAll()

        // simplifies to !a
        withExpr("!(a && b) && (!a || b) && (!b || b)", mapOf(
                listOf(false, false) to true,
                listOf(false, true) to true,
                listOf(true, false) to false,
                listOf(true, true) to false
        )).testAll()

        withExpr("a || b || c", mapOf(
                listOf(false, false, false) to false,
                listOf(false, false, true) to true,
                listOf(false, true, false) to true,
                listOf(false, true, true) to true,
                listOf(true, false, false) to true,
                listOf(true, false, true) to true,
                listOf(true, true, false) to true,
                listOf(true, true, true) to true
        )).testAll()
    }
})