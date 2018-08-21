package com.tsovedenski.parser

import org.junit.Test
import java.lang.Math.pow
import kotlin.math.roundToInt

/**
 * Created by Tsvetan Ovedenski on 20/08/2018.
 */
class ExpressionParserTest {

    @Test
    fun `proper arithmetic`() {
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
            assertSuccess(exprP, input, expected)
        }
    }

    @Test
    fun `simple arithmetic`() {
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
            assertSuccess(exprP, input, expected)
        }
    }
}

