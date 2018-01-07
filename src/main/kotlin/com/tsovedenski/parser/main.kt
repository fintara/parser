package com.tsovedenski.parser

/**
 * Created by Tsvetan Ovedenski on 28/11/2017.
 */
fun main(args: Array<String>) {
    val input = "200-300"

    val phone = count(3, digit) and
                char('-') and
                count(3, digit)

    val parsed = parse(phone, input)

    when (parsed) {
        is Error<*> -> println(parsed.message)
        is Success<*> -> println(parsed)
    }
}
