package com.kazurayam.groovy


import org.junit.jupiter.api.Test

import java.nio.file.Path
import java.nio.file.Paths

class CompilePhasesDifferTest {

    @Test
    void testSmoke() {
        Path fixture = Paths.get("./src/test/fixture/example/Genius.groovy")
        String name = "Genius"
        String sourceCode = fixture.text
        Path outDir = Paths.get("./build/tmp/testOutput/PhasedUnparserTest")
        CompilePhasesDiffer.unparse(name, sourceCode, outDir)
    }
}
