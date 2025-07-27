package com.kazurayam.groovy.ast.tools

import com.github.difflib.DiffUtils
import com.github.difflib.UnifiedDiffUtils
import com.github.difflib.patch.Patch
import com.github.difflib.text.DiffRow
import com.github.difflib.text.DiffRowGenerator
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

        Map<CompilePhase, String> phases = new HashMap<>()

        for (CompilePhase phase : CompilePhase.values()) {
            String unparseResult = adapter.compileToScript(sourceCode, phase.getPhaseNumber())
            phases.put(phase, unparseResult)
            String fileName = "${name}_${phase.getPhaseNumber()}_${phase.toString()}.groovy"
            Path outFile = outDir.resolve(fileName)
            outFile.text = unparseResult
        }

        StringBuilder sb = new StringBuilder()
        sb.append("# Compilation progress\n")
        sb.append("- name: ${name}\n\n")

        reportSection(sb, phases, CompilePhase.INITIALIZATION, CompilePhase.PARSING)
        reportSection(sb, phases, CompilePhase.PARSING, CompilePhase.CONVERSION)
        reportSection(sb, phases, CompilePhase.CONVERSION, CompilePhase.SEMANTIC_ANALYSIS)
        reportSection(sb, phases, CompilePhase.SEMANTIC_ANALYSIS, CompilePhase.CANONICALIZATION)
        reportSection(sb, phases, CompilePhase.CANONICALIZATION, CompilePhase.INSTRUCTION_SELECTION)
        reportSection(sb, phases, CompilePhase.INSTRUCTION_SELECTION, CompilePhase.CLASS_GENERATION)
        reportSection(sb, phases, CompilePhase.CLASS_GENERATION, CompilePhase.OUTPUT)
        reportSection(sb, phases, CompilePhase.OUTPUT, CompilePhase.FINALIZATION)

        Path report = outDir.resolve("progress.md")
        report.text = sb.toString()
    }

    /**
     *
     */
    private static void reportSection(StringBuilder sb, Map<CompilePhase, String> phases,
                                CompilePhase leftPhase, CompilePhase rightPhase) {
        sb.append("## ${leftPhase.getPhaseNumber()}_${leftPhase.toString()}" +
                " vs ${rightPhase.getPhaseNumber()}_${rightPhase.toString()}\n\n")
        sb.append("```\n")

        // generate Unified Diff
        List<String> leftLines = toLines(phases.get(leftPhase))
        String leftName = "${leftPhase.getPhaseNumber()} ${leftPhase.toString()}"
        List<String> rightLines = toLines(phases.get(rightPhase))
        String rightName = "${rightPhase.getPhaseNumber()} ${rightPhase.toString()}"
        List<String> unifiedDiff = generateUnifiedDiff(leftLines, leftName, rightLines, rightName)
        for (String line : unifiedDiff) {
            sb.append("${line}\n")
        }

        sb.append("```\n")
        sb.append("\n\n")
    }

    private static List<String> toLines(String text) {
        StringReader sr = new StringReader(text)
        BufferedReader br = new BufferedReader(sr)
        return br.readLines()
    }

    private static List<String> generateUnifiedDiff(
            List<String> leftLines, String leftName,
            List<String> rightLines, String rightName) {
        Patch<String> patch = DiffUtils.diff(leftLines, rightLines)
        return UnifiedDiffUtils.generateUnifiedDiff(leftName, rightName, leftLines, patch, 3)
    }

}
