// port-lint: source dir.rs
@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)

package io.github.kotlinmania.ignore

import io.github.kotlinmania.io.files.Path
import io.github.kotlinmania.io.files.SystemFileSystem
import kotlin.native.HiddenFromObjC

/** Information about where an ignore decision came from. */
@HiddenFromObjC
class IgnoreMatch internal constructor(
    private val source: IgnoreMatchSource,
) {
    override fun equals(other: Any?): Boolean = other is IgnoreMatch && source == other.source

    override fun hashCode(): Int = source.hashCode()

    override fun toString(): String = "IgnoreMatch($source)"

    internal companion object {
        fun overrides(glob: OverrideGlob): IgnoreMatch = IgnoreMatch(IgnoreMatchSource.Override(glob))

        fun gitignore(glob: Glob): IgnoreMatch = IgnoreMatch(IgnoreMatchSource.Gitignore(glob))

        fun types(glob: FileTypeGlob): IgnoreMatch = IgnoreMatch(IgnoreMatchSource.Types(glob))

        fun hidden(): IgnoreMatch = IgnoreMatch(IgnoreMatchSource.Hidden)
    }
}

internal sealed class IgnoreMatchSource {
    data class Override(
        val glob: OverrideGlob,
    ) : IgnoreMatchSource()

    data class Gitignore(
        val glob: Glob,
    ) : IgnoreMatchSource()

    data class Types(
        val glob: FileTypeGlob,
    ) : IgnoreMatchSource()

    object Hidden : IgnoreMatchSource()
}

internal class IgnoreOptions(
    var hidden: Boolean = true,
    var ignore: Boolean = true,
    var parents: Boolean = true,
    var gitGlobal: Boolean = true,
    var gitIgnore: Boolean = true,
    var gitExclude: Boolean = true,
    var ignoreCaseInsensitive: Boolean = false,
    var requireGit: Boolean = true,
) {
    fun copy(): IgnoreOptions =
        IgnoreOptions(
            hidden = hidden,
            ignore = ignore,
            parents = parents,
            gitGlobal = gitGlobal,
            gitIgnore = gitIgnore,
            gitExclude = gitExclude,
            ignoreCaseInsensitive = ignoreCaseInsensitive,
            requireGit = requireGit,
        )
}

