package com.tsovedenski.parser

import kotlin.coroutines.experimental.*

/**
 * Created by Tsvetan Ovedenski on 07/01/18.
 */
fun <T> buildParser(block: suspend ParserContext.() -> T): Parser<T> = { input ->
    lateinit var result: Result<T>

    val context = ParserContext(input)

    val completion = object : Continuation<T> {
        override val context = EmptyCoroutineContext

        override fun resume(value: T) {
            result = Success(value, context.rest)
        }

        override fun resumeWithException(exception: Throwable) = when (exception) {
            is ParserException -> result = exception.error
            else               -> throw exception
        }
    }

    block.startCoroutine(context, completion)

    result
}

class ParserContext(input: String) {
    var rest = input
        private set

    suspend fun <A> Parser<A>.ev(): A = suspendCoroutine { c ->
        val result = this(rest)
        when (result) {
            is Error   -> c.resumeWithException(handleError(result))
            is Success -> c.resume(handleSuccess(result))
        }
    }

    suspend fun fail(message: String = "Fail"): Nothing = suspendCoroutine { c ->
        c.resumeWithException(handleError(Error(message)))
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