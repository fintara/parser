package com.tsovedenski.parser

/**
 * Created by Tsvetan Ovedenski on 07/01/18.
 */
fun <T> buildParser(block: ParserContext.() -> T): Parser<T> = { input, state ->
    try {
        val context = ParserContext(input, state)
        Success(block(context), context.getRest(), context.getState())
    } catch (e: ParserException) {
        e.error
    }
}

class ParserContext(input: String, initState: ParserState) {

    private var rest = input
    private var state = initState

    fun getRest() = rest
    fun getState() = state

    fun <A> Parser<A>.ev(): A {
        val result = this(rest, state)
        when (result) {
            is Error      -> throw handleError(result)
            is Success<A> -> return handleSuccess(result)
        }
    }

    fun fail(message: String = "Fail"): Nothing {
        throw handleError(Error(message, state))
    }

    private fun <A> handleSuccess(success: Success<A>): A {
        rest = success.rest
        state = success.state
        return success.value
    }

    private fun handleError(error: Error): ParserException {
        return ParserException(error)
    }
}

private class ParserException(val error: Error) : Throwable()