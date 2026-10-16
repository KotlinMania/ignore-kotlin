// port-lint: tests lib.rs
package io.github.kotlinmania.ignore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ErrorTest {
    @Test
    fun errorInspectionFollowsWrappers() {
        val cause = Exception("disk")
        val io = IgnoreError.Io(cause)
        val tagged = io.tagged("ignore", 7)
        val deep = IgnoreError.Glob("*.rs", "bad glob").withDepth(3).withPath("types")

        assertTrue(tagged.isIo())
        assertFalse(deep.isIo())
        assertEquals(cause, tagged.ioError())
        assertEquals(cause, tagged.intoIoError())
        assertEquals(3, deep.depth())
        assertTrue(IgnoreError.Partial(listOf(tagged, deep)).isPartial())
        assertFalse(IgnoreError.Loop("ancestor", "child").isIo())
    }

    @Test
    fun partialErrorBuilderCollapsesLikeUpstream() {
        val empty = PartialErrorBuilder()
        assertNull(empty.intoErrorOption())

        val ioOnly = PartialErrorBuilder()
        ioOnly.pushIgnoreIo(IgnoreError.Io(Exception("ignore me")))
        assertNull(ioOnly.intoErrorOption())

        val single = PartialErrorBuilder()
        val glob = IgnoreError.Glob("*.kt", "bad glob")
        single.maybePush(glob)
        single.maybePushIgnoreIo(IgnoreError.Io(Exception("skip")))
        assertEquals(glob, single.intoErrorOption())

        val multiple = PartialErrorBuilder()
        multiple.push(glob)
        multiple.push(IgnoreError.UnrecognizedFileType("zig"))
        val error = multiple.intoErrorOption()
        val partial = assertIs<IgnoreError.Partial>(error)
        assertEquals(listOf(glob, IgnoreError.UnrecognizedFileType("zig")), partial.errors)
    }
}
