package com.kazurayam.groovy.ast.tools

import groovy.console.ui.AstNodeToScriptAdapter
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilePhase

import java.nio.file.Files
import java.nio.file.Path

class PhasedUnparser {

    static void unparse(String name, String sourceCode, Path outDir) {
        Objects.requireNonNull(name)
        Objects.requireNonNull(sourceCode)
        Objects.requireNonNull(outDir)

        CompilationUnit cu = new CompilationUnit()
        cu.addSource(name, sourceCode)
        cu.compile()

        Files.createDirectories(outDir)

        AstNodeToScriptAdapter adapter = new AstNodeToScriptAdapter()

        for (CompilePhase phase : CompilePhase.values()) {
            String unparseResult = adapter.compileToScript(sourceCode, phase.getPhaseNumber())
            String fileName = "${name}_${phase.getPhaseNumber()}_${phase.toString()}.groovy"
            Path outFile = outDir.resolve(fileName)
            outFile.text = unparseResult
        }
    }

}
