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
* `parserA and parserB` - returns both results of parserA and parserB
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

## Example
This is how to build a parser for simple arithmetic expressions.

An arithmetic expression is a number that is followed by one or more "tails" (operator followed by a number).
```kotlin
val tail = oneOf('+', '-', '*', '/') and integer
val tails = many1(tail).map { it.flatten() }
val expr = integer and tails

val result = parse(expr, "12+6")
// should be Success(value=[12, '+', 6], rest=)
```
