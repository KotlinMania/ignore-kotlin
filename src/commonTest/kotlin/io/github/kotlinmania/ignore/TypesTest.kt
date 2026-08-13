// port-lint: source types.rs
package io.github.kotlinmania.ignore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TypesTest {
    @Test
    fun selectionsAndNegationsFollowPrecedence() {
        val types =
            configuredTypes {
                select("foo")
                negate("rust")
            }

        assertTrue(types.matched("main.foo").isWhitelist())
        assertTrue(types.matched("main.rs").isIgnore())
        assertTrue(types.matched("index.html").isIgnore())
    }

    @Test
    fun emptySelectionDoesNotFilterFiles() {
        val types = configuredTypes()

        assertTrue(types.matched("index.html").isNone())
        assertTrue(types.matched("main.rs").isNone())
    }

    @Test
    fun includeDefinitionsReuseExistingGlobs() {
        val types =
            configuredTypes {
                select("combo")
            }

        assertTrue(types.matched("index.html").isWhitelist())
        assertTrue(types.matched("lib.rs").isWhitelist())
        assertTrue(types.matched("leftpad.js").isIgnore())
    }

    @Test
    fun negatedSelectionIgnoresOnlyItsType() {
        val types =
            configuredTypes {
                negate("rust")
            }

        assertTrue(types.matched("main.rs").isIgnore())
        assertTrue(types.matched("index.html").isNone())
    }

    @Test
    fun allSelectionExpandsCurrentDefinitions() {
        val types =
            configuredTypes {
                select("all")
            }

        assertEquals(7, types.len())
        assertTrue(types.matched("index.htm").isWhitelist())
        assertTrue(types.matched("main.py").isWhitelist())
    }

    @Test
    fun invalidDefinitionsDoNotChangeExistingDefinitions() {
        val builder = typeBuilder()
        val original = builder.definitions()

        assertFailsWith<Error.InvalidDefinition> {
            builder.addDef("combo:include:html,qwerty")
        }
        assertFailsWith<Error.InvalidDefinition> {
            builder.addDef("combo:foobar:html,rust")
        }
        assertFailsWith<Error.InvalidDefinition> {
            builder.addDef("")
        }
        assertEquals(original, builder.definitions())
    }

    @Test
    fun directoryEntriesDoNotUseFileTypeFilters() {
        val types =
            configuredTypes {
                select("rust")
            }

        assertFalse(types.matched("lib.rs", isDir = true).isIgnore())
        assertTrue(types.matched("lib.rs", isDir = true).isNone())
    }

    private fun configuredTypes(configure: TypesBuilder.() -> Unit = {}): Types =
        typeBuilder().apply(configure).build()

    private fun typeBuilder(): TypesBuilder {
        val builder = TypesBuilder()
        for (definition in typeDefinitions()) {
            builder.addDef(definition)
        }
        return builder
    }

    private fun typeDefinitions(): List<String> =
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
}
