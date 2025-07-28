package com.kazurayam.groovy

import org.junit.jupiter.api.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class CompilePhasesDifferTest {

    @Test
    void testReport() {
        Path fixturesDir = Paths.get("./src/test/fixtures")
        String name = "example/Genius.groovy"
        String sourceCode = fixturesDir.resolve(name).text
        Path outDir = Paths.get("./build/tmp/testOutput/CompilePhasesDifferTest")
        Path report = CompilePhasesDiffer.report(name, sourceCode, outDir)
        assert Files.exists(report)
        assert report.getFileName().toString().startsWith("example_Genius.groovy")
    }
}
