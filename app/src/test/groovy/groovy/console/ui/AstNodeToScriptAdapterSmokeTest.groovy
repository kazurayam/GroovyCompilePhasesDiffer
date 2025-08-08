package groovy.console.ui

import com.kazurayam.unittest.DeleteDir
import com.kazurayam.unittest.TestOutputOrganizer
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.CompilerConfiguration
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.jupiter.api.Assertions.*

class AstNodeToScriptAdapterSmokeTest {

    private static final TestOutputOrganizer too =
            new TestOutputOrganizer.Builder(AstNodeToScriptAdapterSmokeTest.class)
                    .outputDirectoryRelativeToProject("build/tmp/testOutput")
                    .subOutputDirectory(AstNodeToScriptAdapter.class)
                    .build()
    private static String sourceCode
    private static Path classesDir
    private static Path geniusClassFile
    private CompilerConfiguration config
    private AstNodeToScriptAdapter adapter

    @BeforeAll
    static void beforeClass() {
        //input
        Path sourceFile = Paths.get('.').resolve('src/main/groovy/com/kazurayam/example/Genius.groovy')
        assertTrue(Files.exists(sourceFile))
        sourceCode = sourceFile.text
        //output
        Path outDir = too.cleanClassOutputDirectory()
        classesDir = outDir.resolve("classes")
        Files.createDirectories(classesDir)
        geniusClassFile = classesDir.resolve('com/kazurayam/example/Genius.class')
    }

    @BeforeEach
    void setup() {
        config = new CompilerConfiguration()
        config.setTargetDirectory(classesDir.toFile())
        //
        CompilationUnit cu = new CompilationUnit(config)
        cu.addSource('Genius.groovy', sourceCode)
        cu.compile()
        adapter = new AstNodeToScriptAdapter()
        //
        deleteGeniusClassFile()
    }

    private static void deleteGeniusClassFile() {
        if (Files.exists(geniusClassFile)) {
            DeleteDir.deleteDirectoryRecursively(classesDir)
        }
    }

    @Test
    void test_CompilePhase_CONVERSION() {
        assertFalse(Files.exists(geniusClassFile), "the Genius class file should not be present")
        String unparsed = adapter.compileToScript(sourceCode,
                CompilePhase.CONVERSION.getPhaseNumber(),
                null, false, false, config)
        assertNotNull(unparsed)
        assertFalse(Files.exists(geniusClassFile), "the Genius class file should not be present")
    }

    @Test
    void test_CompilePhase_CLASS_GENERATION() {
        assertFalse(Files.exists(geniusClassFile), "the Genius class file should not be present")
        String unparsed = adapter.compileToScript(sourceCode,
                CompilePhase.CLASS_GENERATION.getPhaseNumber(),
                null, false, false, config)
        assertNotNull(unparsed)
        assertFalse(Files.exists(geniusClassFile), "the Genius class file should not be present")
    }

    /**
     * See https://github.com/kazurayam/GroovyCompilePhasesDiffer/issues/1 ,
     * issues/4 and issues/5.
     * Running the AstNodeToScriptAdapter.compileToScript with CompilePhase.OUTPUT
     * results a binary *.class file written into the specified directory.
     * If I want to avoid that file, I should refrain from running the method
     * with CompilePhase.OUTPUT.
     */
    @Test
    void issue1_4_5() {
        assertFalse(Files.exists(geniusClassFile), "the Genius class file should not be present")
        String unparsed = adapter.compileToScript(sourceCode,
                CompilePhase.OUTPUT.getPhaseNumber(),
                null, false, false, config)
        assertNotNull(unparsed)
        // the Genius class file will be created when CompilePhase.OUTPUT is specified
        assertTrue(Files.exists(geniusClassFile), "the Genius class file should be present")
    }
}
