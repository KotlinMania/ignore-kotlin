#if canImport(Testing)
import Foundation
import Testing
import Ignore

@Suite("Ignore Swift Export Tests")
struct IgnoreExportTests {
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

    @Test("Swift module loads")
    func testSwiftModuleLoads() {
        #expect(Bool(true), "Ignore swift module imported cleanly")
    }

    @Test("Generated bridge hides Swift-hostile surfaces")
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
            #expect(!bridge.contains(fragment), "Swift bridge exposed \(fragment)")
        }

        #expect(!swift.contains("Error"), "Swift module should not export Kotlin Error")
        #expect(!swift.contains("Match"), "Swift module should not export generic Match")
    }
}
#elseif canImport(XCTest)
import Foundation
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
#endif
