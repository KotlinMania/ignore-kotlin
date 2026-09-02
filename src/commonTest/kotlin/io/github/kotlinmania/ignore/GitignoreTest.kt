// port-lint: tests gitignore.rs
package io.github.kotlinmania.ignore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GitignoreTest {
    private fun giFromStr(root: String, s: String): Gitignore {
        val builder = GitignoreBuilder(root)
        builder.addStr(null, s)
        return builder.build()
    }

    private fun assertIgnored(root: String, gi: String, path: String, isDir: Boolean = false) {
        val g = giFromStr(root, gi)
        assertTrue(g.matched(path, isDir).isIgnore())
    }

    private fun assertNotIgnored(root: String, gi: String, path: String, isDir: Boolean = false) {
        val g = giFromStr(root, gi)
        assertFalse(g.matched(path, isDir).isIgnore())
    }

    @Test fun ig1() = assertIgnored(ROOT, "months", "months")
    @Test fun ig2() = assertIgnored(ROOT, "*.lock", "Cargo.lock")
    @Test fun ig3() = assertIgnored(ROOT, "*.rs", "src/main.rs")
    @Test fun ig4() = assertIgnored(ROOT, "src/*.rs", "src/main.rs")
    @Test fun ig5() = assertIgnored(ROOT, "/*.c", "cat-file.c")
    @Test fun ig6() = assertIgnored(ROOT, "/src/*.rs", "src/main.rs")
    @Test fun ig7() = assertIgnored(ROOT, "!src/main.rs\n*.rs", "src/main.rs")
    @Test fun ig8() = assertIgnored(ROOT, "foo/", "foo", true)
    @Test fun ig9() = assertIgnored(ROOT, "**/foo", "foo")
    @Test fun ig10() = assertIgnored(ROOT, "**/foo", "src/foo")
    @Test fun ig11() = assertIgnored(ROOT, "**/foo/**", "src/foo/bar")
    @Test fun ig12() = assertIgnored(ROOT, "**/foo/**", "wat/src/foo/bar/baz")
    @Test fun ig13() = assertIgnored(ROOT, "**/foo/bar", "foo/bar")
    @Test fun ig14() = assertIgnored(ROOT, "**/foo/bar", "src/foo/bar")
    @Test fun ig15() = assertIgnored(ROOT, "abc/**", "abc/x")
    @Test fun ig16() = assertIgnored(ROOT, "abc/**", "abc/x/y")
    @Test fun ig17() = assertIgnored(ROOT, "abc/**", "abc/x/y/z")
    @Test fun ig18() = assertIgnored(ROOT, "a/**/b", "a/b")
    @Test fun ig19() = assertIgnored(ROOT, "a/**/b", "a/x/b")
    @Test fun ig20() = assertIgnored(ROOT, "a/**/b", "a/x/y/b")
    @Test fun ig21() = assertIgnored(ROOT, "\\!xy", "!xy")
    @Test fun ig22() = assertIgnored(ROOT, "\\#foo", "#foo")
    @Test fun ig23() = assertIgnored(ROOT, "foo", "./foo")
    @Test fun ig24() = assertIgnored(ROOT, "target", "grep/target")
    @Test fun ig25() = assertIgnored(ROOT, "Cargo.lock", "./tabwriter-bin/Cargo.lock")
    @Test fun ig26() = assertIgnored(ROOT, "/foo/bar/baz", "./foo/bar/baz")
    @Test fun ig27() = assertIgnored(ROOT, "foo/", "xyz/foo", true)
    @Test fun ig28() = assertIgnored("./src", "/llvm/", "./src/llvm", true)
    @Test fun ig29() = assertIgnored(ROOT, "node_modules/ ", "node_modules", true)
    @Test fun ig30() = assertIgnored(ROOT, "**/", "foo/bar", true)
    @Test fun ig31() = assertIgnored(ROOT, "path1/*", "path1/foo")
    @Test fun ig32() = assertIgnored(ROOT, ".a/b", ".a/b")
    @Test fun ig33() = assertIgnored("./", ".a/b", ".a/b")
    @Test fun ig34() = assertIgnored(".", ".a/b", ".a/b")
    @Test fun ig35() = assertIgnored("./.", ".a/b", ".a/b")
    @Test fun ig36() = assertIgnored("././", ".a/b", ".a/b")
    @Test fun ig37() = assertIgnored("././.", ".a/b", ".a/b")
    @Test fun ig38() = assertIgnored(ROOT, "\\[", "[")
    @Test fun ig39() = assertIgnored(ROOT, "\\?", "?")
    @Test fun ig40() = assertIgnored(ROOT, "\\*", "*")
    @Test fun ig41() = assertIgnored(ROOT, "\\a", "a")
    @Test fun ig42() = assertIgnored(ROOT, "s*.rs", "sfoo.rs")
    @Test fun ig43() = assertIgnored(ROOT, "**", "foo.rs")
    @Test fun ig44() = assertIgnored(ROOT, "**/**/*", "a/foo.rs")

    @Test fun ignot1() = assertNotIgnored(ROOT, "amonths", "months")
    @Test fun ignot2() = assertNotIgnored(ROOT, "monthsa", "months")
    @Test fun ignot3() = assertNotIgnored(ROOT, "/src/*.rs", "src/grep/src/main.rs")
    @Test fun ignot4() = assertNotIgnored(ROOT, "/*.c", "mozilla-sha1/sha1.c")
    @Test fun ignot5() = assertNotIgnored(ROOT, "/src/*.rs", "src/grep/src/main.rs")
    @Test fun ignot6() = assertNotIgnored(ROOT, "*.rs\n!src/main.rs", "src/main.rs")
    @Test fun ignot7() = assertNotIgnored(ROOT, "foo/", "foo", false)
    @Test fun ignot8() = assertNotIgnored(ROOT, "**/foo/**", "wat/src/afoo/bar/baz")
    @Test fun ignot9() = assertNotIgnored(ROOT, "**/foo/**", "wat/src/fooa/bar/baz")
    @Test fun ignot10() = assertNotIgnored(ROOT, "**/foo/bar", "foo/src/bar")
    @Test fun ignot11() = assertNotIgnored(ROOT, "#foo", "#foo")
    @Test fun ignot12() = assertNotIgnored(ROOT, "\n\n\n", "foo")
    @Test fun ignot13() = assertNotIgnored(ROOT, "foo/**", "foo", true)
    @Test fun ignot14() = assertNotIgnored("./third_party/protobuf", "m4/ltoptions.m4", "./third_party/protobuf/csharp/src/packages/repositories.config")
    @Test fun ignot15() = assertNotIgnored(ROOT, "!/bar", "foo/bar")
    @Test fun ignot16() = assertNotIgnored(ROOT, "*\n!**/", "foo", true)
    @Test fun ignot17() = assertNotIgnored(ROOT, "src/*.rs", "src/grep/src/main.rs")
    @Test fun ignot18() = assertNotIgnored(ROOT, "path1/*", "path2/path1/foo")
    @Test fun ignot19() = assertNotIgnored(ROOT, "s*.rs", "src/foo.rs")

    @Test
    fun parseExcludesFile1() {
        val data = "[core]\nexcludesFile = /foo/bar".encodeToByteArray()
        val got = parseExcludesFile(data)
        assertEquals("/foo/bar", got)
    }

    @Test
    fun parseExcludesFile2() {
        val data = "[core]\nexcludesFile = ~/foo/bar".encodeToByteArray()
        val got = parseExcludesFile(data)
        assertEquals(expandTilde("~/foo/bar"), got)
    }

    @Test
    fun parseExcludesFile3() {
        val data = "[core]\nexcludeFile = /foo/bar".encodeToByteArray()
        val got = parseExcludesFile(data)
        assertNull(got)
    }

    @Test
    fun parseExcludesFile4() {
        val data = "[core]\nexcludesFile = \"~/foo/bar\"".encodeToByteArray()
        val got = parseExcludesFile(data)
        assertEquals(expandTilde("~/foo/bar"), got)
    }

    @Test
    fun parseExcludesFile5() {
        val data = "[core]\nexcludesFile = \" \"~/foo/bar \" \"".encodeToByteArray()
        val got = parseExcludesFile(data)
        assertNull(got)
    }

    @Test
    fun regression106() {
        giFromStr("/", " ")
    }

    @Test
    fun caseInsensitive() {
        val gi =
            GitignoreBuilder(ROOT)
                .caseInsensitive(true)
                .addStr(null, "*.html")
                .build()
        assertTrue(gi.matched("foo.html", false).isIgnore())
        assertTrue(gi.matched("foo.HTML", false).isIgnore())
        assertFalse(gi.matched("foo.htm", false).isIgnore())
        assertFalse(gi.matched("foo.HTM", false).isIgnore())
    }

    @Test fun cs1() = assertIgnored(ROOT, "*.html", "foo.html")
    @Test fun cs2() = assertNotIgnored(ROOT, "*.html", "foo.HTML")
    @Test fun cs3() = assertNotIgnored(ROOT, "*.html", "foo.htm")
    @Test fun cs4() = assertNotIgnored(ROOT, "*.html", "foo.HTM")

    private companion object {
        const val ROOT: String = "/home/foobar/rust/rg"
    }
}
