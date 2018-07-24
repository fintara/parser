package com.tsovedenski.parser

import org.junit.Test

/**
 * Created by Tsvetan Ovedenski on 24/07/18.
 */
class UrlParserTest {

    @Test fun `url 1`() = positive("http", "github.com", 80)
    @Test fun `url 2`() = negative("proto", "github.com", 80)
    @Test fun `url 3`() = negative("http", "github.com", -80)
    @Test fun `url 4`() = positive("http", "github.com", 80, listOf("explore"))
    @Test fun `url 5`() = positive("http", "forum.net", 80, listOf("forum", "viewtopic.php"), mapOf("t" to "129860"))
    @Test fun `url 6`() = positive("https", "www.youtube.com", 443, listOf("feed", "subscriptions"))
    @Test fun `url 7`() = negative("https", "www.youtube.com", -1, listOf("feed", "subscriptions"))

    private val protocol = string("https") or string("http")
    private val port = char(':') andR uint
    private val domain = many1String(noneOf("/\\ \t!@#$%^&*()[]{}<>;:\"'~`+=".toList()))
    private val path = many1String(noneOf("/?".toList())) sepBy char('/')
    private val keyValue = listOf(many1String(noneOf("=&#".toList())), string("="), many1String(noneOf("&#".toList()))).chain()
            .map { Pair(it[0], it[2]) }
    private val params = (keyValue sepBy1 char('&')).map { it.associateBy({ it.first }, { it.second }) }

    private data class Url(val protocol: String, val domain: String, val port: Int, val path: List<String>, val params: Map<String, String>) {
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

    private val parser = buildParser {
        val protocol = protocol.ev()

        string("://").ev()

        val domain = domain.ev()

        val hasPort = lookahead(option('x', char(':'))).ev() == ':'
        val port = if (hasPort) {
            port.ev()
        } else {
            if (protocol == "http") 80 else 443
        }

        optional(char('/')).ev()

        val path = path.ev()

        val hasParams = option('x', char('?')).ev() == '?'
        val params = if (hasParams) params.ev() else mapOf()

        Url(protocol, domain, port, path, params)
    }

    private fun positive(protocol: String,
                            domain: String,
                            port: Int,
                            path: List<String> = listOf(),
                            params: Map<String, String> = mapOf()) {
        val expected = Url(protocol, domain, port, path, params)
        val input = expected.toString()
        assertSuccess(parser.map(Url::toString), input, input)
    }

    private fun negative(protocol: String,
                             domain: String,
                             port: Int,
                             path: List<String> = listOf(),
                             params: Map<String, String> = mapOf()) {
        val expected = Url(protocol, domain, port, path, params)
        val input = expected.toString()
        assertError(parser.map(Url::toString), input)
    }
}