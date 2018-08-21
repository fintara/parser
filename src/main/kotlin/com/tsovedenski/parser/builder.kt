package com.tsovedenski.parser

/**
 * Created by Tsvetan Ovedenski on 07/01/18.
 */
fun <T> buildParser(block: ParserContext.() -> T): Parser<T> = { input ->
    try {
        val context = ParserContext(input)
        Success(block(context), context.getRest())
    } catch (e: ParserException) {
        e.error
    }
}

class ParserContext(input: String) {

    private var rest = input

    fun getRest() = rest

    fun <A> Parser<A>.ev(): A {
        val result = this(rest)
        when (result) {
            is Error      -> throw handleError(result)
            is Success<A> -> return handleSuccess(result)
        }
    }

    fun fail(message: String = "Fail"): Nothing {
        throw handleError(Error(message))
    }

    private fun <A> handleSuccess(success: Success<A>): A {
        rest = success.rest
        return success.value
    }

    private fun handleError(error: Error): ParserException {
        return ParserException(error)
    }
}

private class ParserException(val error: Error) : Throwable()