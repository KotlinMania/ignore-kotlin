// port-lint: tests lib.rs
package io.github.kotlinmania.ignore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class LibTest {
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

    @Test
    fun errorInspectionFollowsWrappers() {
        val cause = Exception("disk")
        val io = Error.Io(cause)
        val tagged = io.tagged("ignore", 7)
        val deep = Error.Glob("*.rs", "bad glob").withDepth(3).withPath("types")

        assertTrue(tagged.isIo())
        assertFalse(deep.isIo())
        assertSame(cause, tagged.ioError())
        assertSame(cause, tagged.intoIoError())
        assertEquals(3, deep.depth())
        assertTrue(Error.Partial(listOf(tagged, deep)).isPartial())
        assertFalse(Error.Loop("ancestor", "child").isIo())
    }

    @Test
    fun partialErrorBuilderCollapsesLikeUpstream() {
        val empty = PartialErrorBuilder()
        assertNull(empty.intoErrorOption())

        val ioOnly = PartialErrorBuilder()
        ioOnly.pushIgnoreIo(Error.Io(Exception("ignore me")))
        assertNull(ioOnly.intoErrorOption())

        val single = PartialErrorBuilder()
        val glob = Error.Glob("*.kt", "bad glob")
        single.maybePush(glob)
        single.maybePushIgnoreIo(Error.Io(Exception("skip")))
        assertEquals(glob, single.intoErrorOption())

        val multiple = PartialErrorBuilder()
        multiple.push(glob)
        multiple.push(Error.UnrecognizedFileType("zig"))
        val error = multiple.intoErrorOption()
        val partial = assertIs<Error.Partial>(error)
        assertEquals(listOf(glob, Error.UnrecognizedFileType("zig")), partial.errors)
    }
}
