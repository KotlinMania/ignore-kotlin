// port-lint: source pathutil.rs
package io.github.kotlinmania.ignore

/** Returns true if and only if this path is considered to be hidden.
 *
 * This only returns true if the base name of the path starts with a `.`.
 *
 * @param path The entry path to inspect. */
internal fun isHidden(path: String): Boolean {
    val name = fileName(path) ?: return false
    return name.startsWith(".")
}

/** Strip [prefix] from the [path] and return the remainder.
 *
 * @param prefix The prefix to remove from the path.
 * @param path The path from which the prefix is stripped.
 * @return The remainder after stripping [prefix], or `null` if [path] doesn't
 *   start with [prefix]. */
internal fun stripPrefix(prefix: String, path: String): String? {
    return if (path.startsWith(prefix)) path.substring(prefix.length) else null
}

/** Returns true if this file path is just a file name. That is, its parent is
 * the empty string.
 *
 * @param path The file path to inspect. */
internal fun isFileName(path: String): Boolean {
    return !path.contains('/') && !path.contains('\\')
}

/** The final component of the path, if it is a normal file.
 *
 * @param path The file path to inspect.
 * @return The final path component, or `null` if the path terminates in `.`,
 *   `..`, or consists solely of a root or prefix. */
internal fun fileName(path: String): String? {
    if (path.isEmpty()) return null
    if (path == ".") return null
    if (path.endsWith('.')) return null
    if (path.endsWith("..")) return null

    val lastSlash = path.lastIndexOf('/')
    val lastBackslash = path.lastIndexOf('\\')
    val lastSep = maxOf(lastSlash, lastBackslash)

    val name = if (lastSep < 0) path else path.substring(lastSep + 1)
    return if (name.isEmpty() || name == "." || name == "..") null else name
}
