package com.tsovedenski.parser

/**
 * Created by Tsvetan Ovedenski on 20/08/2018.
 * Adapted from: http://hackage.haskell.org/package/parsec-3.1.13.0/docs/Text-Parsec-Expr.html
 */
enum class Assoc {
    None, Left, Right
}

typealias UnaryParser <T> = Parser<(T) -> T>
typealias BinaryParser <T> = Parser<(T, T) -> T>

sealed class Operator <T>
data class Infix <T> (val parser: BinaryParser<T>, val assoc: Assoc) : Operator<T>()
data class Prefix <T> (val parser: UnaryParser<T>) : Operator<T>()
data class Postfix <T> (val parser: UnaryParser<T>) : Operator<T>()

fun <T> binary(symbol: String, assoc: Assoc, f: (T, T) -> T): Infix<T> =
    Infix(string(symbol).flatMap { just(f) }, assoc)

fun <T> prefix(symbol: String, f: (T) -> T): Prefix<T> =
    Prefix(string(symbol).flatMap { just(f) })

fun <T> postfix(symbol: String, f: (T) -> T): Postfix<T> =
    Postfix(string(symbol).flatMap { just(f) })

typealias OperatorTable <T> = List<List<Operator<T>>>

// todo: see ExpressionParserBuilderTest::mkParser
//fun <T> buildExpressionParser(operators: OperatorTable<T>, simpleExpr: Parser<T>): Parser<T>
//    = operators.fold(simpleExpr, ::makeParser)

fun <T> buildExpressionParser(operators: OperatorTable<T>): (Parser<T>) -> Parser<T>
    = { simpleExpr -> operators.fold(simpleExpr, ::makeParser) }

private fun <T> makeParser(term: Parser<T>, ops: List<Operator<T>>): Parser<T> {
    val pentuple= ops.foldRight(Pentuple(), ::splitOp)

    val rassocOp = choice(pentuple.rassoc)
    val lassocOp = choice(pentuple.lassoc)
    val nassocOp = choice(pentuple.nassoc)
    val prefixOp = choice(pentuple.prefix)
    val postfixOp = choice(pentuple.postfix)

    val ambiguousRight = ambiguous(Assoc.Right, rassocOp)
    val ambiguousLeft = ambiguous(Assoc.Left, lassocOp)
    val ambiguousNone = ambiguous(Assoc.None, nassocOp)

    val prefixP = prefixOp or just(::id)
    val postfixP = postfixOp or just(::id)

    val termP = buildParser {
        val pre = prefixP.ev()
        val x = term.ev()
        val post = postfixP.ev()
        post(pre(x))
    }

    val rassocP: (T) -> Parser<T> = { rassocP(rassocOp, termP, it) or ambiguousLeft or ambiguousNone }
    val lassocP: (T) -> Parser<T> = { lassocP(lassocOp, termP, it) or ambiguousRight or ambiguousNone }
    val nassocP: (T) -> Parser<T> = { x -> buildParser {
        val f = nassocOp.ev()
        val y = termP.ev()
        (ambiguousRight or ambiguousLeft or ambiguousNone or just(f(x, y))).ev()
    }}

    return buildParser {
        val x = termP.ev()
        (rassocP(x) or lassocP(x) or nassocP(x) or just(x)).ev()
    }
}

private fun <T> rassocP(rassocOp: BinaryParser<T>, termP: Parser<T>, x: T): Parser<T> = buildParser {
    val f = rassocOp.ev()
    val y = buildParser {
        val z = termP.ev()
        rassocP1(rassocOp, termP, z).ev()
    }.ev()
    f(x, y)
}

private fun <T> rassocP1(rassocOp: BinaryParser<T>, termP: Parser<T>, x: T): Parser<T>
        = rassocP(rassocOp, termP, x) or just(x)

private fun <T> lassocP(lassocOp: BinaryParser<T>, termP: Parser<T>, x: T): Parser<T> = buildParser {
    val f = lassocOp.ev()
    val y = termP.ev()
    lassocP1(lassocOp, termP, f(x, y)).ev()
}

private fun <T> lassocP1(lassocOp: BinaryParser<T>, termP: Parser<T>, x: T): Parser<T>
        = lassocP(lassocOp, termP, x) or just(x)

private fun <T> ambiguous(assoc: Assoc, parser: Parser<T>): Parser<Nothing> = buildParser {
    parser.ev()
    fail("ambiguous use of $assoc associative operator")
}

private data class Pentuple <T> (
    val rassoc: List<BinaryParser<T>> = listOf(),
    val lassoc: List<BinaryParser<T>> = listOf(),
    val nassoc: List<BinaryParser<T>> = listOf(),
    val prefix: List<UnaryParser<T>> = listOf(),
    val postfix: List<UnaryParser<T>> = listOf()
)

@Suppress("UNCHECKED_CAST")
private fun <T> splitOp(op: Operator<T>, lists: Pentuple<T>): Pentuple<T> = when (op) {
    is Infix<*>   -> when (op.assoc) {
        Assoc.None -> lists.copy(nassoc = lists.nassoc.prepend(op.parser as BinaryParser<T>))
        Assoc.Left -> lists.copy(lassoc = lists.lassoc.prepend(op.parser as BinaryParser<T>))
        Assoc.Right -> lists.copy(rassoc = lists.rassoc.prepend(op.parser as BinaryParser<T>))
    }
    is Prefix<*>  -> lists.copy(prefix = lists.prefix.prepend(op.parser as UnaryParser<T>))
    is Postfix<*> -> lists.copy(postfix = lists.postfix.prepend(op.parser as UnaryParser<T>))
}

// todo: must be prepend or can be append?
private fun <T> List<T>.prepend(obj: T): List<T> = listOf(obj) + this

private fun <T> id(a: T): T = a