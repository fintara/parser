package com.tsovedenski.parser

/**
 * Created by Tsvetan Ovedenski on 07/01/18.
 */
fun <T> buildParser(block: ParserContext<T>.() -> T): Parser<T> = { input ->
    try {
        val context = ParserContext<T>(input)
        Success(block(context), context.getRest())
    } catch (e: ParserException) {
        Error(e.error.message)
    }
}

class ParserContext<T>(private val input: String) {

    private var rest = input

    fun getRest() = rest

    fun <A> Parser<A>.ev(): A {
        val result = this(rest)
        when (result) {
            is Error<A>   -> throw handleError(result)
            is Success<A> -> return handleSuccess(result)
        }
    }

    private fun <A> handleSuccess(success: Success<A>): A {
        rest = success.rest
        return success.value
    }

    private fun <A> handleError(error: Error<A>): ParserException {
        return ParserException(error)
    }
}

private class ParserException(val error: Error<*>) : Throwable()