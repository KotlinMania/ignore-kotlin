// port-lint: tests types.rs
package io.github.kotlinmania.ignore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TypesTest {
    private fun types(): List<String> =
        listOf(
            "html:*.html",
            "html:*.htm",
            "rust:*.rs",
            "js:*.js",
            "py:*.py",
            "python:*.py",
            "foo:*.{rs,foo}",
            "combo:include:html,rust",
        )

    private fun checkMatch(
        typesDefs: List<String>,
        sel: List<String>,
        selnot: List<String>,
        path: String,
        expectedMatched: Boolean,
    ) {
        val btypes = TypesBuilder()
        for (tydef in typesDefs) {
            btypes.addDef(tydef)
        }
        for (s in sel) {
            btypes.select(s)
        }
        for (sn in selnot) {
            btypes.negate(sn)
        }
        val types = btypes.build()
        val mat = types.matched(path, false)
        val isMatched = !mat.isIgnore()
        assertEquals(expectedMatched, isMatched)
    }

    @Test
    fun match1() = checkMatch(types(), listOf("rust"), emptyList(), "lib.rs", true)

    @Test
    fun match2() = checkMatch(types(), listOf("html"), emptyList(), "index.html", true)

    @Test
    fun match3() = checkMatch(types(), listOf("html"), emptyList(), "index.htm", true)

    @Test
    fun match4() = checkMatch(types(), listOf("html", "rust"), emptyList(), "main.rs", true)

    @Test
    fun match5() = checkMatch(types(), emptyList(), emptyList(), "index.html", true)

    @Test
    fun match6() = checkMatch(types(), emptyList(), listOf("rust"), "index.html", true)

    @Test
    fun match7() = checkMatch(types(), listOf("foo"), listOf("rust"), "main.foo", true)

    @Test
    fun match8() = checkMatch(types(), listOf("combo"), emptyList(), "index.html", true)

    @Test
    fun match9() = checkMatch(types(), listOf("combo"), emptyList(), "lib.rs", true)

    @Test
    fun match10() = checkMatch(types(), listOf("py"), emptyList(), "main.py", true)

    @Test
    fun match11() = checkMatch(types(), listOf("python"), emptyList(), "main.py", true)

    @Test
    fun matchnot1() = checkMatch(types(), listOf("rust"), emptyList(), "index.html", false)

    @Test
    fun matchnot2() = checkMatch(types(), emptyList(), listOf("rust"), "main.rs", false)

    @Test
    fun matchnot3() = checkMatch(types(), listOf("foo"), listOf("rust"), "main.rs", false)

    @Test
    fun matchnot4() = checkMatch(types(), listOf("rust"), listOf("foo"), "main.rs", false)

    @Test
    fun matchnot5() = checkMatch(types(), listOf("rust"), listOf("foo"), "main.foo", false)

    @Test
    fun matchnot6() = checkMatch(types(), listOf("combo"), emptyList(), "leftpad.js", false)

    @Test
    fun matchnot7() = checkMatch(types(), listOf("py"), emptyList(), "index.html", false)

    @Test
    fun matchnot8() = checkMatch(types(), listOf("python"), emptyList(), "doc.md", false)

    @Test
    fun testInvalidDefs() {
        val btypes = TypesBuilder()
        for (tydef in types()) {
            btypes.addDef(tydef)
        }
        val originalDefs = btypes.definitions()
        val badDefs =
            listOf(
                "combo:include:html,qwerty",
                "combo:foobar:html,rust",
                "",
            )
        for (def in badDefs) {
            assertFailsWith<Error.InvalidDefinition> {
                btypes.addDef(def)
            }
            assertEquals(originalDefs, btypes.definitions())
        }
    }
}
