// port-lint: source default_types.rs
package io.github.kotlinmania.ignore

import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultTypesTest {
    @Test
    fun defaultTypesAreSorted() {
        val names = DEFAULT_TYPES.asSequence().map { (aliases, _) -> aliases[0] }.iterator()
        if (!names.hasNext()) {
            return
        }
        var previousName = names.next()
        for (name in names) {
            assertTrue(
                name > previousName,
                """"$name" should be sorted before "$previousName" in `DEFAULT_TYPES`""",
            )
            previousName = name
        }
    }
}
