// port-lint: source lib.rs
package io.github.kotlinmania.ignore

/**
 * An exception wrapping an [IgnoreError].
 */
class IgnoreException(
    val error: IgnoreError,
) : Exception(error.toString())

/**
 * Represents an error that can occur when parsing a gitignore file or walking a directory tree.
 */
sealed class IgnoreError {
    /** A collection of "soft" errors. These occur when adding an ignore
     * file partially succeeded. */
    data class Partial(
        val errors: List<IgnoreError>,
    ) : IgnoreError()

    /** An error associated with a specific line number. */
    data class WithLineNumber(
        /** The line number. */
        val line: Long,
        /** The underlying error. */
        val err: IgnoreError,
    ) : IgnoreError()

    /** An error associated with a particular file path. */
    data class WithPath(
        /** The file path. */
        val path: String,
        /** The underlying error. */
        val err: IgnoreError,
    ) : IgnoreError()

    /** An error associated with a particular directory depth when recursively
     * walking a directory. */
    data class WithDepth(
        /** The directory depth. */
        val depth: Int,
        /** The underlying error. */
        val err: IgnoreError,
    ) : IgnoreError()

    /** An error that occurs when a file loop is detected when traversing
     * symbolic links. */
    data class Loop(
        /** The ancestor file path in the loop. */
        val ancestor: String,
        /** The child file path in the loop. */
        val child: String,
    ) : IgnoreError()

    /** An error that occurs when doing I/O, such as reading an ignore file. */
    data class Io(
        val err: Exception,
    ) : IgnoreError()

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
    ) : IgnoreError()

    /** A type selection for a file type that is not defined. */
    data class UnrecognizedFileType(
        val type: String,
    ) : IgnoreError()

    /** A user specified file type definition could not be parsed. */
    data object InvalidDefinition : IgnoreError()

    /** Wraps this error in an [IgnoreException]. */
    fun asException(): IgnoreException = IgnoreException(this)

    /** Returns true if this is a partial error.
     *
     * A partial error occurs when only some operations failed while others
     * may have succeeded. For example, an ignore file may contain an invalid
     * glob among otherwise valid globs. */
    fun isPartial(): Boolean =
        when (this) {
            is Partial -> true
            is WithLineNumber -> err.isPartial()
            is WithPath -> err.isPartial()
            is WithDepth -> err.isPartial()
            else -> false
        }

    /** Returns true if this error is exclusively an I/O error. */
    fun isIo(): Boolean =
        when (this) {
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
     * [null] is returned if this [IgnoreError] doesn't correspond to an I/O error.
     * This might happen, for example, when the error was produced because a
     * cycle was found in the directory tree while following symbolic links. */
    fun ioError(): Exception? =
        when (this) {
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
    fun depth(): Int? =
        when (this) {
            is WithPath -> err.depth()
            is WithDepth -> depth
            else -> null
        }

    /** Turn an error into a tagged error with the given file path. */
    fun withPath(path: String): IgnoreError = WithPath(path, this)

    /** Turn an error into a tagged error with the given depth. */
    fun withDepth(depth: Int): IgnoreError = WithDepth(depth, this)

    /** Turn an error into a tagged error with the given file path and line
     * number. If path is empty, then it is omitted from the error. */
    fun tagged(path: String, lineNo: Long): IgnoreError {
        val errLine = WithLineNumber(lineNo, this)
        return if (path.isEmpty()) errLine else errLine.withPath(path)
    }

    override fun toString(): String =
        when (this) {
            is Partial -> {
                errors.joinToString("\n")
            }

            is WithLineNumber -> {
                "line $line: $err"
            }

            is WithPath -> {
                "$path: $err"
            }

            is WithDepth -> {
                err.toString()
            }

            is Loop -> {
                "File system loop found: $child points to an ancestor $ancestor"
            }

            is Io -> {
                err.toString()
            }

            is Glob -> {
                when (glob) {
                    null -> err
                    else -> "error parsing glob '$glob': $err"
                }
            }

            is UnrecognizedFileType -> {
                "unrecognized file type: $type"
            }

            is InvalidDefinition -> {
                "invalid definition (format is type:glob, e.g., html:*.html)"
            }
        }
}

internal class PartialErrorBuilder {
    private val errors: MutableList<IgnoreError> = mutableListOf()

    fun push(err: IgnoreError) {
        errors.add(err)
    }

    fun pushIgnoreIo(err: IgnoreError) {
        if (!err.isIo()) push(err)
    }

    fun maybePush(err: IgnoreError?) {
        if (err != null) push(err)
    }

    fun maybePushIgnoreIo(err: IgnoreError?) {
        if (err != null) pushIgnoreIo(err)
    }

    fun intoErrorOption(): IgnoreError? =
        when {
            errors.isEmpty() -> null
            errors.size == 1 -> errors.removeAt(0)
            else -> IgnoreError.Partial(errors.toList())
        }
}
