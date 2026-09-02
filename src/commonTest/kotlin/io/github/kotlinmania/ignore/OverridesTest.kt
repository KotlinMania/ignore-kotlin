// port-lint: tests overrides.rs
package io.github.kotlinmania.ignore

import kotlin.test.Test
import kotlin.test.assertTrue

class OverridesTest {
    private val root = "/home/andrew/foo"

    private fun ov(vararg globs: String): Override {
        val builder = OverrideBuilder(root)
        for (glob in globs) {
            builder.add(glob)
        }
        return builder.build()
    }

    @Test
    fun empty() {
        val ov = ov()
        assertTrue(ov.matched("a.foo", false).isNone())
        assertTrue(ov.matched("a", false).isNone())
        assertTrue(ov.matched("", false).isNone())
    }

    @Test
    fun simple() {
        val ov = ov("*.foo", "!*.bar")
        assertTrue(ov.matched("a.foo", false).isWhitelist())
        assertTrue(ov.matched("a.foo", true).isWhitelist())
        assertTrue(ov.matched("a.rs", false).isIgnore())
        assertTrue(ov.matched("a.rs", true).isNone())
        assertTrue(ov.matched("a.bar", false).isIgnore())
        assertTrue(ov.matched("a.bar", true).isIgnore())
    }

    @Test
    fun onlyIgnores() {
        val ov = ov("!*.bar")
        assertTrue(ov.matched("a.rs", false).isNone())
        assertTrue(ov.matched("a.rs", true).isNone())
        assertTrue(ov.matched("a.bar", false).isIgnore())
        assertTrue(ov.matched("a.bar", true).isIgnore())
    }

    @Test
    fun precedence() {
        val ov = ov("*.foo", "!*.bar.foo")
        assertTrue(ov.matched("a.foo", false).isWhitelist())
        assertTrue(ov.matched("a.baz", false).isIgnore())
        assertTrue(ov.matched("a.bar.foo", false).isIgnore())
    }

    @Test
    fun gitignore() {
        val ov = ov("/foo", "bar/*.rs", "baz/**")
        assertTrue(ov.matched("bar/lib.rs", false).isWhitelist())
        assertTrue(ov.matched("bar/wat/lib.rs", false).isIgnore())
        assertTrue(ov.matched("wat/bar/lib.rs", false).isIgnore())
        assertTrue(ov.matched("foo", false).isWhitelist())
        assertTrue(ov.matched("wat/foo", false).isIgnore())
        assertTrue(ov.matched("baz", false).isIgnore())
        assertTrue(ov.matched("baz/a", false).isWhitelist())
        assertTrue(ov.matched("baz/a/b", false).isWhitelist())
    }

    @Test
    fun allowDirectories() {
        val ov = ov("*.rs")
        assertTrue(ov.matched("foo.rs", false).isWhitelist())
        assertTrue(ov.matched("foo.c", false).isIgnore())
        assertTrue(ov.matched("foo", false).isIgnore())
        assertTrue(ov.matched("foo", true).isNone())
        assertTrue(ov.matched("src/foo.rs", false).isWhitelist())
        assertTrue(ov.matched("src/foo.c", false).isIgnore())
        assertTrue(ov.matched("src/foo", false).isIgnore())
        assertTrue(ov.matched("src/foo", true).isNone())
    }

    @Test
    fun absolutePath() {
        val ov = ov("!/bar")
        assertTrue(ov.matched("./foo/bar", false).isNone())
    }

    @Test
    fun caseInsensitive() {
        val ov =
            OverrideBuilder(root)
                .caseInsensitive(true)
                .add("*.html")
                .build()
        assertTrue(ov.matched("foo.html", false).isWhitelist())
        assertTrue(ov.matched("foo.HTML", false).isWhitelist())
        assertTrue(ov.matched("foo.htm", false).isIgnore())
        assertTrue(ov.matched("foo.HTM", false).isIgnore())
    }

    @Test
    fun defaultCaseSensitive() {
        val ov =
            OverrideBuilder(root)
                .add("*.html")
                .build()
        assertTrue(ov.matched("foo.html", false).isWhitelist())
        assertTrue(ov.matched("foo.HTML", false).isIgnore())
        assertTrue(ov.matched("foo.htm", false).isIgnore())
        assertTrue(ov.matched("foo.HTM", false).isIgnore())
    }
}
