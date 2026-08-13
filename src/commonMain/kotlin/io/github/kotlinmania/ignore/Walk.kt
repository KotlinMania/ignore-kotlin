// port-lint: source walk.rs
@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)

package io.github.kotlinmania.ignore

import io.github.kotlinmania.io.files.FileMetadata
import io.github.kotlinmania.io.files.Path
import io.github.kotlinmania.io.files.SystemFileSystem
import kotlin.native.HiddenFromObjC

/** A directory entry with a possible error attached. */
@HiddenFromObjC
class DirEntry internal constructor(
    private val pathValue: String,
    private val fileTypeValue: IgnoreFileType,
    private val depthValue: Int,
    private val errorValue: Error? = null,
) {
    /** The full path that this entry represents. */
    fun path(): String = pathValue

    /** The full path that this entry represents. */
    fun intoPath(): String = pathValue

    /** Whether this entry corresponds to a symbolic link or not. */
    fun pathIsSymlink(): Boolean = false

    /** Returns true if and only if this entry corresponds to stdin. */
    fun isStdin(): Boolean = pathValue == "<stdin>"

    /** Return the metadata for the file that this entry points to. */
    fun metadata(): IgnoreMetadata? = SystemFileSystem.metadataOrNull(Path(pathValue))?.toIgnoreMetadata()

    /** Return the file type for the file that this entry points to. */
    fun fileType(): IgnoreFileType = fileTypeValue

    /** Return the file name of this entry. */
    fun fileName(): String = fileName(pathValue) ?: pathValue

    /** Returns the depth at which this entry was created relative to the root. */
    fun depth(): Int = depthValue

    /** Returns an error associated with processing this entry, if one exists. */
    fun error(): Error? = errorValue

    override fun equals(other: Any?): Boolean =
        other is DirEntry &&
            pathValue == other.pathValue &&
            fileTypeValue == other.fileTypeValue &&
            depthValue == other.depthValue &&
            errorValue == other.errorValue

    override fun hashCode(): Int {
        var result = pathValue.hashCode()
        result = 31 * result + fileTypeValue.hashCode()
        result = 31 * result + depthValue
        result = 31 * result + (errorValue?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = "DirEntry($pathValue)"
}

/** File type information for a [DirEntry]. */
@HiddenFromObjC
data class IgnoreFileType(
    val isDir: Boolean,
    val isFile: Boolean,
)

/** Metadata for a [DirEntry]. */
@HiddenFromObjC
data class IgnoreMetadata(
    val fileType: IgnoreFileType,
    val len: Long,
)

private fun FileMetadata.toIgnoreMetadata(): IgnoreMetadata =
    IgnoreMetadata(IgnoreFileType(isDirectory, isRegularFile), size)

/** WalkBuilder builds a recursive directory iterator. */
@HiddenFromObjC
class WalkBuilder(
    path: String,
) {
    private val paths = mutableListOf(path)
    private val ignoreBuilder = IgnoreBuilder()
    private var maxDepth: Int? = null
    private var minDepth: Int? = null
    private var maxFilesize: Long? = null
    private var followLinks = false
    private var threads = 0
    private var sorter: Comparator<String>? = null
    private var filter: ((DirEntry) -> Boolean)? = null

    /** Build a new [Walk] iterator. */
    fun build(): Walk =
        Walk(
            paths = paths.toList(),
            ignore = ignoreBuilder.build(""),
            maxDepth = maxDepth,
            minDepth = minDepth,
            maxFilesize = maxFilesize,
            sorter = sorter,
            filter = filter,
        )

    /** Build a new [WalkParallel] iterator. */
    fun buildParallel(): WalkParallel = WalkParallel(build(), threads)

    /** Add a file path to the iterator. */
    fun add(path: String): WalkBuilder {
        paths += path
        return this
    }

    /** The maximum depth to recurse. */
    fun maxDepth(depth: Int?): WalkBuilder {
        maxDepth = depth
        if (minDepth != null && maxDepth != null && maxDepth!! < minDepth!!) {
            maxDepth = minDepth
        }
        return this
    }

    /** The minimum depth to recurse. */
    fun minDepth(depth: Int?): WalkBuilder {
        minDepth = depth
        if (maxDepth != null && minDepth != null && minDepth!! > maxDepth!!) {
            minDepth = maxDepth
        }
        return this
    }

    /** Whether to follow symbolic links or not. */
    fun followLinks(yes: Boolean): WalkBuilder {
        followLinks = yes
        return this
    }

    /** Whether to ignore files above the specified limit. */
    fun maxFilesize(filesize: Long?): WalkBuilder {
        maxFilesize = filesize
        return this
    }

    /** The number of threads to use for traversal. */
    fun threads(n: Int): WalkBuilder {
        threads = n
        return this
    }

    /** Add an override matcher. */
    fun overrides(overrides: Override): WalkBuilder {
        ignoreBuilder.overrides(overrides)
        return this
    }

    /** Add a global ignore matcher from an explicit ignore file. */
    fun addIgnore(ignore: Gitignore): WalkBuilder {
        ignoreBuilder.addIgnore(ignore)
        return this
    }

    /** Add a custom ignore file name to read in each directory. */
    fun addCustomIgnoreFilename(fileName: String): WalkBuilder {
        ignoreBuilder.addCustomIgnoreFilename(fileName)
        return this
    }

    /** Add a file type matcher. */
    fun types(types: Types): WalkBuilder {
        ignoreBuilder.types(types)
        return this
    }

    /** Enables all the standard ignore filters. */
    fun standardFilters(yes: Boolean): WalkBuilder =
        hidden(yes)
            .parents(yes)
            .ignore(yes)
            .gitIgnore(yes)
            .gitGlobal(yes)
            .gitExclude(yes)

    /** Enables ignoring hidden files. */
    fun hidden(yes: Boolean): WalkBuilder {
        ignoreBuilder.hidden(yes)
        return this
    }

    /** Enables reading ignore files from parent directories. */
    fun parents(yes: Boolean): WalkBuilder {
        ignoreBuilder.parents(yes)
        return this
    }

    /** Enables reading `.ignore` files. */
    fun ignore(yes: Boolean): WalkBuilder {
        ignoreBuilder.ignore(yes)
        return this
    }

    /** Enables reading a global gitignore file. */
    fun gitGlobal(yes: Boolean): WalkBuilder {
        ignoreBuilder.gitGlobal(yes)
        return this
    }

    /** Enables reading `.gitignore` files. */
    fun gitIgnore(yes: Boolean): WalkBuilder {
        ignoreBuilder.gitIgnore(yes)
        return this
    }

    /** Enables reading `.git/info/exclude` files. */
    fun gitExclude(yes: Boolean): WalkBuilder {
        ignoreBuilder.gitExclude(yes)
        return this
    }

    /** Whether a git repository is required to apply git-related ignore rules. */
    fun requireGit(yes: Boolean): WalkBuilder {
        ignoreBuilder.requireGit(yes)
        return this
    }

    /** Process ignore files case insensitively. */
    fun ignoreCaseInsensitive(yes: Boolean): WalkBuilder {
        ignoreBuilder.ignoreCaseInsensitive(yes)
        return this
    }

    /** Set a function for sorting directory entries by their path. */
    @HiddenFromObjC
    fun sortByFilePath(cmp: Comparator<String>): WalkBuilder {
        sorter = cmp
        return this
    }

    /** Do not cross file system boundaries. */
    fun sameFileSystem(yes: Boolean): WalkBuilder = this

    /** Do not yield directory entries that are believed to correspond to stdout. */
    fun skipStdout(yes: Boolean): WalkBuilder = this

    /** Yields only entries which satisfy the given predicate. */
    @HiddenFromObjC
    fun filterEntry(filter: (DirEntry) -> Boolean): WalkBuilder {
        this.filter = filter
        return this
    }

    /** Set the current working directory used for matching global gitignores. */
    fun currentDir(cwd: String): WalkBuilder {
        ignoreBuilder.currentDir(cwd)
        return this
    }
}

/** Walk is a recursive directory iterator over file paths in one or more directories. */
@HiddenFromObjC
class Walk internal constructor(
    private val paths: List<String>,
    private val ignore: Ignore,
    private val maxDepth: Int?,
    private val minDepth: Int?,
    private val maxFilesize: Long?,
    private val sorter: Comparator<String>?,
    private val filter: ((DirEntry) -> Boolean)?,
) : Iterable<Result<DirEntry>> {
    override fun iterator(): Iterator<Result<DirEntry>> =
        paths.asSequence().flatMap { walkPath(it, 0, ignore).asSequence() }.iterator()

    private fun walkPath(path: String, depth: Int, activeIgnore: Ignore): List<Result<DirEntry>> {
        val metadata =
            SystemFileSystem.metadataOrNull(Path(path))
                ?: return listOf(Result.failure(Error.Io(Exception("No such file or directory: $path"))))
        val entry = DirEntry(path, metadata.toIgnoreMetadata().fileType, depth)
        val minAllows = minDepth?.let { depth >= it } ?: true
        val output = mutableListOf<Result<DirEntry>>()
        val decision = activeIgnore.matchedDirEntry(entry)
        val ignored = depth > 0 && decision.isIgnore()
        val filtered = filter?.invoke(entry) == false
        val tooLarge = maxFilesize?.let { !entry.fileType().isDir && metadata.size > it } ?: false
        if (!ignored && !filtered && !tooLarge && minAllows) {
            output += Result.success(entry)
        }
        if (ignored || filtered || !entry.fileType().isDir) return output
        if (maxDepth?.let { depth >= it } == true) return output
        val children =
            try {
                SystemFileSystem.list(Path(path)).map { it.toString() }
            } catch (err: Exception) {
                output += Result.failure(Error.Io(err).withPath(path).withDepth(depth))
                return output
            }.let { childPaths ->
                sorter?.let { childPaths.sortedWith(it) } ?: childPaths
            }
        val childIgnore = activeIgnore.child(path)
        for (child in children) {
            output += walkPath(child, depth + 1, childIgnore)
        }
        return output
    }

    companion object {
        /** Creates a new recursive directory iterator for the file path given. */
        fun new(path: String): Walk = WalkBuilder(path).build()
    }
}

/** WalkState indicates whether walking should continue, skip or quit. */
@HiddenFromObjC
enum class WalkState {
    Continue,
    Skip,
    Quit,
}

/** A parallel recursive directory iterator facade. */
@HiddenFromObjC
class WalkParallel internal constructor(
    private val walk: Walk,
    private val threads: Int,
) {
    /** Execute the recursive directory iterator. */
    @HiddenFromObjC
    fun run(visitorFactory: () -> (Result<DirEntry>) -> WalkState) {
        val visitor = visitorFactory()
        for (entry in walk) {
            when (visitor(entry)) {
                WalkState.Continue,
                WalkState.Skip,
                -> Unit
                WalkState.Quit -> return
            }
        }
    }
}
