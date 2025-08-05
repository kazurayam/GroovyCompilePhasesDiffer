package com.kazurayam.groovy

import com.kazurayam.unittest.DeleteDir
import com.kazurayam.unittest.TestOutputOrganizer
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Stream

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue

class CompilePhasesDifferTest {

    private static final Logger log = LoggerFactory.getLogger(CompilePhasesDifferTest)
    private static final TestOutputOrganizer too =
            new TestOutputOrganizer.Builder(CompilePhasesDifferTest.class)
                    .outputDirectoryRelativeToProject("build/tmp/testOutput")
                    .subOutputDirectory(CompilePhasesDifferTest.class)
                    .build();
    private static Path outDir

    @BeforeAll
    static void beforeAll() {
        outDir = too.cleanClassOutputDirectory()
    }

    @Test
    void testReport() {
        String identifier = "com/kazurayam/example/Genius.groovy"
        Path sourceDir = Paths.get("./src/main/groovy")
        String sourceCode = sourceDir.resolve(identifier).text
        //
        Path report = CompilePhasesDiffer.report(identifier, sourceCode, outDir)
        assertTrue(Files.exists(report))
        assertTrue(report.getFileName().toString().startsWith('com_kazurayam_example_Genius.groovy'))
        assertTrue(report.getFileName().toString().endsWith('-CompilePhasesDiff.md'))
        try (Stream<Path> files = Files.list(outDir)) {
            // outDir should contain 9 .groovy files and 1 .md file
            assertEquals(10, files.count())
        }
    }
}
