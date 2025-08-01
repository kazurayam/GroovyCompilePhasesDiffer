package com.kazurayam.groovy

import com.github.difflib.DiffUtils
import com.github.difflib.UnifiedDiffUtils
import com.github.difflib.patch.Patch
import groovy.console.ui.AstNodeToScriptAdapter
import groovy.transform.Immutable
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilePhase

import java.nio.file.Files
import java.nio.file.Path

/**
 *
 */
class CompilePhasesDiffer {

    /**
     *
     * @param groovySource
     * @param identifier
     * @param outDir
     * @return
     */
    static Path report(String groovySource, String identifier, Path outDir) {
        // the params should not be null
        Objects.requireNonNull(groovySource)
        Objects.requireNonNull(identifier)
        Objects.requireNonNull(outDir)

        // replace non-file-path-comprising-characters in the identifier
        String escapedId = escape(identifier)

        // ensure the output directory
        Files.createDirectories(outDir)

        // instantiate the CompilationUnit object
        CompilationUnit cu = new CompilationUnit()
        cu.addSource(escapedId, groovySource)
        cu.compile()

        // AstNodeToScriptAdapter enables us to unparse the Abstract Syntax Tree
        AstNodeToScriptAdapter adapter = new AstNodeToScriptAdapter()

        // iterate over all entries of CompilePhases
        Map<CompilePhase, UnparseEntity> phases = new HashMap<>()
        for (CompilePhase phase : CompilePhase.values()) {
            // unparse the AST
            String rebuiltSource = adapter.compileToScript(groovySource, phase.getPhaseNumber())
            // memorize the rebuilt Groovy source
            UnparseEntity ue = new UnparseEntity(identifier: escapedId, source: rebuiltSource)
            phases.put(phase, ue)
            // save the rebuilt Groovy source of each CompilePhase into a file in the output directory
            Path outFile = outDir.resolve(
                    "${escapedId}-${phase.getPhaseNumber()}_${phase.toString()}.groovy")
            outFile.text = rebuiltSource
        }

        // generate the report
        StringBuilder sb = new StringBuilder()
        sb.append("# How Groovy compiler transforms a source\n\n")
        sb.append("Groovy Compiler transforms the Abstract Syntax Tree of \"${identifier}\" gradually." +
                " This report shows how the AST Transformation progresses by looking at the diffs each CompilePhase.\n\n")
        // generate sections that present the diff of ASTs
        reportSubsection(sb, phases, CompilePhase.INITIALIZATION, CompilePhase.PARSING)
        reportSubsection(sb, phases, CompilePhase.PARSING, CompilePhase.CONVERSION)
        reportSubsection(sb, phases, CompilePhase.CONVERSION, CompilePhase.SEMANTIC_ANALYSIS)
        reportSubsection(sb, phases, CompilePhase.SEMANTIC_ANALYSIS, CompilePhase.CANONICALIZATION)
        reportSubsection(sb, phases, CompilePhase.CANONICALIZATION, CompilePhase.INSTRUCTION_SELECTION)
        reportSubsection(sb, phases, CompilePhase.INSTRUCTION_SELECTION, CompilePhase.CLASS_GENERATION)
        reportSubsection(sb, phases, CompilePhase.CLASS_GENERATION, CompilePhase.OUTPUT)
        reportSubsection(sb, phases, CompilePhase.OUTPUT, CompilePhase.FINALIZATION)
        // write the report
        Path report = outDir.resolve("${escapedId}-CompilePhasesDiff.md")
        report.text = sb.toString()
        return report
    }

    /**
     * Escape characters in the str that should not be used in a file path string
     * into safe characters
     * @param str
     * @return escaped string
     */
    private static String escape(String str) {
        return str
                .replaceAll("\\\\", '_')
                .replaceAll("/", '_')
                .replaceAll(":", '')
                .replaceAll("\\*", '')
                .replaceAll("\\?", '')
                .replaceAll('"', '')
                .replaceAll("<", '')
                .replaceAll(">", '')
                .replaceAll("\\|", '')
    }

    /**
     * compile a Markdown subsection
     */
    private static void reportSubsection(StringBuilder sb,
                                      Map<CompilePhase, UnparseEntity> phases,
                                      CompilePhase leftPhase,
                                      CompilePhase rightPhase) {
        sb.append("## ${leftPhase.getPhaseNumber()}_${leftPhase.toString()}" +
                " vs ${rightPhase.getPhaseNumber()}_${rightPhase.toString()}\n\n")
        sb.append("```\n")

        // generate Unified Diff
        List<String> leftLines = toLines(phases.get(leftPhase).getSource())
        String leftName = "${phases.get(leftPhase).getIdentifier()}-${leftPhase.getPhaseNumber()}_${leftPhase.toString()}.groovy"
        List<String> rightLines = toLines(phases.get(rightPhase).getSource())
        String rightName = "${phases.get(leftPhase).getIdentifier()}-${rightPhase.getPhaseNumber()}_${rightPhase.toString()}.groovy"

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

    @Immutable
    static class UnparseEntity {
        String identifier, source
    }
}
