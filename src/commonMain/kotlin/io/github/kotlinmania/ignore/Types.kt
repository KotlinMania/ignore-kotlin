// port-lint: source types.rs
@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)

package io.github.kotlinmania.ignore

import kotlin.native.HiddenFromObjC

/** A single file type definition. */
@HiddenFromObjC
class FileTypeDef internal constructor(
    private val nameValue: String,
    private val globsValue: List<String>,
) {
    /** Return the name of this file type. */
    fun name(): String = nameValue

    /** Return the globs used to recognize this file type. */
    fun globs(): List<String> = globsValue

    internal fun withGlob(glob: String): FileTypeDef = FileTypeDef(nameValue, globsValue + glob)

    internal fun sorted(): FileTypeDef = FileTypeDef(nameValue, globsValue.sorted())

    internal fun matches(path: String): Boolean {
        val name = fileName(path) ?: return false
        return globsValue.any { glob -> globMatchesName(glob, name) }
    }

    override fun equals(other: Any?): Boolean =
        other is FileTypeDef && nameValue == other.nameValue && globsValue == other.globsValue

    override fun hashCode(): Int = 31 * nameValue.hashCode() + globsValue.hashCode()

    override fun toString(): String = "FileTypeDef(name=$nameValue, globs=$globsValue)"
}

/** Types is a file type matcher. */
@HiddenFromObjC
class Types internal constructor(
    private val defs: List<FileTypeDef>,
    private val selections: List<TypeSelection<FileTypeDef>>,
) {
    private val hasSelected = selections.any { !it.isNegated() }

    /** Returns true if and only if this matcher has zero selections. */
    fun isEmpty(): Boolean = selections.isEmpty()

    /** Returns the number of selections used in this matcher. */
    fun len(): Int = selections.size

    /** Return the set of current file type definitions. */
    fun definitions(): List<FileTypeDef> = defs

    /** Returns a decision for the given path against this file type matcher. */
    fun matched(path: String, isDir: Boolean = false): Match<FileTypeGlob> {
        if (isDir || selections.isEmpty()) return Match.None
        if (fileName(path) == null) {
            return if (hasSelected) Match.Ignore(FileTypeGlob.unmatched()) else Match.None
        }
        var lastSelection: TypeSelection<FileTypeDef>? = null
        for (selection in selections) {
            if (selection.inner().matches(path)) {
                lastSelection = selection
            }
        }
        if (lastSelection != null) {
            val glob = FileTypeGlob.matched(lastSelection.inner())
            return if (lastSelection.isNegated()) Match.Ignore(glob) else Match.Whitelist(glob)
        }
        return if (hasSelected) Match.Ignore(FileTypeGlob.unmatched()) else Match.None
    }

    companion object {
        /** Creates a new file type matcher that never matches any path. */
        fun empty(): Types = Types(emptyList(), emptyList())
    }
}

/** Information about the file type decision that was made. */
@HiddenFromObjC
class FileTypeGlob internal constructor(private val def: FileTypeDef?) {
    /** Return the file type definition that matched, if one exists. */
    fun fileTypeDef(): FileTypeDef? = def

    override fun equals(other: Any?): Boolean = other is FileTypeGlob && def == other.def

    override fun hashCode(): Int = def?.hashCode() ?: 0

    override fun toString(): String = if (def == null) "FileTypeGlob(unmatched)" else "FileTypeGlob($def)"

    internal companion object {
        fun unmatched(): FileTypeGlob = FileTypeGlob(null)
        fun matched(def: FileTypeDef): FileTypeGlob = FileTypeGlob(def)
    }
}

internal sealed class TypeSelection<T> {
    abstract fun isNegated(): Boolean
    abstract fun name(): String
    abstract fun inner(): T
    abstract fun <U> map(transform: (T) -> U): TypeSelection<U>

    class Select<T>(private val nameValue: String, private val innerValue: T) : TypeSelection<T>() {
        override fun isNegated(): Boolean = false
        override fun name(): String = nameValue
        override fun inner(): T = innerValue
        override fun <U> map(transform: (T) -> U): TypeSelection<U> = Select(nameValue, transform(innerValue))
    }

    class Negate<T>(private val nameValue: String, private val innerValue: T) : TypeSelection<T>() {
        override fun isNegated(): Boolean = true
        override fun name(): String = nameValue
        override fun inner(): T = innerValue
        override fun <U> map(transform: (T) -> U): TypeSelection<U> = Negate(nameValue, transform(innerValue))
    }
}

