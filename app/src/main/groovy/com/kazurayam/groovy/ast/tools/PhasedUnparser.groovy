package com.kazurayam.groovy.ast.tools

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
        sb.append("## ${leftPhase.getPhaseNumber()}_${leftPhase.toString()} vs ${rightPhase.getPhaseNumber()}_${rightPhase.toString()}\n\n")

        DiffRowGenerator generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .inlineDiffByWord(true)
                //.oldTag(f -> "~")
                //.newTag(f -> "**")
                .build()
        List<String> leftLines = toLines(phases.get(leftPhase))
        List<String> rightLines = toLines(phases.get(rightPhase))
        List<DiffRow> rows = generator.generateDiffRows(leftLines, rightLines)
        sb.append("|${leftPhase.getPhaseNumber()} ${leftPhase.toString()}|${rightPhase.getPhaseNumber()} ${rightPhase.toString()}|\n")
        sb.append("|----------|----------|\n")
        for (DiffRow row : rows) {
            sb.append("|${row.getOldLine()}|${row.getNewLine()}|\n")
        }
        sb.append("\n\n")
    }

    private static List<String> toLines(String text) {
        StringReader sr = new StringReader(text)
        BufferedReader br = new BufferedReader(sr)
        return br.readLines()
    }
}
