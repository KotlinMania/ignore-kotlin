// port-lint: source lib.rs
package io.github.kotlinmania.ignore

/**
 * The result of a glob match.
 *
 * The type parameter [T] typically refers to a type that provides more
 * information about a particular match. For example, it might identify
 * the specific gitignore file and the specific glob pattern that caused
 * the match.
 */
sealed class Match<out T> {
    /** The path didn't match any glob. */
    data object None : Match<Nothing>()

    /** The highest precedent glob matched indicates the path should be ignored. */
    data class Ignore<out T>(
        val value: T,
    ) : Match<T>()

    /** The highest precedent glob matched indicates the path should be whitelisted. */
    data class Whitelist<out T>(
        val value: T,
    ) : Match<T>()

    /** Returns true if the match result didn't match any globs. */
    fun isNone(): Boolean = this is None

    /** Returns true if the match result implies the path should be ignored. */
    fun isIgnore(): Boolean = this is Ignore

    /** Returns true if the match result implies the path should be whitelisted. */
    fun isWhitelist(): Boolean = this is Whitelist

    /** Inverts the match so that [Ignore] becomes [Whitelist] and [Whitelist] becomes [Ignore]. */
    fun invert(): Match<T> =
        when (this) {
            is None -> None
            is Ignore -> Whitelist(value)
            is Whitelist -> Ignore(value)
        }

    /** Return the value inside this match if it exists. */
    fun inner(): T? =
        when (this) {
            is None -> null
            is Ignore -> value
            is Whitelist -> value
        }

    /** Apply the given function to the value inside this match. */
    inline fun <U> map(f: (T) -> U): Match<U> =
        when (this) {
            is None -> None
            is Ignore -> Ignore(f(value))
            is Whitelist -> Whitelist(f(value))
        }

    /** Return the match if it is not none. Otherwise, return [other]. */
    fun or(other: Match<@UnsafeVariance T>): Match<T> =
        if (isNone()) other else this
}
