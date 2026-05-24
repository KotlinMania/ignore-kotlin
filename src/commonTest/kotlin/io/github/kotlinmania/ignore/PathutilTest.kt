// port-lint: source pathutil.rs
package io.github.kotlinmania.ignore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PathutilTest {
    @Test
    fun hiddenPathsUseTheFinalComponent() {
        assertTrue(isHidden(".git"))
        assertTrue(isHidden("src/.ignore"))
        assertFalse(isHidden("src/git"))
        assertFalse(isHidden("."))
        assertFalse(isHidden("src/."))
    }

    @Test
    fun fileNameRejectsDotTerminators() {
        assertNull(fileName(""))
        assertNull(fileName("."))
        assertNull(fileName("src/."))
        assertNull(fileName("src/.."))
        assertNull(fileName("name."))
        assertEquals("file", fileName("src/file"))
    }
}
