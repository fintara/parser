Parser
======

An attempt to implement parser combinators in Kotlin, inspired by Parsec.

## Parsers

### Basic parsers
* `space` - any whitespace
* `upper` - any uppercase letter
* `lower` - any lowercase letter
* `letter` - any letter
* `digit` - any digit 0..9
* `alphaNum` - any letter or digit
* `int` - an integer
* `float` - a floating-point number (decimal or e-notation)
* `number` - a floating-point or integer number

### Configurable parsers
* `oneOf(list)` - any character in the provided list
* `noneOf(list)` - character should not be in the provided list
* `char(ch)` - character equal to the provided one
* `string(str)` - string equal to the provided one
* `satisfy(pred)` - character for which provided predicate returns true

### Combinators
* `count(number, parser)` - repeats `parser` as many times as stated
* `many(parser)` - repeats `parser` until error occurs
* `many1(parser)` - repeats `parser` until error occurs, should succeed at least once
* `just(value)` - always returns `value`
* `option(value, parser)` - returns `value` if `parser` fails
* `parserA and parserB` - returns both results of `parserA` and `parserB`
* `parserA then parserB` - returns result of `parserB` only if `parserA` succeeds
* `parserA or parserB` - tries to run `parserA` and if fails, returns result of `parserB`
* `parser.map { ... }` - apply function to the result of `parser`

### Misc
* `parser % "Error message"` - returns a parser with `"Error message"` for error

## How to use
In order to parse a string with some parser, `parse(parser, input)` function should be used.
It returns a `Result<T>` which can be either a `Success` (contains the parsed value and rest of the input) 
or an `Error` (contains error message).

```kotlin
val input = "ABCD1234"
val result = parse(letter, input)

when (result) {
    is Error<*> -> println(result.message)
    is Success<*> -> println(result.value)
}
// should print 'A'
```

## Parser builder
In addition to the combinators `and`/`then`/`or`, as well as the modifier `map`, 
there is a way to build parsers in declarative style.

`buildParser` creates a context which takes care of passing the rest of the input 
from one parser to the other upon calling `.ev()`, all while taking care of whether `Success` or `Error` was returned.
In case of the latter, execution is stopped and the error is returned.

## Examples
### Arithmetic expressions
This is how to build a parser for simple arithmetic expressions.

An arithmetic expression is a number that is followed by one or more "tails" (operator followed by a number).
```kotlin
val tail = oneOf('+', '-', '*', '/') and number
val tails = many1(tail).map { it.flatten() }
val expr = number and tails

val result = parse(expr, "12+6.5")
// should be Success(value=[12, '+', 6.5], rest=)
```

### Parser builder
An easy way to combine different parsers and their results.

This parser would take two numbers and sum them together.
```kotlin
val sum = buildParser {
    val a = int.ev()
    char('+').ev()
    val b = uint.ev()
    a + b
}

val result = parse(sum, "3+4")
// should be Success(value=7, rest=)
```
