package com.tsovedenski.parser

/**
 * Created by Tsvetan Ovedenski on 06/01/2018.
 */
sealed class Result <out T>
data class Success <out T> (val value: T, val rest: String) : Result<T>()
data class Error <out T> (val message: String) : Result<T>()
typealias Parser <T> = (String) -> Result<T>

fun <T> parse(parser: Parser<T>, input: String): Result<T> = parser(input)

operator fun <T> Parser<T>.rem(message: String): Parser<T> = { input ->
    val result = this(input)

    when (result) {
        is Error<T> -> Error(message)
        is Success<T> -> result
    }
}

@Suppress("UNCHECKED_CAST")
infix fun <T> Parser<T>.and(other: Parser<T>): Parser<List<T>> = { input ->
    val first = this(input)

    when (first) {
        is Error<T> -> Error(first.message)
        is Success<T> -> {
            val second = other(first.rest)
            when (second) {
                is Error<T> -> Error(second.message)
                is Success<T> -> Success(when {
                    first.value is List<*> && second.value is List<*> -> first.value + second.value
                    first.value is List<*> -> first.value + listOf(second.value)
                    second.value is List<*> -> listOf(first.value) + second.value
                    else -> listOf(first.value, second.value)
                } as List<T>, second.rest)
            }
        }
    }
}

infix fun <A, B> Parser<A>.then(other: Parser<B>): Parser<B> = { input ->
    val result = this(input)

    when (result) {
        is Error<A> -> Error(result.message)
        is Success<A> -> other(result.rest)
    }
}

infix fun <T> Parser<T>.or(other: Parser<T>): Parser<T> = { input ->
    val result = this(input)

    when (result) {
        is Error<T> -> other(input)
        is Success<T> -> result
    }
}

fun <A, B> Parser<A>.map(op: (A) -> B): Parser<B> = { input ->
    val result = this(input)

    when (result) {
        is Error<A> -> Error(result.message)
        is Success<A> -> Success(op(result.value), result.rest)
    }
}
