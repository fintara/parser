package com.tsovedenski.parser

import org.junit.Test
import java.lang.Math.pow
import kotlin.math.roundToInt

/**
 * Created by Tsvetan Ovedenski on 20/08/2018.
 */
class ExpressionParserBuilderTest {

    @Test
    fun `arithmetic`() {
        val table: OperatorTable<Number> = listOf(
                listOf(
                        prefix("-") { x -> -x.toDouble() }
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

        val expr = mkParser(table, unumber)

        val input = "1+2*3"
        val expected = 7.0

        assertSuccess(expr, input, expected)
    }

    @Test
    fun `simple arithmetic`() {
        val table: OperatorTable<Int> = listOf(
                listOf( postfix("++") { it + 1 } ),
                listOf( binary("^", Assoc.Left) { x, y -> pow(x.toDouble(), y.toDouble()).roundToInt() } ),
                listOf( binary("*", Assoc.Left) { x, y -> x * y } ),
                listOf(
                        binary("+", Assoc.Left) { x, y -> x + y },
                        binary("-", Assoc.Left) { x, y -> x - y }
                )
        )

        val expr = mkParser(table, uint)

        val input = "1+2^2*2++"
        val expected = 13

        assertSuccess(expr, input, expected)
    }

    // todo: ugly, how to use expr and term recursively?
    private fun <T> mkParser(table: OperatorTable<T>, natural: Parser<T>): Parser<T> {
        val expr = buildExpressionParser(table)
        var term : Parser<T> = fail("")
        term = term or parens(expr(term)) or natural
        return expr(term)
    }
}