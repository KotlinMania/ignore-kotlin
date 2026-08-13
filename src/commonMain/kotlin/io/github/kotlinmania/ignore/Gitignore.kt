// port-lint: source gitignore.rs
@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)

package io.github.kotlinmania.ignore

import io.github.kotlinmania.io.buffered
import io.github.kotlinmania.io.files.Path
import io.github.kotlinmania.io.files.SystemFileSystem
import io.github.kotlinmania.io.readString
import kotlin.native.HiddenFromObjC

/**
 * The gitignore module provides a way to match globs from a gitignore file
 * against file paths.
 *
 * This implements the gitignore-style path matching needed by the port without
 * shelling out to the `git` command line tool.
 */

/** Glob represents a single glob in a gitignore file. */
@HiddenFromObjC
class Glob internal constructor(
    private val fromValue: String?,
    private val originalValue: String,
    private val actualValue: String,
    private val whitelist: Boolean,
    private val onlyDir: Boolean,
) {
    /** Returns the file path that defined this glob. */
    fun from(): String? = fromValue

    /** The original glob as it was defined in a gitignore file. */
    fun original(): String = originalValue

    /** The actual glob that was compiled to respect gitignore semantics. */
    fun actual(): String = actualValue

    /** Whether this was a whitelisted glob or not. */
    fun isWhitelist(): Boolean = whitelist

    /** Whether this glob must match a directory or not. */
    fun isOnlyDir(): Boolean = onlyDir

    internal fun hasDoublestarPrefix(): Boolean =
        actualValue.startsWith("**/") || actualValue == "**"

    override fun equals(other: Any?): Boolean =
        other is Glob &&
            fromValue == other.fromValue &&
            originalValue == other.originalValue &&
            actualValue == other.actualValue &&
            whitelist == other.whitelist &&
            onlyDir == other.onlyDir

    override fun hashCode(): Int {
        var result = fromValue?.hashCode() ?: 0
        result = 31 * result + originalValue.hashCode()
        result = 31 * result + actualValue.hashCode()
        result = 31 * result + whitelist.hashCode()
        result = 31 * result + onlyDir.hashCode()
        return result
    }

    override fun toString(): String = "Glob(original=$originalValue, actual=$actualValue)"
}

/** Gitignore is a matcher for the globs in one or more gitignore files in the same directory. */
@HiddenFromObjC
class Gitignore internal constructor(
    private val root: String,
    private val globs: List<CompiledIgnoreGlob>,
) {
    private val ignores = globs.count { !it.glob.isWhitelist() }.toLong()
    private val whitelists = globs.count { it.glob.isWhitelist() }.toLong()

    /** Returns the directory containing this gitignore matcher. */
    fun path(): String = root

    /** Returns true if and only if this gitignore has zero globs. */
    fun isEmpty(): Boolean = globs.isEmpty()

    /** Returns the total number of globs. */
    fun len(): Int = globs.size

    /** Returns the total number of ignore globs. */
    fun numIgnores(): Long = ignores

    /** Returns the total number of whitelisted globs. */
    fun numWhitelists(): Long = whitelists

    /** Returns whether [path] matched a pattern in this matcher. */
    fun matched(path: String, isDir: Boolean): Match<Glob> {
        if (isEmpty()) return Match.None
        return matchedStripped(strip(path), isDir)
    }

    /** Returns whether [path] or one of its parents matched this matcher. */
    fun matchedPathOrAnyParents(path: String, isDir: Boolean): Match<Glob> {
        if (isEmpty()) return Match.None
        var current = strip(path)
        matchedStripped(current, isDir).let { if (!it.isNone()) return it }
        while (true) {
            val parent = parentPath(current) ?: return Match.None
            matchedStripped(parent, isDir = true).let { if (!it.isNone()) return it }
            current = parent
        }
    }

    internal fun strip(path: String): String {
        var normalizedPath = normalizePath(path).removePrefix("./")
        val normalizedRoot = root.removePrefix("./").trimEnd('/')
        if (normalizedRoot.isNotEmpty() && normalizedRoot != "." && !isFileName(normalizedPath)) {
            normalizedPath = stripRoot(normalizedRoot, normalizedPath)
        }
        return normalizedPath.trimStart('/')
    }

    internal fun matchedStripped(path: String, isDir: Boolean): Match<Glob> {
        var result: Match<Glob> = Match.None
        for (compiled in globs) {
            if (compiled.matches(path, isDir)) {
                result =
                    if (compiled.glob.isWhitelist()) {
                        Match.Whitelist(compiled.glob)
                    } else {
                        Match.Ignore(compiled.glob)
                    }
            }
        }
        return result
    }

    companion object {
        /** Creates a new gitignore matcher from the gitignore file path given. */
        fun new(gitignorePath: String): Pair<Gitignore, Error?> {
            val parent = parentPath(gitignorePath) ?: "/"
            val builder = GitignoreBuilder(parent)
            val error = builder.add(gitignorePath)
            return try {
                Pair(builder.build(), error)
            } catch (err: Error) {
                Pair(empty(), error ?: err)
            }
        }

        /** Creates a new global gitignore matcher when platform data is available. */
        fun global(): Pair<Gitignore, Error?> = Pair(empty(), null)

        /** Creates a new empty gitignore matcher that never matches anything. */
        fun empty(): Gitignore = Gitignore("", emptyList())
    }
}

