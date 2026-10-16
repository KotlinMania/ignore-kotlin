# ignore-kotlin

[![GitHub link](https://img.shields.io/badge/GitHub-KotlinMania%2Fignore--kotlin-blue.svg)](https://github.com/KotlinMania/ignore-kotlin)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.kotlinmania/ignore-kotlin)](https://central.sonatype.com/artifact/io.github.kotlinmania/ignore-kotlin)
[![Build status](https://img.shields.io/github/actions/workflow/status/KotlinMania/ignore-kotlin/ci.yml?branch=main)](https://github.com/KotlinMania/ignore-kotlin/actions)

This is a Kotlin Multiplatform line-by-line transliteration port of the Rust crate [`ignore`](https://github.com/BurntSushi/ripgrep/tree/master/crates/ignore) from [`BurntSushi/ripgrep`](https://github.com/BurntSushi/ripgrep).

The `ignore` crate provides a fast recursive directory iterator that respects various filters such as globs, file types, and `.gitignore` files. This library also provides lower-level direct access to gitignore, override, and file type matchers.

**Original Project:** Based on [`BurntSushi/ripgrep/crates/ignore`](https://github.com/BurntSushi/ripgrep/tree/master/crates/ignore) (upstream crate: [`ignore`](https://crates.io/crates/ignore)). All design credit belongs to Andrew Gallant and the ripgrep authors.

---

## Installation

Add to your `build.gradle.kts`:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.kotlinmania:ignore-kotlin:0.1.0")
        }
    }
}
```

---

## Usage Examples

### Basic Directory Walking

Recursively traverse the directory while automatically filtering out files and directories according to ignore rules found in `.ignore`, `.gitignore`, and `.git/info/exclude`:

```kotlin
import io.github.kotlinmania.ignore.Walk

for (result in Walk.new("./")) {
    if (result.isSuccess()) {
        println(result.getOrNull()?.path())
    } else {
        println("ERROR: ${result.error()}")
    }
}
```

### Advanced Walking with `WalkBuilder`

By default, the recursive iterator ignores hidden files and directories. This can be configured using `WalkBuilder`:

```kotlin
import io.github.kotlinmania.ignore.WalkBuilder

val walk =
    WalkBuilder("./")
        .hidden(false)
        .maxDepth(3)
        .build()

for (result in walk) {
    result.getOrNull()?.let { entry ->
        println("${entry.path()} (depth ${entry.depth()})")
    }
}
```

### Gitignore Matching

Direct access to gitignore matching:

```kotlin
import io.github.kotlinmania.ignore.GitignoreBuilder

val gitignore =
    GitignoreBuilder("/path/to/root")
        .addLine(null, "*.log")
        .addLine(null, "!important.log")
        .build()

val match = gitignore.matched("app.log", isDir = false)
if (match.isIgnore()) {
    println("File is ignored")
}
```

---

## Supported Targets

- **macOS** (`macosArm64`)
- **iOS / tvOS / watchOS** (`iosArm64`, `iosSimulatorArm64`, `tvosArm64`, `tvosSimulatorArm64`, `watchosDeviceArm64`, `watchosSimulatorArm64`, Swift Export + XCFramework)
- **Linux** (`linuxArm64`, `linuxX64`)
- **Windows** (`mingwX64`)
- **Android** (API 24+)
- **JVM** (Java 11+)
- **JavaScript** (Node.js & browser)
- **WebAssembly** (`wasmJs`, `wasmWasi`)

---

## License

Dual-licensed under MIT or UNLICENSE, matching upstream [`ignore`](https://github.com/BurntSushi/ripgrep/tree/master/crates/ignore).
Original work Copyright (c) Andrew Gallant (BurntSushi).
Kotlin port Copyright (c) 2026 Sydney Renee and The Solace Project.
