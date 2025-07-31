package groovy.console.ui


import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilePhase
import static org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class AstNodeToScriptAdapterSmokeTest {

    private static String sourceCode
    private AstNodeToScriptAdapter adapter

    @BeforeAll
    static void beforeClass() {
        Path sourceFile = Paths.get('.').resolve('src/main/groovy/org/example/Genius.groovy')
        assertTrue(Files.exists(sourceFile))
        sourceCode = sourceFile.text
    }

    @BeforeEach
    void setup() {
        CompilationUnit cu = new CompilationUnit()
        cu.addSource('Genius.groovy', sourceCode)
        cu.compile()
        adapter = new AstNodeToScriptAdapter()
    }

    @Test
    void test_CompilePhase_CONVERSION() {
        String unparsed = adapter.compileToScript(sourceCode, CompilePhase.CONVERSION.getPhaseNumber())
        println unparsed
    }

    @Test
    void test_CompilePhase_OUTPUT() {
        String unparsed = adapter.compileToScript(sourceCode, CompilePhase.OUTPUT.getPhaseNumber())
        println unparsed
    }
}
