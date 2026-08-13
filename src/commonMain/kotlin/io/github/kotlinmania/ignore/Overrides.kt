// port-lint: source overrides.rs
@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)

package io.github.kotlinmania.ignore

import kotlin.native.HiddenFromObjC

/**
 * The overrides module provides a way to specify a set of override globs.
 *
 * This provides functionality similar to include or exclude flags in command
 * line tools.
 */

/** Glob represents a single glob in an override matcher. */
@HiddenFromObjC
class OverrideGlob internal constructor(
    private val unmatchedIgnore: Boolean,
    private val matchedGlob: Glob?,
) {
    override fun equals(other: Any?): Boolean =
        other is OverrideGlob &&
            unmatchedIgnore == other.unmatchedIgnore &&
            matchedGlob == other.matchedGlob

    override fun hashCode(): Int = 31 * unmatchedIgnore.hashCode() + (matchedGlob?.hashCode() ?: 0)

    override fun toString(): String =
        if (unmatchedIgnore) "OverrideGlob(unmatchedIgnore)" else "OverrideGlob($matchedGlob)"

    internal companion object {
        fun unmatched(): OverrideGlob = OverrideGlob(true, null)

        fun matched(glob: Glob): OverrideGlob = OverrideGlob(false, glob)
    }
}

/** Manages a set of overrides provided explicitly by the end user. */
@HiddenFromObjC
class Override internal constructor(
    private val gitignore: Gitignore,
) {
    /** Returns the directory of this override set. */
    fun path(): String = gitignore.path()

    /** Returns true if and only if this matcher is empty. */
    fun isEmpty(): Boolean = gitignore.isEmpty()

    /** Returns the total number of ignore globs. */
    fun numIgnores(): Long = gitignore.numWhitelists()

    /** Returns the total number of whitelisted globs. */
    fun numWhitelists(): Long = gitignore.numIgnores()

    /** Returns whether the given file path matched a pattern in this override matcher. */
    fun matched(path: String, isDir: Boolean): Match<OverrideGlob> {
        if (isEmpty()) return Match.None
        val decision = gitignore.matched(path, isDir).invert()
        if (decision.isNone() && numWhitelists() > 0 && !isDir) {
            return Match.Ignore(OverrideGlob.unmatched())
        }
        return decision.map { OverrideGlob.matched(it) }
    }

    companion object {
        /** Returns an empty matcher that never matches any file path. */
        fun empty(): Override = Override(Gitignore.empty())
    }
}

/** Builds a matcher for a set of glob overrides. */
@HiddenFromObjC
class OverrideBuilder(
    path: String,
) {
    private val builder = GitignoreBuilder(path).allowUnclosedClass(false)

    /** Builds a new override matcher from the globs added so far. */
    fun build(): Override = Override(builder.build())

    /** Add a glob to the set of overrides. */
    fun add(glob: String): OverrideBuilder {
        builder.addOverrideLine(glob)
        return this
    }

    /** Toggle whether the globs should be matched case insensitively or not. */
    fun caseInsensitive(yes: Boolean): OverrideBuilder {
        builder.caseInsensitive(yes)
        return this
    }

    /** Toggle whether unclosed character classes are allowed. */
    fun allowUnclosedClass(yes: Boolean): OverrideBuilder {
        builder.allowUnclosedClass(yes)
        return this
    }
}
