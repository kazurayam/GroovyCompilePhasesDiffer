package groovy.console.ui

import com.kazurayam.unittest.DeleteDir
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilePhase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.jupiter.api.Assertions.*

class AstNodeToScriptAdapterSmokeTest {

    private static String sourceCode
    private AstNodeToScriptAdapter adapter

    private static Path UNWANTED_CLASS_FILE = Paths.get('com/kazurayam/example/Genius.class')

    @BeforeAll
    static void beforeClass() {
        Path sourceFile = Paths.get('.').resolve('src/main/groovy/com/kazurayam/example/Genius.groovy')
        assertTrue(Files.exists(sourceFile))
        sourceCode = sourceFile.text
    }

    @BeforeEach
    void setup() {
        CompilationUnit cu = new CompilationUnit()
        cu.addSource('Genius.groovy', sourceCode)
        cu.compile()
        adapter = new AstNodeToScriptAdapter()
        //
        deleteComDir()
    }

    @AfterEach
    void tearDown() {
        deleteComDir()
    }

    private static void deleteComDir() {
        if (Files.exists(UNWANTED_CLASS_FILE)) {
            Path comDir = Paths.get('com')
            DeleteDir.deleteDirectoryRecursively(comDir)
        }
    }

    @Test
    void test_CompilePhase_CONVERSION() {
        assertFalse(Files.exists(UNWANTED_CLASS_FILE), "the unwanted class file should not be present")
        String unparsed = adapter.compileToScript(sourceCode, CompilePhase.CONVERSION.getPhaseNumber())
        assertNotNull(unparsed)
        assertFalse(Files.exists(UNWANTED_CLASS_FILE), "the unwanted class file should not be present")
    }

    @Test
    void test_CompilePhase_CLASS_GENERATION() {
        assertFalse(Files.exists(UNWANTED_CLASS_FILE), "the unwanted class file should not be present")
        String unparsed = adapter.compileToScript(sourceCode, CompilePhase.CLASS_GENERATION.getPhaseNumber())
        assertNotNull(unparsed)
        assertFalse(Files.exists(UNWANTED_CLASS_FILE), "the unwanted class file should not be present")
    }

    /**
     * See https://github.com/kazurayam/GroovyCompilePhasesDiffer/issues/1
     * Running the AstNodeToScriptAdapter.compileToScript with CompilePhase.OUTPUT
     * results a unwanted class file written into the current working directory.
     * In other words, if I want to avoid that file,
     * I should refrain from running the method with CompilePhase.OUTPUT.
     */
    @Test
    void issue1() {
        assertFalse(Files.exists(UNWANTED_CLASS_FILE), "the unwanted class file should not be present")
        String unparsed = adapter.compileToScript(sourceCode, CompilePhase.OUTPUT.getPhaseNumber())
        assertNotNull(unparsed)
        // the unwanted class file will be created when CompilePhase.OUTPUT is specified
        assertTrue(Files.exists(UNWANTED_CLASS_FILE), "the unwanted class file should be present")
    }
}
