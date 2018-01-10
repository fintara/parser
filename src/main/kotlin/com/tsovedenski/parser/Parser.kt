package com.tsovedenski.parser

/**
 * Created by Tsvetan Ovedenski on 06/01/2018.
 */
sealed class Result <out T>
data class Success <out T> (val value: T, val rest: String) : Result<T>()
data class Error <out T> (val message: String) : Result<T>()
typealias Parser <T> = (String) -> Result<T>

fun <T> parse(parser: Parser<T>, input: String): Result<T> = parser(input)
fun <T> run(parser: Parser<T>, input: String): T? {
    val result = parse(parser, input)
    return when (result) {
        is Error<T>   -> null
        is Success<T> -> result.value
    }
}

operator fun <T> Parser<T>.rem(message: String): Parser<T> = { input ->
    val result = this(input)

    when (result) {
        is Error<T>   -> Error(message)
        is Success<T> -> result
    }
}

infix fun <T> Parser<T>.and(other: Parser<T>): Parser<List<T>> = { input ->
    val first = this(input)

    when (first) {
        is Error<T>   -> Error(first.message)
        is Success<T> -> {
            val second = other(first.rest)
            when (second) {
                is Error<T>   -> Error(second.message)
                is Success<T> -> Success(flatten(first.value, second.value), second.rest)
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
private fun <T> flatten(fst: T, snd: T): List<T> {
    val list = when {
        fst is List<*> && snd is List<*> -> fst + snd
        fst is List<*>                   -> fst + listOf(snd)
        snd is List<*>                   -> listOf(fst) + snd
        else                             -> listOf(fst, snd)
    } as List<T>

    return list.filter { it != Unit }
}

infix fun <T> Parser<T>.andR(other: Parser<T>): Parser<T> = { input ->
    val result = this(input)

    when (result) {
        is Error<T>   -> result
        is Success<T> -> other(result.rest)
    }
}

infix fun <T> Parser<T>.andL(other: Parser<T>): Parser<T> = { input ->
    val first = this(input)

    when (first) {
        is Error<T>   -> first
        is Success<T> -> {
            val second = other(first.rest)
            when (second) {
                is Error<T>   -> second
                is Success<T> -> first.copy(rest = second.rest)
            }
        }
    }
}

infix fun <T> Parser<T>.or(other: Parser<T>): Parser<T> = { input ->
    val result = this(input)

    when (result) {
        is Error<T>   -> other(input)
        is Success<T> -> result
    }
}

fun <A, B> Parser<A>.map(action: (A) -> B): Parser<B> = { input ->
    val result = this(input)

    when (result) {
        is Error<A>   -> Error(result.message)
        is Success<A> -> Success(action(result.value), result.rest)
    }
}
