import Testing
import Ignore

@Suite("Ignore Export Smoke Tests")
struct IgnoreExportTests {
    @Test("Swift module loads cleanly")
    func testSwiftModuleLoads() throws {
        #expect(Bool(true))
    }

    @Test("GitignoreBuilder and WalkBuilder can be constructed and operated")
    func testBuildersCanBeConstructed() throws {
        let builder = GitignoreBuilder(path: "/test")
        _ = builder.addLine(from: nil, line: "*.log")
        let gitignore = builder.build()
        #expect(!gitignore.isEmpty())
        #expect(gitignore.path() == "/test")

        let walkBuilder = WalkBuilder(path: ".")
        _ = walkBuilder.maxDepth(depth: 3)
        let walk = walkBuilder.build()
        _ = walk
    }
}
