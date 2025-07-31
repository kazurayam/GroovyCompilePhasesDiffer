package com.kazurayam.groovy

import org.junit.jupiter.api.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Stream

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue

class CompilePhasesDifferTest {

    @Test
    void testReport() {
        String identifier = "org/example/Genius.groovy"
        Path sourceDir = Paths.get("./src/main/groovy")
        String sourceCode = sourceDir.resolve(identifier).text
        Path outDir = Paths.get("./build/tmp/testOutput/CompilePhasesDifferTest")
        //
        Path report = CompilePhasesDiffer.report(identifier, sourceCode, outDir)
        assertTrue(Files.exists(report))
        assertTrue(report.getFileName().toString().startsWith('org_example_Genius.groovy'))
        assertTrue(report.getFileName().toString().endsWith('-CompilePhasesDiff.md'))
        try (Stream<Path> files = Files.list(outDir)) {
            // outDir should contain 9 .groovy files and 1 .md file
            assertEquals(10, files.count())
        }
    }
}
