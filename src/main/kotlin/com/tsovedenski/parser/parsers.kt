@file:Suppress("UNCHECKED_CAST")

package com.tsovedenski.parser

import kotlin.math.pow

/**
 * Created by Tsvetan Ovedenski on 06/01/2018.
 */
val any: Parser<Char> = { input ->
    when (input.isEmpty()) {
        true -> Error("end of input")
        else -> Success(input.first(), input.substring(1))
    }
}

fun <T> just(value: T): Parser<T> = { input ->
    Success(value, input)
}

fun fail(message: String): Parser<Nothing> = { _ ->
    Error(message)
}

fun satisfy(predicate: (Char) -> Boolean): Parser<Char> {
    return any.flatMap {
        if (predicate(it)) {
            just(it)
        } else {
            fail("satisfy")
        }
    }
}

fun <T> option(default: T, parser: Parser<T>) = parser or just(default)

fun <T> optional(parser: Parser<T>): Parser<Unit> = parser
        .map { Unit }
        .recover { just(Unit) }

fun char(wanted: Char) = satisfy { it == wanted } % "'$wanted'"

val space     = satisfy(Char::isWhitespace) % "space"
val upper     = satisfy(Char::isUpperCase) % "upper"
val lower     = satisfy(Char::isLowerCase) % "lower"
val alphaNum  = satisfy(Char::isLetterOrDigit) % "alpha-numeric"
val letter    = satisfy(Char::isLetter) % "letter"
val digit     = satisfy(Char::isDigit) % "digit"

val uint: Parser<Int> = many1(digit).map { it.joinToString("").toInt() }
val int: Parser<Int> = buildParser {
    val sign = option('x', char('-')).ev()
    val number = uint.ev()
    when (sign) {
        '-'  -> -number
        else -> number
    }
}

private val floatP: Parser<Double> = buildParser {
    val base = int.ev()
    char('.').ev()
    val frac = int.ev()
    "$base.$frac".toDouble()
}

private val floatE: Parser<Double> = buildParser {
    val base = floatP.ev()
    oneOf('e', 'E').ev()
    val exp  = int.ev()
    base * (10.0).pow(exp)
}

val ufloat: Parser<Double> = (floatE or floatP) % "floating number"
val float: Parser<Double> = buildParser {
    val sign = option('x', char('-')).ev()
    val number = ufloat.ev()
    when (sign) {
        '-'  -> -number
        else -> number
    }
}

val unumber = (ufloat or uint) as Parser<Number> % "positive real number"
val number = (float or int) as Parser<Number> % "real number"

val eof: Parser<Unit> = { input ->
    val result = any(input)
    when (result) {
        is Error   -> Success(Unit, "")
        is Success -> Error("end of input")
    }
}

fun oneOf(vararg possible: Char) = oneOf(possible.toList())
fun oneOf(possible: List<Char>) = satisfy { it in possible } % "one of $possible"
fun noneOf(vararg possible: Char) = noneOf(possible.toList())
fun noneOf(possible: List<Char>) = satisfy { it !in possible } % "none of $possible"

fun string(wanted: String): Parser<String> {
    val parser = count(wanted.length, any).map { it.joinToString("") }
    return parser.flatMap { word ->
        when (word) {
            wanted -> just(word)
            else   -> fail("Could not match '$wanted'")
        }
    }
}

fun <T> count(number: Int, parser: Parser<T>): Parser<List<T>> {
    if (number <= 0) {
        return just(listOf())
    }
    return List(number) { parser }.chain()
}

infix fun <T, S> Parser<T>.sepBy(sep: Parser<S>): Parser<List<T>> = (this sepBy1 sep) or just(listOf())
infix fun <T, S> Parser<T>.sepBy1(sep: Parser<S>): Parser<List<T>> = (this and many(sep andR this)) as Parser<List<T>>

infix fun <T, S> Parser<T>.endBy(sep: Parser<S>): Parser<List<T>> = many(this andL sep) as Parser<List<T>>
infix fun <T, S> Parser<T>.endBy1(sep: Parser<S>): Parser<List<T>> = many1(this andL sep) as Parser<List<T>>

private fun <T> manyAccum(parser: Parser<T>, list: MutableList<T>): Parser<List<T>> {
    return parser
            .flatMap {
                list.add(it)
                manyAccum(parser, list)
            }
            .recover { just(list) }
}
fun <T> many(parser: Parser<T>): Parser<List<T>> = manyAccum(parser, mutableListOf())
fun manyString(parser: Parser<Char>): Parser<String> = many(parser).map { it.joinToString("") }

fun <T> many1(parser: Parser<T>): Parser<List<T>> = parser.flatMap { x -> many(parser).flatMap { xs -> just(listOf(x) + xs) } }
fun many1String(parser: Parser<Char>): Parser<String> = many1(parser).map { it.joinToString("") }

fun <T> skipMany(parser: Parser<T>): Parser<Unit> = parser.flatMap { skipMany(parser) }.recover { just(Unit) }

fun <T> skipMany1(parser: Parser<T>): Parser<Unit> = parser.flatMap { skipMany(parser).recover { just(Unit) } }

val skipSpaces = skipMany(space)

fun <T> choice(vararg parsers: Parser<T>): Parser<T> = choice(parsers.toList())
fun <T> choice(parsers: List<Parser<T>>): Parser<T> {
    if (parsers.isEmpty()) {
        return fail("choice")
    }

    return parsers.first().recover { choice(parsers.drop(1)) }
}

fun <O,T,C> between(open: Parser<O>, close: Parser<C>, parser: Parser<T>): Parser<T>
        = (open andR parser andL close) as Parser<T>

fun <T> lookahead(parser: Parser<T>): Parser<T> = { input ->
    val result = parser(input)

    when (result) {
        is Error   -> result
        is Success -> result.copy(rest = input)
    }
}