/** A compiled collection of ignore matchers active for one directory. */
@HiddenFromObjC
class Ignore internal constructor(
    private val dir: String,
    private val overrides: Override,
    private val types: Types,
    private val explicitIgnores: List<Gitignore>,
    private val customIgnoreFilenames: List<String>,
    private val customIgnoreMatcher: Gitignore,
    private val ignoreMatcher: Gitignore,
    private val gitIgnoreMatcher: Gitignore,
    private val gitExcludeMatcher: Gitignore,
    private val hasGit: Boolean,
    private val opts: IgnoreOptions,
    private val parent: Ignore? = null,
) {
    /** Returns the root path for this matcher. */
    fun path(): String = dir

    internal fun isRoot(): Boolean = parent == null

    internal fun parent(): Ignore? = parent

    /** Create a new matcher for [path] with this matcher as its parent. */
    internal fun child(path: String): Ignore = addChild(path).first

    /** Create a new matcher for [path] and return any non-fatal loading error. */
    internal fun addChild(path: String): Pair<Ignore, Error?> {
        var error: Error? = null

        fun remember(err: Error?) {
            if (err != null) error = err
        }

        val custom =
            createGitignore(
                dir = path,
                names = customIgnoreFilenames,
                caseInsensitive = opts.ignoreCaseInsensitive,
            ).also { remember(it.second) }.first
        val ignore =
            if (opts.ignore) {
                createGitignore(path, listOf(".ignore"), opts.ignoreCaseInsensitive)
                    .also { remember(it.second) }
                    .first
            } else {
                Gitignore.empty()
            }
        val gitIgnore =
            if (opts.gitIgnore) {
                createGitignore(path, listOf(".gitignore"), opts.ignoreCaseInsensitive)
                    .also { remember(it.second) }
                    .first
            } else {
                Gitignore.empty()
            }
        val gitExclude =
            if (opts.gitExclude) {
                createGitignore(joinPath(path, ".git"), listOf("info/exclude"), opts.ignoreCaseInsensitive)
                    .also { remember(it.second) }
                    .first
            } else {
                Gitignore.empty()
            }
        return Pair(
            Ignore(
                dir = path,
                overrides = overrides,
                types = types,
                explicitIgnores = explicitIgnores,
                customIgnoreFilenames = customIgnoreFilenames,
                customIgnoreMatcher = custom,
                ignoreMatcher = ignore,
                gitIgnoreMatcher = gitIgnore,
                gitExcludeMatcher = gitExclude,
                hasGit = hasDirectory(path, ".git") || hasDirectory(path, ".jj"),
                opts = opts,
                parent = this,
            ),
            error,
        )
    }

    /** Match a directory entry using override, ignore, type and hidden-file rules. */
    fun matchedDirEntry(entry: DirEntry): Match<IgnoreMatch> {
        val path = entry.path()
        val isDir = entry.fileType().isDir
        val overrideMatch = overrides.matched(path, isDir).map(IgnoreMatch::overrides)
        if (!overrideMatch.isNone()) return overrideMatch

        var whitelisted: Match<IgnoreMatch> = Match.None
        val ignoreMatch = matchedIgnore(path, isDir)
        if (ignoreMatch.isIgnore()) return ignoreMatch
        if (ignoreMatch.isWhitelist()) whitelisted = ignoreMatch

        val typeMatch = types.matched(path, isDir).map(IgnoreMatch::types)
        if (typeMatch.isIgnore()) return typeMatch
        if (typeMatch.isWhitelist()) whitelisted = typeMatch

        if (whitelisted.isNone() && opts.hidden && entry.depth() > 0 && isHidden(path)) {
            return Match.Ignore(IgnoreMatch.hidden())
        }
        return whitelisted
    }

    private fun matchedIgnore(path: String, isDir: Boolean): Match<IgnoreMatch> {
        if (!hasAnyIgnoreRules()) return Match.None
        var custom: Match<IgnoreMatch> = Match.None
        var ignore: Match<IgnoreMatch> = Match.None
        var gitIgnore: Match<IgnoreMatch> = Match.None
        var gitExclude: Match<IgnoreMatch> = Match.None
        val anyGit = !opts.requireGit || parents().any { it.hasGit }
        var sawGit = false
        for (matcher in parents()) {
            if (custom.isNone()) {
                custom = matcher.customIgnoreMatcher.matched(path, isDir).map(IgnoreMatch::gitignore)
            }
            if (ignore.isNone()) {
                ignore = matcher.ignoreMatcher.matched(path, isDir).map(IgnoreMatch::gitignore)
            }
            if (anyGit && !sawGit && gitIgnore.isNone()) {
                gitIgnore = matcher.gitIgnoreMatcher.matched(path, isDir).map(IgnoreMatch::gitignore)
            }
            if (anyGit && !sawGit && gitExclude.isNone()) {
                gitExclude = matcher.gitExcludeMatcher.matched(path, isDir).map(IgnoreMatch::gitignore)
            }
            sawGit = sawGit || matcher.hasGit
            if (!opts.parents) break
        }
        var explicit: Match<IgnoreMatch> = Match.None
        for (matcher in explicitIgnores.asReversed()) {
            if (!explicit.isNone()) break
            explicit = matcher.matched(path, isDir).map(IgnoreMatch::gitignore)
        }
        return custom
            .or(ignore)
            .or(gitIgnore)
            .or(gitExclude)
            .or(explicit)
    }

    private fun parents(): Sequence<Ignore> =
        sequence {
            var current: Ignore? = this@Ignore
            while (current != null) {
                yield(current)
                current = current.parent
            }
        }

    private fun hasAnyIgnoreRules(): Boolean =
        opts.ignore ||
            opts.gitGlobal ||
            opts.gitIgnore ||
            opts.gitExclude ||
            customIgnoreFilenames.isNotEmpty() ||
            explicitIgnores.isNotEmpty()
}

