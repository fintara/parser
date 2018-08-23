package com.tsovedenski.parser

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

/**
 * Created by Tsvetan Ovedenski on 24/07/18.
 */
object UrlParserTest : Spek({
    describe("url") {
        val parser = createParser()

        it("parses with http") {
            positive(parser, "http", "github.com", 80)
        }

        it("parses with https") {
            positive(parser, "https", "www.youtube.com", 443, listOf("feed", "subscriptions"))
        }

        it("parses with path") {
            positive(parser, "http", "github.com", 80, listOf("explore"))
        }

        it("parses with query params") {
            positive(parser, "http", "forum.net", 80, listOf("forum", "viewtopic.php"), mapOf("t" to "129860"))
        }

        it("fails when unknown protocol") {
            negative(parser, "proto", "github.com", 80)
        }

        it("fails when negative port") {
            negative(parser, "http", "github.com", -80)
        }

        it("fails with negative port and path") {
            negative(parser, "https", "www.youtube.com", -1, listOf("feed", "subscriptions"))
        }
    }
})

private data class Url(
    val protocol: String,
    val domain: String,
    val port: Int,
    val path: List<String>,
    val params: Map<String, String>
) {
    override fun toString(): String {
        val builder = StringBuilder()
        builder.append(protocol)
        builder.append("://")
        builder.append(domain)
        if ((protocol == "http" && port != 80) || (protocol == "https" && port != 443)) {
            builder.append(':')
            builder.append(port)
        }
        builder.append('/')
        builder.append(path.joinToString("/"))
        if (params.isNotEmpty()) {
            builder.append('?')
            params.forEach { k, v ->
                builder.append(k)
                builder.append('=')
                builder.append(v)
            }
        }
        return builder.toString()
    }
}

private fun createParser(): Parser<Url> {
    val protocolP = string("https") or string("http")
    val portP = char(':') andR uint
    val domainP = many1String(noneOf("/\\ \t!@#$%^&*()[]{}<>;:\"'~`+=".toList()))
    val pathP = many1String(noneOf("/?".toList())) sepBy char('/')
    val keyValueP = chain(many1String(noneOf("=&#".toList())), string("="), many1String(noneOf("&#".toList())))
            .map { Pair(it[0], it[2]) }
    val paramsP = (keyValueP sepBy1 char('&')).map { xs -> xs.associateBy({ it.first }, { it.second }) }

    return buildParser {
        val protocol = protocolP.ev()

        string("://").ev()

        val domain = domainP.ev()

        val hasPort = lookahead(option('x', char(':'))).ev() == ':'
        val port = if (hasPort) {
            portP.ev()
        } else {
            if (protocol == "http") 80 else 443
        }

        optional(char('/')).ev()

        val path = pathP.ev()

        val hasParams = option('x', char('?')).ev() == '?'
        val params = if (hasParams) paramsP.ev() else mapOf()

        Url(protocol, domain, port, path, params)
    }
}

private fun positive(
        parser: Parser<Url>,
        protocol: String,
        domain: String,
        port: Int,
        path: List<String> = listOf(),
        params: Map<String, String> = mapOf()) {
    val expected = Url(protocol, domain, port, path, params)
    val input = expected.toString()
    assertSuccess(parser.map(Url::toString), input, input)
}

private fun negative(
        parser: Parser<Url>,
        protocol: String,
        domain: String,
        port: Int,
        path: List<String> = listOf(),
        params: Map<String, String> = mapOf()) {
    val expected = Url(protocol, domain, port, path, params)
    val input = expected.toString()
    assertError(parser.map(Url::toString), input)
}