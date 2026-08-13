// port-lint: source overrides.rs
package io.github.kotlinmania.ignore

import kotlin.test.Test
import kotlin.test.assertTrue

class OverridesTest {
    private fun ov(globs: List<String>): Override {
        val builder = OverrideBuilder("/home/andrew/foo")
        for (glob in globs) {
            builder.add(glob)
        }
        return builder.build()
    }

    @Test
    fun empty() {
        val ov = ov(emptyList())
        assertTrue(ov.matched("a.foo", false).isNone())
        assertTrue(ov.matched("a", false).isNone())
        assertTrue(ov.matched("", false).isNone())
    }

    @Test
    fun simple() {
        val ov = ov(listOf("*.foo", "!*.bar"))
        assertTrue(ov.matched("a.foo", false).isWhitelist())
        assertTrue(ov.matched("a.foo", true).isWhitelist())
        assertTrue(ov.matched("a.rs", false).isIgnore())
        assertTrue(ov.matched("a.rs", true).isNone())
        assertTrue(ov.matched("a.bar", false).isIgnore())
        assertTrue(ov.matched("a.bar", true).isIgnore())
    }

    @Test
    fun onlyIgnores() {
        val ov = ov(listOf("!*.bar"))
        assertTrue(ov.matched("a.rs", false).isNone())
        assertTrue(ov.matched("a.rs", true).isNone())
        assertTrue(ov.matched("a.bar", false).isIgnore())
        assertTrue(ov.matched("a.bar", true).isIgnore())
    }

    @Test
    fun precedence() {
        val ov = ov(listOf("*.foo", "!*.bar.foo"))
        assertTrue(ov.matched("a.foo", false).isWhitelist())
        assertTrue(ov.matched("a.baz", false).isIgnore())
        assertTrue(ov.matched("a.bar.foo", false).isIgnore())
    }

    @Test
    fun caseInsensitive() {
        val ov =
            OverrideBuilder("/home/andrew/foo")
                .caseInsensitive(true)
                .add("*.html")
                .build()
        assertTrue(ov.matched("foo.html", false).isWhitelist())
        assertTrue(ov.matched("foo.HTML", false).isWhitelist())
        assertTrue(ov.matched("foo.htm", false).isIgnore())
        assertTrue(ov.matched("foo.HTM", false).isIgnore())
    }
}
