/**
 * Created by Tsvetan Ovedenski on 06/01/2018.
 */
sealed class Result <T>
data class Success <T> (val value: T, val rest: String) : Result<T>()
data class Error <T> (val message: String) : Result<T>()
typealias Parser <T> = (String) -> Result<T>

fun <T> parse(parser: Parser<T>, input: String): Result<T> = parser(input)

operator fun <T> Parser<T>.rem(id: String): Parser<T> = { input ->
    val result = this(input)

    when (result) {
        is Error<T>   -> Error("Could not find $id")
        is Success<T> -> result
    }
}

infix fun <A, B> Parser<A>.andThen(other: Parser<B>): Parser<B> = { input ->
    val result = this(input)

    when (result) {
        is Error<A>   -> Error(result.message)
        is Success<A> -> other(result.rest)
    }
}

infix fun <T> Parser<T>.or(other: Parser<T>): Parser<T> = { input ->
    val result = this(input)

    when (result) {
        is Error<T>   -> other(input)
        is Success<T> -> result
    }
}

fun <A, B> fmap(parser: Parser<A>, op: (A) -> B): Parser<B> = { input ->
    val result = parser(input)

    when (result) {
        is Error<A>   -> Error(result.message)
        is Success<A> -> Success(op(result.value), result.rest)
    }
}
