// port-lint: source lib.rs
package io.github.kotlinmania.ignore

/**
 * The ignore library provides a fast recursive directory iterator that respects
 * various filters such as globs, file types and `.gitignore` files. The precise
 * matching rules and precedence is explained in the documentation for
 * [WalkBuilder].
 *
 * Secondarily, this library exposes gitignore and file type matchers for use
 * cases that demand more fine-grained control.
 *
 * Example:
 *
 * This example shows the most basic usage of this library. This code will
 * recursively traverse the current directory while automatically filtering out
 * files and directories according to ignore globs found in files like
 * `.ignore` and `.gitignore`:
 *
 * ```kotlin
 * for (result in Walk("./")) {
 *     // Each item yielded by the iterator is either a directory entry or an
 *     // error, so either print the path or the error.
 *     result.fold(
 *         onSuccess = { entry -> println(entry.path()) },
 *         onFailure = { err -> println("ERROR: $err") },
 *     )
 * }
 * ```
 *
 * Example: advanced
 *
 * By default, the recursive directory iterator will ignore hidden files and
 * directories. This can be disabled by building the iterator with [WalkBuilder]:
 *
 * ```kotlin
 * for (result in WalkBuilder("./").hidden(false).build()) {
 *     println(result)
 * }
 * ```
 *
 * See the documentation for [WalkBuilder] for many other options.
 */

/**
 * Represents an error that can occur when parsing a gitignore file.
 */
sealed class Error : kotlin.Exception() {
    /** A collection of "soft" errors. These occur when adding an ignore
     * file partially succeeded. */
    data class Partial(val errors: List<Error>) : Error()

    /** An error associated with a specific line number. */
    data class WithLineNumber(
        /** The line number. */
        val line: Long,
        /** The underlying error. */
        val err: Error,
    ) : Error()

    /** An error associated with a particular file path. */
    data class WithPath(
        /** The file path. */
        val path: String,
        /** The underlying error. */
        val err: Error,
    ) : Error()

    /** An error associated with a particular directory depth when recursively
     * walking a directory. */
    data class WithDepth(
        /** The directory depth. */
        val depth: Int,
        /** The underlying error. */
        val err: Error,
    ) : Error()

    /** An error that occurs when a file loop is detected when traversing
     * symbolic links. */
    data class Loop(
        /** The ancestor file path in the loop. */
        val ancestor: String,
        /** The child file path in the loop. */
        val child: String,
    ) : Error()

    /** An error that occurs when doing I/O, such as reading an ignore file. */
    data class Io(val err: Exception) : Error()

    /** An error that occurs when trying to parse a glob.
     *
     * The [glob] field, when available, always corresponds to the glob
     * provided by an end user. E.g., it is the glob as written in a
     * `.gitignore` file.
     *
     * (This glob may be distinct from the glob that is actually compiled,
     * after accounting for `gitignore` semantics.) */
    data class Glob(
        /** The original glob that caused this error, if available. */
        val glob: String?,
        /** The underlying glob error as a string. */
        val err: String,
    ) : Error()

    /** A type selection for a file type that is not defined. */
    data class UnrecognizedFileType(val type: String) : Error()

    /** A user specified file type definition could not be parsed. */
    object InvalidDefinition : Error()

    /** Returns true if this is a partial error.
     *
     * A partial error occurs when only some operations failed while others
     * may have succeeded. For example, an ignore file may contain an invalid
     * glob among otherwise valid globs. */
    fun isPartial(): Boolean = when (this) {
        is Partial -> true
        is WithLineNumber -> err.isPartial()
        is WithPath -> err.isPartial()
        is WithDepth -> err.isPartial()
        else -> false
    }

    /** Returns true if this error is exclusively an I/O error. */
    fun isIo(): Boolean = when (this) {
        is Partial -> errors.size == 1 && errors[0].isIo()
        is WithLineNumber -> err.isIo()
        is WithPath -> err.isIo()
        is WithDepth -> err.isIo()
        is Loop -> false
        is Io -> true
        is Glob -> false
        is UnrecognizedFileType -> false
        is InvalidDefinition -> false
    }

