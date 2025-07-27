package groovy.console.ui

import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilePhase
import org.junit.jupiter.api.Test

class AstNodeToScriptAdapterSimpleTest {
    def sourceCode = '''
class HelloWorld {
    static void main(String[] args) {
        println "Hello, Groovy!"
    }
}
'''

    @Test
    void testSmoke() {
        CompilationUnit cu = new CompilationUnit()
        cu.addSource('HelloWorld.groovy', sourceCode)
        cu.compile()

        List<ClassNode> classes = cu.getAST().getClasses()
        def adapter = new AstNodeToScriptAdapter()

        classes.each {ClassNode classNode ->
            def reconstructed = adapter.compileToScript(sourceCode, CompilePhase.CONVERSION.phaseNumber)
            println reconstructed
        }
    }

}