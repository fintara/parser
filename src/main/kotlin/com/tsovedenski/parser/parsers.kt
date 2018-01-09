@file:Suppress("UNCHECKED_CAST")

package com.tsovedenski.parser

import kotlin.math.pow

/**
 * Created by Tsvetan Ovedenski on 06/01/2018.
 */
val takeFirst: Parser<Char> = { input ->
    when (input.isEmpty()) {
        true -> Error("end of input")
        else -> Success(input.first(), input.substring(1))
    }
}

fun <T> just(value: T): Parser<T> = { input ->
    Success(value, input)
}

fun satisfy(predicate: (Char) -> Boolean): Parser<Char> = { input ->
    val result = takeFirst(input)

    when (result) {
        is Error -> result
        is Success -> when (predicate(result.value)) {
            true -> result
            else -> Error("satisfy")
        }
    }
}

fun <T> option(default: T, parser: Parser<T>) = parser or just(default)

fun char(wanted: Char) = satisfy { it == wanted } % "'$wanted'"

val any       = takeFirst
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
        '-' -> -number
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
        '-' -> -number
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

fun symbol(wanted: String): Parser<String> = { input ->
    val parser = count(wanted.length, any).map { it.joinToString("") }
    val result = parser(input)

    when (result) {
        is Error -> result
        is Success -> when (result.value == wanted) {
            true -> result
            else -> Error("Could not match '$wanted'")
        }
    }
}

fun <T> count(number: Int, parser: Parser<T>): Parser<List<T>> = fn@{ input ->
    val accum = mutableListOf<T>()

    var i = 0
    var rest = input
    do {
        val result = parser(rest)
        when (result) {
            is Error<T>   -> return@fn Error(result.message)
            is Success<T> -> {
                accum.add(result.value)
                rest = result.rest
            }
        }
    } while (++i < number)

    Success(accum, rest)
}

fun <T> many(parser: Parser<T>): Parser<List<T>> = fn@{ input ->
    val accum = mutableListOf<T>()
    var rest  = input

    do {
        val result = parser(rest)
        if (result is Success) {
            accum.add(result.value)
            rest = result.rest
        }
    } while (result is Success)

    Success(accum, rest)
}

fun <T> many1(parser: Parser<T>): Parser<List<T>> = fn@{ input ->
    val accum =  mutableListOf<T>()

    var result: Result<T>
    var rest = input

    do {
        result = parser(rest)
        if (result is Success) {
            accum.add(result.value)
            rest = result.rest
        }
    } while (result is Success)

    when (accum.isEmpty()) {
        true -> Error((result as Error).message)
        else -> Success(accum, rest)
    }
}

fun <T> skipMany(parser: Parser<T>): Parser<Unit> = fn@{ input ->
    var rest = input
    do {
        val result = parser(rest)
        if (result is Success) {
            rest = result.rest
        }
    } while (result is Success)

    Success(Unit, rest)
}

fun <T> skipMany1(parser: Parser<T>): Parser<Unit> = fn@{ input ->
    var more1 = false
    var rest = input
    do {
        val result = parser(rest)
        if (result is Success) {
            more1 = true
            rest = result.rest
        }
    } while (result is Success)

    when (more1) {
        false -> Error("skip many1")
        else -> Success(Unit, rest)
    }
}

val skipSpaces = skipMany(space)
