// port-lint: tests lib.rs
package io.github.kotlinmania.ignore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MatchTest {
    @Test
    fun matchPredicatesAndTransforms() {
        val ignore = Match.Ignore("path")
        val whitelist = Match.Whitelist("path")
        val none: Match<String> = Match.None

        assertFalse(ignore.isNone())
        assertTrue(ignore.isIgnore())
        assertFalse(ignore.isWhitelist())
        assertEquals("path", ignore.inner())
        assertEquals(whitelist, ignore.invert())
        assertEquals(Match.Ignore(4), ignore.map { it.length })
        assertEquals(ignore, ignore.or(Match.Whitelist("fallback")))

        assertTrue(none.isNone())
        assertNull(none.inner())
        assertEquals(none, none.invert())
        assertEquals(Match.Ignore("fallback"), none.or(Match.Ignore("fallback")))
    }
}
