Parser
======

[![Build Status](https://travis-ci.org/fintara/parser.svg?branch=master)](https://travis-ci.org/fintara/parser)

An attempt to implement parser combinators in Kotlin, inspired by Parsec.

## Parsers

### Basic parsers
* `space` - any whitespace
* `upper` - any uppercase letter
* `lower` - any lowercase letter
* `letter` - any letter
* `digit` - any digit 0..9
* `alphaNum` - any letter or digit
* `uint/int` - an integer (unsigned or signed)
* `ulong/long` - a long (unsigned or signed)
* `ufloat/float` - a floating-point number (decimal or e-notation, unsigned or signed)
* `unumber/number` - a floating-point or integer number
* `eof` - end of input
* `skipSpaces` - skip arbitrary number of spaces

### Configurable parsers
* `oneOf(list)` - any character in the provided list
* `noneOf(list)` - character should not be in the provided list
* `char(ch)` - character equal to the provided one
* `string(str)` - string equal to the provided one
* `satisfy(pred)` - character for which provided predicate returns true

### Combinators
* `List/Pair/Triple(parsers).chain()` - inverts the structure and returns a parser of `List/Pair/Triple` with chained results 
* `count(number, parser)` - repeats `parser` as many times as stated
* `choice(list)` - tries list of parsers in order, returning the first success
* `chain(parsers)` - executes a list of parsers and returns a list of their results
* `between(open, close, parser)` - runs `open`, then `parser` and `close`, returning the result of `parser`
* `many(parser)` - repeats `parser` 0+ times until error occurs
* `many1(parser)` - repeats `parser` 1+ times until error occurs
* `skipMany(parser)` - repeats `parser` 0+ times but does not return any result
* `skipMany1(parser)` - repeats `parser` 1+ times but does not return any result
* `parser sepBy sep` - repeats `parser` 0+ times, separated by `sep`
* `parser sepBy1 sep` - repeats `parser` 1+ times, separated by `sep`
* `parser endBy sep` - like `sepBy`, but input must also end with `sep`
* `parser endBy1 sep` - like `sepBy1`, but input must also end with `sep`
* `just(value)` - always returns `value`
* `fail(message)` - always fails with `message`
* `option(value, parser)` - returns `value` if `parser` fails
* `optional(parser)` - tries to run `parser` but does not return any result (does not fail)
* `lookahead(parser)` - executes `parser` without consuming any input
* `pA and pB` - returns both results of parsers `pA` and `pB` as a pair
* `pA andF pB` - (and-flat) returns both results of parsers `pA` and `pB` flattened to a list without `Unit`s
* `pA andL pB` - (and-left) returns result of `pA` (left parser) only if `pB` succeeds
* `pA andR pB` - (and-right) returns result of `pB` (right parser) only if `pA` succeeds
* `pA or pB` - tries to run `pA` and if fails, returns result of `pB`
* `parser.map { ... }` - apply function to the result of `parser`
* `parser.flatMap { ... }` - use the successful result of `parser` and return a new parser
* `parser.recoverWith { ... }` - use the error of `parser` and return a new parser

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
    is Error   -> println(result.message)
    is Success -> println(result.value)
}
// should print 'A'
```

Another possibility is to use `run(parser, input)` that directly returns the value (if succeeded) or `null` (if error).

```kotlin
val input = "42A"
val result = run(int, input)
// should be 42
```

```kotlin
val tail = oneOf('+', '-', '*', '/') andF number
val tails = many1(tail).map { it.flatten() }
val expr = number andF tails

val result = run(expr, "12+6.5")
// should be [12, '+', 6.5]
```


## Parser builder
In addition to the combinators `and`/`andL`/`andR`/`or`, as well as the modifier `map`, 
there is a way to build parsers in declarative style.

`buildParser` creates a context which takes care of passing the rest of the input 
from one parser to the other upon calling `.ev()`, all while tracking the return value (`Success` or `Error`) of each one.
In case of the latter, execution is stopped and the error is returned.

#### Example
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


## Expression parser builder
There is a possibility to create a parser for an expression that also evaluates it.
It is only necessary to provide an operators table where all operators are listed and their functions defined.
Operators table is a list of Operator lists ordered in descending precedence.

#### Example
In this example we support incrementation, multiplication and addition/subtraction,
with `++` having the highest precedence.
```kotlin
val table: OperatorTable<Int> = listOf(
    listOf( postfix("++") { it + 1 } ),
    listOf( binary("*", Assoc.Left) { x, y -> x * y } ),
    listOf(
        binary("+", Assoc.Left) { x, y -> x + y },
        binary("-", Assoc.Left) { x, y -> x - y }
    )
)

val expr = buildExpressionParser(table, uint)
run(expr, "1+2*3") // should be 7
run(expr, "2++") // should be 3
```
