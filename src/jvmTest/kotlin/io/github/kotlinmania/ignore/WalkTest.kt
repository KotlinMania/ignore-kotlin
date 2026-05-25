// port-lint: source walk.rs
package io.github.kotlinmania.ignore

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import io.github.kotlinmania.io.buffered
import io.github.kotlinmania.io.files.Path
import io.github.kotlinmania.io.files.SystemFileSystem
import io.github.kotlinmania.io.files.SystemTemporaryDirectory
import io.github.kotlinmania.io.writeString

class WalkTest {
    @Test
    fun walkBuilderAppliesOverridesAndHiddenFilter() {
        val root = Path(SystemTemporaryDirectory, "ignore-kotlin-${Random.nextLong().toString(16)}")
        val src = Path(root, "src")
        val keep = Path(src, "keep.kt")
        val skip = Path(src, "skip.tmp")
        val hidden = Path(src, ".hidden.kt")
        SystemFileSystem.createDirectories(src, mustCreate = true)
        write(keep, "keep")
        write(skip, "skip")
        write(hidden, "hidden")

        try {
            val overrides = OverrideBuilder(root.toString()).add("!*.tmp").build()
            val paths = WalkBuilder(root.toString())
                .hidden(true)
                .overrides(overrides)
                .build()
                .mapNotNull { it.getOrNull()?.path() }
                .map { stripRoot(root.toString(), it) }
                .filter { it.isNotEmpty() }
                .sorted()

            assertEquals(listOf("src", "src/keep.kt"), paths)

            var sawKeep = false
            WalkBuilder(root.toString())
                .hidden(true)
                .overrides(overrides)
                .buildParallel()
                .run {
                    { entry ->
                        if (entry.getOrNull()?.path() == keep.toString()) {
                            sawKeep = true
                            WalkState.Quit
                        } else {
                            WalkState.Continue
                        }
                    }
                }
            assertTrue(sawKeep)
        } finally {
            SystemFileSystem.delete(keep, mustExist = false)
            SystemFileSystem.delete(skip, mustExist = false)
            SystemFileSystem.delete(hidden, mustExist = false)
            SystemFileSystem.delete(src, mustExist = false)
            SystemFileSystem.delete(root, mustExist = false)
        }
    }

    @Test
    fun walkBuilderLoadsIgnoreFilesFromDirectories() {
        val root = Path(SystemTemporaryDirectory, "ignore-kotlin-${Random.nextLong().toString(16)}")
        val git = Path(root, ".git")
        val keep = Path(root, "keep.kt")
        val tmp = Path(root, "skip.tmp")
        val log = Path(root, "skip.log")
        val ignore = Path(root, ".ignore")
        val gitignore = Path(root, ".gitignore")
        SystemFileSystem.createDirectories(git, mustCreate = true)
        write(keep, "keep")
        write(tmp, "tmp")
        write(log, "log")
        write(ignore, "*.tmp")
        write(gitignore, "*.log")

        try {
            val paths = WalkBuilder(root.toString())
                .hidden(false)
                .requireGit(true)
                .build()
                .mapNotNull { it.getOrNull()?.path() }
                .map { stripRoot(root.toString(), it) }
                .filter { it.isNotEmpty() }
                .sorted()

            assertEquals(listOf(".git", ".gitignore", ".ignore", "keep.kt"), paths)
        } finally {
            SystemFileSystem.delete(keep, mustExist = false)
            SystemFileSystem.delete(tmp, mustExist = false)
            SystemFileSystem.delete(log, mustExist = false)
            SystemFileSystem.delete(ignore, mustExist = false)
            SystemFileSystem.delete(gitignore, mustExist = false)
            SystemFileSystem.delete(git, mustExist = false)
            SystemFileSystem.delete(root, mustExist = false)
        }
    }

    private fun write(path: Path, text: String) {
        SystemFileSystem.sink(path).buffered().use { sink ->
            sink.writeString(text)
        }
    }
}
