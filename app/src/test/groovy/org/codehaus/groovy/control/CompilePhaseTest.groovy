package org.codehaus.groovy.control

import org.junit.jupiter.api.Test

class CompilePhaseTest {

    @Test
    void test_values() {
        System.out.println("Values of the Enum `org.codehaus.groovy.control.CompilePhase`")
        System.out.println("")
        System.out.println("|name|value|")
        System.out.println("|----|-----|")
        for (CompilePhase cp : CompilePhase.values()) {
            System.out.println(String.format("|`%s`|%d|", cp, cp.getPhaseNumber()))
            /*
            INITIALIZATION:1
            PARSING:2
            CONVERSION:3
            SEMANTIC_ANALYSIS:4
            CANONICALIZATION:5
            INSTRUCTION_SELECTION:6
            CLASS_GENERATION:7
            OUTPUT:8
            FINALIZATION:9
             */
        }

    }
}