/** TypesBuilder builds a type matcher from definitions and selections. */
@HiddenFromObjC
class TypesBuilder {
    private val types = mutableMapOf<String, FileTypeDef>()
    private val selections = mutableListOf<TypeSelection<Unit>>()

    /** Build the current definitions and selections into a file type matcher. */
    fun build(): Types {
        val definitions = definitions()
        val builtSelections = mutableListOf<TypeSelection<FileTypeDef>>()
        for (selection in selections) {
            val def = types[selection.name()] ?: throw Error.UnrecognizedFileType(selection.name())
            builtSelections += selection.map { def }
        }
        return Types(definitions, builtSelections)
    }

    /** Return the set of current file type definitions. */
    fun definitions(): List<FileTypeDef> =
        types.values.map { it.sorted() }.sortedBy { it.name() }

    /** Select the file type given by [name]. */
    fun select(name: String): TypesBuilder {
        if (name == "all") {
            for (typeName in types.keys.sorted()) {
                selections += TypeSelection.Select(typeName, Unit)
            }
        } else {
            selections += TypeSelection.Select(name, Unit)
        }
        return this
    }

    /** Ignore the file type given by [name]. */
    fun negate(name: String): TypesBuilder {
        if (name == "all") {
            for (typeName in types.keys.sorted()) {
                selections += TypeSelection.Negate(typeName, Unit)
            }
        } else {
            selections += TypeSelection.Negate(name, Unit)
        }
        return this
    }

    /** Clear any file type definitions for the type name given. */
    fun clear(name: String): TypesBuilder {
        types.remove(name)
        return this
    }

    /** Add one glob to a named file type. */
    fun add(name: String, glob: String): TypesBuilder {
        validateTypeName(name)
        val existing = types[name]
        types[name] = existing?.withGlob(glob) ?: FileTypeDef(name, listOf(glob))
        return this
    }

    /** Add a file type definition in string form. */
    fun add(definition: String): TypesBuilder = addDef(definition)

    /** Add a file type definition in string form. */
    fun addDef(definition: String): TypesBuilder {
        val parts = definition.split(":")
        when (parts.size) {
            2 -> {
                val name = parts[0]
                val glob = parts[1]
                if (name.isEmpty() || glob.isEmpty()) throw Error.InvalidDefinition
                add(name, glob)
            }
            3 -> {
                val name = parts[0]
                val include = parts[1]
                val typeNames = parts[2]
                if (name.isEmpty() || include != "include" || typeNames.isEmpty()) {
                    throw Error.InvalidDefinition
                }
                val includes = typeNames.split(",")
                if (includes.any { it !in types }) throw Error.InvalidDefinition
                val globs = includes.flatMap { types.getValue(it).globs() }
                for (glob in globs) {
                    add(name, glob)
                }
            }
            else -> throw Error.InvalidDefinition
        }
        return this
    }

    /** Add the default file type definitions. */
    fun addDefaults(): TypesBuilder {
        for ((names, exts) in DEFAULT_TYPES) {
            for (name in names) {
                for (ext in exts) {
                    add(name, ext)
                }
            }
        }
        return this
    }
}

private fun validateTypeName(name: String) {
    if (name == "all" || name.any { !it.isLetterOrDigit() }) {
        throw Error.InvalidDefinition
    }
}

private fun globMatchesName(glob: String, name: String): Boolean {
    val alternatives = expandBraceAlternatives(glob)
    return alternatives.any { alt -> Regex("^${globToTypeRegex(alt)}$").matches(name) }
}

private fun expandBraceAlternatives(glob: String): List<String> {
    val open = glob.indexOf('{')
    if (open < 0) return listOf(glob)
    val close = glob.indexOf('}', startIndex = open + 1)
    if (close < 0) return listOf(glob)
    val prefix = glob.substring(0, open)
    val suffix = glob.substring(close + 1)
    return glob.substring(open + 1, close)
        .split(",")
        .flatMap { option -> expandBraceAlternatives(prefix + option + suffix) }
}

private fun globToTypeRegex(glob: String): String {
    val out = StringBuilder()
    for (c in glob) {
        when (c) {
            '*' -> out.append(".*")
            '?' -> out.append('.')
            else -> out.append(Regex.escape(c.toString()))
        }
    }
    return out.toString()
}