/** Builds [Ignore] matchers used by the recursive walkers. */
@HiddenFromObjC
class IgnoreBuilder {
    private var overrides: Override = Override.empty()
    private var types: Types = Types.empty()
    private val explicitIgnores = mutableListOf<Gitignore>()
    private val customIgnoreFilenames = mutableListOf<String>()
    private val opts = IgnoreOptions()
    private var currentDir: String? = null

    fun overrides(overrides: Override): IgnoreBuilder {
        this.overrides = overrides
        return this
    }

    fun types(types: Types): IgnoreBuilder {
        this.types = types
        return this
    }

    fun addIgnore(ignore: Gitignore): IgnoreBuilder {
        explicitIgnores += ignore
        return this
    }

    fun addCustomIgnoreFilename(fileName: String): IgnoreBuilder {
        customIgnoreFilenames += fileName
        return this
    }

    fun hidden(yes: Boolean): IgnoreBuilder {
        opts.hidden = yes
        return this
    }

    fun parents(yes: Boolean): IgnoreBuilder {
        opts.parents = yes
        return this
    }

    fun ignore(yes: Boolean): IgnoreBuilder {
        opts.ignore = yes
        return this
    }

    fun gitGlobal(yes: Boolean): IgnoreBuilder {
        opts.gitGlobal = yes
        return this
    }

    fun gitIgnore(yes: Boolean): IgnoreBuilder {
        opts.gitIgnore = yes
        return this
    }

    fun gitExclude(yes: Boolean): IgnoreBuilder {
        opts.gitExclude = yes
        return this
    }

    fun requireGit(yes: Boolean): IgnoreBuilder {
        opts.requireGit = yes
        return this
    }

    fun ignoreCaseInsensitive(yes: Boolean): IgnoreBuilder {
        opts.ignoreCaseInsensitive = yes
        return this
    }

    fun currentDir(cwd: String): IgnoreBuilder {
        currentDir = cwd
        return this
    }

    fun build(root: String): Ignore {
        val base =
            Ignore(
                dir = currentDir ?: "",
                overrides = overrides,
                types = types,
                explicitIgnores = explicitIgnores.toList(),
                customIgnoreFilenames = customIgnoreFilenames.toList(),
                customIgnoreMatcher = Gitignore.empty(),
                ignoreMatcher = Gitignore.empty(),
                gitIgnoreMatcher = Gitignore.empty(),
                gitExcludeMatcher = Gitignore.empty(),
                hasGit = false,
                opts = opts.copy(),
            )
        return if (root.isEmpty()) base else base.addChild(root).first
    }
}

private fun createGitignore(
    dir: String,
    names: List<String>,
    caseInsensitive: Boolean,
): Pair<Gitignore, Error?> {
    val builder = GitignoreBuilder(dir).caseInsensitive(caseInsensitive)
    var error: Error? = null
    for (name in names) {
        val path = joinPath(dir, name)
        if (SystemFileSystem.metadataOrNull(Path(path)) != null) {
            val err = builder.add(path)
            if (err != null) error = err
        }
    }
    return Pair(builder.build(), error)
}

private fun hasDirectory(path: String, name: String): Boolean {
    val metadata = SystemFileSystem.metadataOrNull(Path(joinPath(path, name))) ?: return false
    return metadata.isDirectory
}

private fun joinPath(dir: String, name: String): String =
    when {
        dir.isEmpty() -> name
        dir.endsWith("/") -> dir + name
        else -> "$dir/$name"
    }
