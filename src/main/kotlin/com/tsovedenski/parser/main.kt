package com.tsovedenski.parser

/**
 * Created by Tsvetan Ovedenski on 28/11/2017.
 */
fun main(args: Array<String>) {
    val input = "1ABCD1234"

    val anyLetterString = many1(letter).map { it.joinToString("") }
    val parsed = parse(anyLetterString, input)

    when (parsed) {
        is Error<*> -> println(parsed.message)
        is Success<*> -> println(parsed)
    }
}
