/**
 * Created by Tsvetan Ovedenski on 06/01/2018.
 */
val takeFirst: Parser<Char> = { input ->
    when (input.isEmpty()) {
        true -> Error("No more input")
        else -> Success(input.first(), input.substring(1))
    }
}

fun satisfy(predicate: (Char) -> Boolean): Parser<Char> = { input ->
    val result = takeFirst(input)

    when (result) {
        is Error -> result
        is Success -> when (predicate(result.value)) {
            true -> result
            else -> Error("Could not satisfy")
        }
    }
}

fun char(wanted: Char) = satisfy { it == wanted } % "'$wanted'"

val any       = satisfy { true }
val space     = satisfy(Char::isWhitespace)    % "space"
val upper     = satisfy(Char::isUpperCase)     % "upper"
val lower     = satisfy(Char::isLowerCase)     % "lower"
val alphaNum  = satisfy(Char::isLetterOrDigit) % "alphanum"
val digit     = satisfy(Char::isDigit)         % "digit"
val letter    = satisfy(Char::isLetter)        % "letter"

fun oneOf(vararg possible: Char) = satisfy { it in possible }   % "one of $possible"
fun noneOf(vararg possible: Char) = satisfy { it !in possible } % "none of $possible"

fun string(wanted: String): Parser<String> = { input ->
    val parser = fmap(count(wanted.length, any)) { it.joinToString("") }
    val result = parser(input)

    when (result) {
        is Error -> result
        is Success -> when (result.value == wanted) {
            true -> result
            else -> Error("Could not match '$wanted'")
        }
    }
}

val int: Parser<Int> = fmap(digit) { it.toString().toInt() }

fun <T> count(number: Int, parser: Parser<T>): Parser<List<T>> = fn@{ input ->
    val accum = mutableListOf<T>()

    var i = 0
    var rest = input
    do {
        val result = parser(rest)
        when (result) {
            is Error<T> -> return@fn Error(result.message)
            is Success<T> -> {
                accum.add(result.value)
                rest = result.rest
            }
        }
    } while (++i < number)

    Success(accum, rest)
}

fun <T> many(parser: Parser<T>): Parser<List<T>> = fn@{ input ->
    val accum =  mutableListOf<T>()
    var rest = input

    do {
        val result = parser(rest)
        if (result is Success) {
            accum.add(result.value)
            rest = result.rest
        }
    } while (result is Success)

    Success(accum, rest)
}

fun <T> many1(parser: Parser<T>): Parser<List<T>> = fn@{ input ->
    val accum =  mutableListOf<T>()

    var result: Result<T>
    var rest = input

    do {
        result = parser(rest)
        if (result is Success) {
            accum.add(result.value)
            rest = result.rest
        }
    } while (result is Success)

    when (accum.isEmpty()) {
        true -> Error((result as Error).message)
        else -> Success(accum, rest)
    }
}