/** Builds a gitignore matcher from lines or explicit glob patterns. */
@HiddenFromObjC
class GitignoreBuilder(
    path: String,
) {
    private val root = normalizePath(path)
    private val globs = mutableListOf<CompiledIgnoreGlob>()
    private var caseInsensitive = false
    private var allowUnclosedClass = true

    /** Toggle whether globs should be matched case insensitively. */
    fun caseInsensitive(yes: Boolean): GitignoreBuilder {
        caseInsensitive = yes
        return this
    }

    /** Toggle whether unclosed character classes are allowed. */
    fun allowUnclosedClass(yes: Boolean): GitignoreBuilder {
        allowUnclosedClass = yes
        return this
    }

    /** Add a single gitignore-style line. */
    fun addLine(from: String?, line: String): GitignoreBuilder {
        val parsed = parseIgnoreLine(line) ?: return this
        val glob =
            Glob(
                fromValue = from,
                originalValue = parsed.original,
                actualValue = parsed.actual,
                whitelist = parsed.whitelist,
                onlyDir = parsed.onlyDir,
            )
        globs += CompiledIgnoreGlob(glob, parsed, caseInsensitive, allowUnclosedClass)
        return this
    }

    /** Add each glob from the file path given. */
    fun add(path: String): Error? {
        val contents =
            try {
                SystemFileSystem.source(Path(path)).buffered().use { source -> source.readString() }
            } catch (err: Exception) {
                return Error.Io(err).withPath(path)
            }
        var error: Error? = null
        for ((index, rawLine) in contents.lines().withIndex()) {
            val line = if (index == 0) rawLine.removePrefix("\uFEFF") else rawLine
            try {
                addLine(path, line)
            } catch (err: Error) {
                error = err.tagged(path, (index + 1).toLong())
            }
        }
        return error
    }

    /** Add each glob line from the string given. */
    internal fun addStr(from: String?, gitignore: String): GitignoreBuilder {
        for (line in gitignore.lines()) {
            addLine(from, line)
        }
        return this
    }

    /** Add a single override-style line. */
    internal fun addOverrideLine(line: String): GitignoreBuilder {
        val parsed = parseIgnoreLine(line) ?: return this
        val glob =
            Glob(
                fromValue = null,
                originalValue = parsed.original,
                actualValue = parsed.actual,
                whitelist = parsed.whitelist,
                onlyDir = parsed.onlyDir,
            )
        globs += CompiledIgnoreGlob(glob, parsed, caseInsensitive, allowUnclosedClass)
        return this
    }

    /** Build the matcher from all globs added so far. */
    fun build(): Gitignore = Gitignore(root, globs.toList())
}

internal data class ParsedIgnoreGlob(
    val original: String,
    val actual: String,
    val whitelist: Boolean,
    val onlyDir: Boolean,
    val anchored: Boolean,
    val basenameOnly: Boolean,
)

internal class CompiledIgnoreGlob(
    val glob: Glob,
    private val parsed: ParsedIgnoreGlob,
    private val caseInsensitive: Boolean,
    private val allowUnclosedClass: Boolean,
) {
    fun matches(path: String, isDir: Boolean): Boolean {
        if (parsed.onlyDir && !isDir) return false
        val normalizedPath = normalizePath(path).trimStart('/')
        val pattern = parsed.actual.trimStart('/')
        val candidates =
            buildList {
                add(normalizedPath)
                fileName(normalizedPath)?.let { add(it) }
            }
        val selected =
            when {
                parsed.anchored -> listOf(normalizedPath)
                parsed.basenameOnly -> candidates
                else -> listOf(normalizedPath)
            }
        return selected.any { globMatches(pattern, it, caseInsensitive, allowUnclosedClass) }
    }
}

