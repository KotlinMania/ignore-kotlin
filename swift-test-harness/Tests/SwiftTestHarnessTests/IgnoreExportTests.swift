import XCTest
import Ignore

final class IgnoreExportTests: XCTestCase {
    private var repositoryRoot: URL {
        var url = URL(fileURLWithPath: #filePath)
        for _ in 0..<4 {
            url.deleteLastPathComponent()
        }
        return url
    }

    private func generatedText(_ components: String...) throws -> String {
        var url = repositoryRoot
        for component in components {
            url.appendPathComponent(component)
        }
        return try String(contentsOf: url, encoding: .utf8)
    }

    func testSwiftModuleLoads() throws {
        XCTAssertTrue(true, "Ignore swift module imported cleanly")
    }

    func testGeneratedBridgeHidesSwiftHostileSurfaces() throws {
        let bridge = try generatedText(
            "build",
            "SwiftExport",
            "macosArm64",
            "Debug",
            "files",
            "Ignore",
            "Ignore.kt"
        )
        let swift = try generatedText(
            "build",
            "SPMPackage",
            "macosArm64",
            "Debug",
            "Sources",
            "Ignore",
            "Ignore.swift"
        )

        let forbiddenBridgeFragments = [
            "as kotlin.Function",
            "<kotlin.Any?>",
            "kotlin.Exception",
            "kotlin.Throwable",
            "MutableList<kotlin.Any",
            "MutableMap<",
            "MutableSet<kotlin.Any",
        ]
        for fragment in forbiddenBridgeFragments {
            XCTAssertFalse(bridge.contains(fragment), "Swift bridge exposed \(fragment)")
        }

        XCTAssertFalse(swift.contains("Error"), "Swift module should not export Kotlin Error")
        XCTAssertFalse(swift.contains("Match"), "Swift module should not export generic Match")
    }
}
