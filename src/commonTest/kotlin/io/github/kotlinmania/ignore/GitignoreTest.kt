// port-lint: source gitignore.rs
package io.github.kotlinmania.ignore

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GitignoreTest {
    @Test
    fun basenameAndNestedPatternsUseGitignoreRules() {
        val gi = giFromString(ROOT, "*.rs")

        assertTrue(gi.matched("src/main.rs", isDir = false).isIgnore())
        assertTrue(gi.matched("main.rs", isDir = false).isIgnore())
        assertFalse(gi.matched("src/main.kt", isDir = false).isIgnore())
    }

    @Test
    fun absolutePatternsStayRooted() {
        val gi = giFromString(ROOT, "/src/*.rs")

        assertTrue(gi.matched("src/main.rs", isDir = false).isIgnore())
        assertFalse(gi.matched("src/grep/src/main.rs", isDir = false).isIgnore())
    }

    @Test
    fun laterWhitelistOverridesEarlierIgnore() {
        val gi = giFromString(ROOT, "*.rs\n!src/main.rs")

        assertFalse(gi.matched("src/main.rs", isDir = false).isIgnore())
        assertTrue(gi.matched("src/lib.rs", isDir = false).isIgnore())
    }

    @Test
    fun directoryOnlyPatternsRequireDirectoryEntries() {
        val gi = giFromString(ROOT, "foo/")

        assertTrue(gi.matched("xyz/foo", isDir = true).isIgnore())
        assertFalse(gi.matched("foo", isDir = false).isIgnore())
    }

    @Test
    fun escapedLeadingCharactersAreLiteral() {
        val gi = giFromString(ROOT, "\\!xy\n\\#foo")

        assertTrue(gi.matched("!xy", isDir = false).isIgnore())
        assertTrue(gi.matched("#foo", isDir = false).isIgnore())
    }

    @Test
    fun trailingSpacesAreTrimmedUnlessEscaped() {
        val gi = giFromString(ROOT, "node_modules/ ")

        assertTrue(gi.matched("node_modules", isDir = true).isIgnore())
    }

    @Test
    fun parentsCanBeMatchedForPathLists() {
        val gi = giFromString(ROOT, "target")

        assertTrue(gi.matchedPathOrAnyParents("grep/target/file.txt", isDir = false).isIgnore())
        assertFalse(gi.matchedPathOrAnyParents("grep/build/file.txt", isDir = false).isIgnore())
    }

    @Test
    fun caseSensitivityCanBeDisabled() {
        val gi = GitignoreBuilder(ROOT)
            .caseInsensitive(true)
            .addStr(null, "*.html")
            .build()

        assertTrue(gi.matched("foo.html", isDir = false).isIgnore())
        assertTrue(gi.matched("foo.HTML", isDir = false).isIgnore())
        assertFalse(gi.matched("foo.htm", isDir = false).isIgnore())
    }

    private fun giFromString(root: String, text: String): Gitignore =
        GitignoreBuilder(root).addStr(null, text).build()

    private companion object {
        const val ROOT: String = "/home/foobar/rust/rg"
    }
}
