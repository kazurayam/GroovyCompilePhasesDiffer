package com.kazurayam.groovy


import com.kazurayam.unittest.TestOutputOrganizer
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Stream

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue

class CompilePhasesDifferTest {

    private static final TestOutputOrganizer too =
            new TestOutputOrganizer.Builder(CompilePhasesDifferTest.class)
                    .outputDirectoryRelativeToProject("build/tmp/testOutput")
                    .subOutputDirectory(CompilePhasesDifferTest.class)
                    .build();
    private static Path classesDir
    private static Path reportDir

    @BeforeAll
    static void beforeAll() {
        Path outDir = too.cleanClassOutputDirectory()
        classesDir = outDir.resolve("classes")
        reportDir = outDir.resolve("report")
    }

    @Test
    void testReport() {
        String identifier = "com/kazurayam/example/Genius.groovy"
        Path sourceDir = Paths.get("./src/main/groovy")
        String sourceCode = sourceDir.resolve(identifier).text
        //
        Path report = CompilePhasesDiffer.report(identifier, sourceCode, classesDir, reportDir)
        assertTrue(Files.exists(report))
        assertTrue(report.getFileName().toString().startsWith('com_kazurayam_example_Genius.groovy'))
        assertTrue(report.getFileName().toString().endsWith('-CompilePhasesDiff.md'))
        try (Stream<Path> files = Files.list(reportDir)) {
            // the reportDir should contain 9 *.groovy files and 1 *.md file
            assertEquals(10, files.count())
        }
        try (Stream<Path> files = Files.list(classesDir)) {
            // the classesDir should contain a *.class file
            assertEquals(1, files.count())
        }
    }
}
