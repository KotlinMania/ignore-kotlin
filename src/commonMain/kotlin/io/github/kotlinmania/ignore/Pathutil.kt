// port-lint: source src/pathutil.rs
package io.github.kotlinmania.ignore

/** Strip [prefix] from the [path] and return the remainder.
 *
 * If [path] doesn't have a prefix equal to [prefix], then return `null`. */
internal fun stripPrefix(prefix: String, path: String): String? {
    return if (path.startsWith(prefix)) path.substring(prefix.length) else null
}

/** Returns true if this file path is just a file name. That is, its parent is
 * the empty string. */
internal fun isFileName(path: String): Boolean {
    return !path.contains('/') && !path.contains('\\')
}

/** The final component of the path, if it is a normal file.
 *
 * If the path terminates in `.`, `..`, or consists solely of a root or
 * prefix, [fileName] will return `null`. */
internal fun fileName(path: String): String? {
    if (path.isEmpty()) return null
    if (path == ".") return null
    if (path.endsWith('.') && path.length > 1 && path[path.length - 2] == '/') return null
    if (path.endsWith("..")) return null

    val lastSlash = path.lastIndexOf('/')
    val lastBackslash = path.lastIndexOf('\\')
    val lastSep = maxOf(lastSlash, lastBackslash)

    val name = if (lastSep < 0) path else path.substring(lastSep + 1)
    return if (name.isEmpty() || name == "." || name == "..") null else name
}
