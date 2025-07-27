package groovy.console.ui

import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilePhase

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AstNodeToScriptAdapterVariationsTest {

    private static String sourceCode = '''
package groovyinaction.ch09ast
import groovy.transform.Immutable
import groovy.transform.ToString
@Immutable
@ToString(includePackage=false)
class Genius {
    String firstName, lastName
}
'''
    private AstNodeToScriptAdapter adapter

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
