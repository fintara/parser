/**
 * Created by Tsvetan Ovedenski on 28/11/2017.
 */
fun main(args: Array<String>) {
    val input = ""

    val oneMore = fmap(int) { it + 1}
    val parsed = parse(many1(letter), input)

//    val parsed = parse(string("var y = 5;"), input)

    when (parsed) {
        is Error<*>   -> println(parsed.message)
        is Success<*> -> println(parsed)
    }
}
