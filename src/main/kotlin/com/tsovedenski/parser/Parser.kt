@file:Suppress("UNCHECKED_CAST")

package com.tsovedenski.parser

/**
 * Created by Tsvetan Ovedenski on 06/01/2018.
 */
sealed class Result <out T>
data class Success <out T> (val value: T, val rest: String) : Result<T>()
data class Error (val message: String) : Result<Nothing>()
typealias Parser <T> = (String) -> Result<T>

fun <T> parse(parser: Parser<T>, input: String): Result<T> = parser(input)
fun <T> run(parser: Parser<T>, input: String): T? {
    val result = parse(parser, input)
    return when (result) {
        is Error      -> null
        is Success<T> -> result.value
    }
}

operator fun <T> Parser<T>.rem(message: String): Parser<T> = recover { fail(message) }

infix fun <T> Parser<T>.and(other: Parser<T>): Parser<List<T>> = listOf(this, other).chain()

infix fun <T> Parser<T>.andR(other: Parser<T>): Parser<T> = flatMap { other }

infix fun <T> Parser<T>.andL(other: Parser<T>): Parser<T> = flatMap { x -> other.flatMap { just(x) } }

infix fun <T> Parser<T>.or(other: Parser<T>): Parser<T> = recover(const(other))

fun <A, B> Parser<A>.map(action: (A) -> B): Parser<B> = flatMap { just(action(it)) }

fun <A, B> Parser<A>.flatMap(action: (A) -> Parser<B>): Parser<B> = flatMap(action) { e -> fail(e.message) }

private inline fun <A, B> Parser<A>.flatMap(
        crossinline success: (A) -> Parser<B>,
        crossinline error: (Error) -> Parser<B>
): Parser<B> = { input ->
    val result = this(input)

    when (result) {
        is Error      -> error(result)(input)
        is Success<A> -> success(result.value)(result.rest)
    }
}

fun <T> Parser<T>.recover(action: (Error) -> Parser<T>): Parser<T> = flatMap(::just, action)

fun <T> List<Parser<T>>.chain(): Parser<List<T>> {
    if (this.isEmpty()) {
        return just(listOf())
    }

    val first = first()
    val rest = drop(1)

    return first.flatMap { x ->
        rest.chain().flatMap { xs ->
            just(listOf(x) + xs)
        }
    }
}

private fun <T> flatten(fst: T, snd: T): List<T> {
    val list = when {
        fst is List<*> && snd is List<*> -> fst + snd
        fst is List<*>                   -> fst + listOf(snd)
        snd is List<*>                   -> listOf(fst) + snd
        else                             -> listOf(fst, snd)
    } as List<T>

    return list.filter { it != Unit }
}

private fun <A, B> const(a: A): (B) -> A = { _ -> a }