    /** Inspect the original [Exception] if there is one.
     *
     * [null] is returned if this [Error] doesn't correspond to an I/O error.
     * This might happen, for example, when the error was produced because a
     * cycle was found in the directory tree while following symbolic links. */
    fun ioError(): Exception? = when (this) {
        is Partial -> if (errors.size == 1) errors[0].ioError() else null
        is WithLineNumber -> err.ioError()
        is WithPath -> err.ioError()
        is WithDepth -> err.ioError()
        is Loop -> null
        is Io -> err
        is Glob -> null
        is UnrecognizedFileType -> null
        is InvalidDefinition -> null
    }

    /** Similar to [ioError] except returns null when no I/O error exists. */
    fun intoIoError(): Exception? = ioError()

    /** Returns a depth associated with recursively walking a directory (if
     * this error was generated from a recursive directory iterator). */
    fun depth(): Int? = when (this) {
        is WithPath -> err.depth()
        is WithDepth -> depth
        else -> null
    }

    /** Turn an error into a tagged error with the given file path. */
    internal fun withPath(path: String): Error = WithPath(path, this)

    /** Turn an error into a tagged error with the given depth. */
    internal fun withDepth(depth: Int): Error = WithDepth(depth, this)

    /** Turn an error into a tagged error with the given file path and line
     * number. If path is empty, then it is omitted from the error. */
    internal fun tagged(path: String, lineNo: Long): Error {
        val errLine = WithLineNumber(lineNo, this)
        return if (path.isEmpty()) errLine else errLine.withPath(path)
    }

    override fun toString(): String = when (this) {
        is Partial -> errors.joinToString("\n")
        is WithLineNumber -> "line $line: $err"
        is WithPath -> "$path: $err"
        is WithDepth -> err.toString()
        is Loop -> "File system loop found: $child points to an ancestor $ancestor"
        is Io -> err.toString()
        is Glob -> when (glob) {
            null -> err
            else -> "error parsing glob '$glob': $err"
        }
        is UnrecognizedFileType -> "unrecognized file type: $type"
        is InvalidDefinition ->
            "invalid definition (format is type:glob, e.g., html:*.html)"
    }
}

/** The result of a glob match.
 *
 * The type parameter [T] typically refers to a type that provides more
 * information about a particular match. For example, it might identify
 * the specific gitignore file and the specific glob pattern that caused
 * the match. */
sealed class Match<out T> {
    /** The path didn't match any glob. */
    object None : Match<Nothing>()

    /** The highest precedent glob matched indicates the path should be
     * ignored. */
    data class Ignore<out T>(val value: T) : Match<T>()

    /** The highest precedent glob matched indicates the path should be
     * whitelisted. */
    data class Whitelist<out T>(val value: T) : Match<T>()

    /** Returns true if the match result didn't match any globs. */
    fun isNone(): Boolean = this is None

    /** Returns true if the match result implies the path should be ignored. */
    fun isIgnore(): Boolean = this is Ignore

    /** Returns true if the match result implies the path should be
     * whitelisted. */
    fun isWhitelist(): Boolean = this is Whitelist

    /** Inverts the match so that [Ignore] becomes [Whitelist] and
     * [Whitelist] becomes [Ignore]. A non-match remains the same. */
    fun invert(): Match<T> = when (this) {
        is None -> None
        is Ignore -> Whitelist(value)
        is Whitelist -> Ignore(value)
    }

    /** Return the value inside this match if it exists. */
    fun inner(): T? = when (this) {
        is None -> null
        is Ignore -> value
        is Whitelist -> value
    }

    /** Apply the given function to the value inside this match.
     *
     * If the match has no value, then return the match unchanged. */
    fun <U> map(f: (T) -> U): Match<U> = when (this) {
        is None -> None
        is Ignore -> Ignore(f(value))
        is Whitelist -> Whitelist(f(value))
    }

    /** Return the match if it is not none. Otherwise, return [other]. */
    fun or(other: Match<@UnsafeVariance T>): Match<T> =
        if (isNone()) other else this
}

internal class PartialErrorBuilder {
    private val errors: MutableList<Error> = mutableListOf()

    fun push(err: Error) {
        errors.add(err)
    }

    fun pushIgnoreIo(err: Error) {
        if (!err.isIo()) push(err)
    }

    fun maybePush(err: Error?) {
        if (err != null) push(err)
    }

    fun maybePushIgnoreIo(err: Error?) {
        if (err != null) pushIgnoreIo(err)
    }

    fun intoErrorOption(): Error? = when {
        errors.isEmpty() -> null
        errors.size == 1 -> errors.removeAt(0)
        else -> Error.Partial(errors.toList())
    }
}