private fun parseIgnoreLine(line: String, invertBang: Boolean = false): ParsedIgnoreGlob? {
    if (line.startsWith("#")) return null
    var text = if (line.endsWith("\\ ")) line else line.trimEnd()
    if (text.isEmpty()) return null
    var whitelist = false
    var anchored = false
    if (text.startsWith("\\!") || text.startsWith("\\#")) {
        text = text.drop(1)
        anchored = text.startsWith("/")
    } else {
        if (text.startsWith("!")) {
            whitelist = true
            text = text.drop(1)
        }
        if (text.startsWith("/")) {
            text = text.drop(1)
            anchored = true
        }
    }
    if (invertBang) {
        whitelist = !whitelist
    }
    if (text.isEmpty()) return null
    var onlyDir = false
    if (text.endsWith("/")) {
        onlyDir = true
        text = text.dropLast(1)
        if (text.endsWith("\\")) {
            text = text.dropLast(1)
        }
    }
    var actual = normalizePath(text)
    if (!anchored && !actual.contains("/") && !hasDoublestarPrefix(actual)) {
        actual = "**/$actual"
    }
    if (actual.endsWith("/**")) {
        actual = "$actual/*"
    }
    val basenameOnly = !actual.contains("/")
    return ParsedIgnoreGlob(
        original = line,
        actual = actual,
        whitelist = whitelist,
        onlyDir = onlyDir,
        anchored = anchored,
        basenameOnly = basenameOnly,
    )
}

internal fun stripRoot(root: String, path: String): String {
    val normalizedRoot = normalizePath(root).trimEnd('/')
    val normalizedPath = normalizePath(path)
    if (normalizedRoot.isEmpty()) return normalizedPath.trimStart('/')
    if (normalizedPath == normalizedRoot) return ""
    val prefix = "$normalizedRoot/"
    return if (normalizedPath.startsWith(prefix)) {
        normalizedPath.substring(prefix.length)
    } else {
        normalizedPath.trimStart('/')
    }
}

internal fun normalizePath(path: String): String =
    path.replace('\\', '/').replace(Regex("/{2,}"), "/")

private fun parentPath(path: String): String? {
    val normalized = normalizePath(path).trimEnd('/')
    val slash = normalized.lastIndexOf('/')
    return when {
        slash < 0 -> null
        slash == 0 -> "/"
        else -> normalized.substring(0, slash)
    }
}

private fun hasDoublestarPrefix(glob: String): Boolean =
    glob.startsWith("**/") || glob == "**"

private fun globMatches(pattern: String, candidate: String, caseInsensitive: Boolean, allowUnclosedClass: Boolean): Boolean {
    val regex =
        buildString {
            append("^")
            append(globToRegex(pattern, allowUnclosedClass))
            append("$")
        }.let {
            if (caseInsensitive) Regex(it, RegexOption.IGNORE_CASE) else Regex(it)
        }
    return regex.matches(candidate)
}

private fun globToRegex(pattern: String, allowUnclosedClass: Boolean): String {
    val out = StringBuilder()
    var i = 0
    while (i < pattern.length) {
        when (val c = pattern[i]) {
            '*' -> {
                if (i + 1 < pattern.length && pattern[i + 1] == '*') {
                    val slashAfter = i + 2 < pattern.length && pattern[i + 2] == '/'
                    if (slashAfter) {
                        out.append("(?:.*/)?")
                        i += 3
                    } else {
                        out.append(".*")
                        i += 2
                    }
                } else {
                    out.append("[^/]*")
                    i++
                }
            }
            '?' -> {
                out.append("[^/]")
                i++
            }
            '\\' -> {
                if (i + 1 < pattern.length) {
                    out.append(Regex.escape(pattern[i + 1].toString()))
                    i += 2
                } else {
                    out.append(Regex.escape("\\"))
                    i++
                }
            }
            '[' -> {
                val end = pattern.indexOf(']', startIndex = i + 1)
                if (end < 0) {
                    if (!allowUnclosedClass) throw Error.Glob(pattern, "unclosed character class")
                    out.append(Regex.escape("["))
                    i++
                } else {
                    out.append(pattern.substring(i, end + 1))
                    i = end + 1
                }
            }
            else -> {
                out.append(Regex.escape(c.toString()))
                i++
            }
        }
    }
    return out.toString()
}
