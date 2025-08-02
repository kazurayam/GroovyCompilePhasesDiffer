# Groovy Compile Phases Differ

This is a memorandum by @kazurayam just for his own sake.

## How Groovy Compiler processes the @Immutable annotation

One day, I wanted to learn something about Abstract Syntax Tree Transformation performed by Groovy Compiler.

I read a book [Groovy in Action, Second edition, Chapter 9, Compile-time metaprogramming and AST transformations](https://livebook.manning.com/book/groovy-in-action-second-edition/chapter-9#96)

### Sample class with `@Immutable` annotation

The book presented a sample Groovy class `Genius` with `@Immutable` annotation.

    package org.example

    import groovy.transform.Immutable
    import groovy.transform.ToString

    /**
     * Quoted from a book "Groovy In Action, second edition" published by Manning
     *
     * https://www.groovy-lang.org/metaprogramming.html#_compile_time_metaprogramming
     */
    @Immutable
    @ToString(includePackage=false)
    class Genius {
        String firstName, lastName
    }

### JUnit5 test for the `Genius` class

    package org.example

    import org.junit.jupiter.api.Test
    import static org.junit.jupiter.api.Assertions.assertEquals
    import static org.junit.jupiter.api.Assertions.assertNotNull

    class GeniusTest {

        @Test
        void testMapConstructor() {
            Genius instance = new Genius(firstName: 'Albert', lastName: 'Einstein')
            assertNotNull(instance)
        }

        @Test
        void testTupleStyleConstructor() {
            Genius instance = new Genius('Albert', 'Einstein')
            assertNotNull(instance)
        }

        @Test
        void testGetters() {
            Genius instance = new Genius('Albert', 'Einstein')
            assertEquals('Albert', instance.getFirstName())
            assertEquals('Einstein', instance.getLastName())
        }

        @Test
        void testToString() {
            Genius instance = new Genius('Albert', 'Einstein')
            assertEquals('Genius(Albert, Einstein)',
                    instance.toString())
        }
    }

This test passed. This test proves that:

1.  `@Immutable` annotation resulted a Map constructor for the `Genuin` class; like `new Geniums(firstName:'Albert',lastName:'Einstein')`

2.  `@Immutable` annotation resulted a Tuple-style constructor; like `new Genius('Albert','Einsten')`

3.  `@Immutable` annotation resulted Getter methods, like `instance.getFirstName()`

4.  `@Immutable` annotation resulted `toString` method, like `instance.toString()`

I could see the description in the book is right. No surprise. But I couldn’t be confident that I understood it firmly.

I wanted to see how the final `class` file looks like. I wanted to read it! But I know the `class` file is a binary file, not a text. So, I wanted to **unparse** the `class` into a text which explicitly declares all the methods and properties of the `Genuin` class.

How can I **unparse** the binary `class` file into a text?

## AstNodeToScriptAdapter

One day, I (kazurayam) asked a question to Copilot (AI agent):

&gt;In Groovy, I want to unparse an AST into source. How can I do it?

Copilot replied as follows:

&gt;✨ If you’re working with Groovy’s abstract syntax tree (AST) and want to turn it back into source code (a process often called “unparsing”), you’ve got a few tools at your disposal—though Groovy doesn’t have a built-in one-size-fits-all solution for this.

&gt;Here’s how you can go about it:

&gt;🛠 Option 1: Use Groovy’s AstNodeToScriptAdapter
Groovy includes an adapter class that converts AST nodes back into Groovy source code. It’s found in the org.codehaus.groovy.tools.ast package.

groovy

    import org.codehaus.groovy.ast.ClassNode
    import org.codehaus.groovy.control.CompilationUnit
    import org.codehaus.groovy.tools.ast.AstNodeToScriptAdapter

    def sourceCode = '''
    class HelloWorld {
    static void main(String[] args) {
    println "Hello, Groovy!"
    }
    }
    '''

    def cu = new CompilationUnit()
    cu.addSource('script.groovy', sourceCode)
    cu.compile()

    def classes = cu.ast.getClasses()
    def adapter = new AstNodeToScriptAdapter()

    classes.each { ClassNode classNode ->
    def reconstructed = adapter.print(classNode)
    println reconstructed
    }

&gt;🧩 Notes
You’ll need access to the Groovy internals, so make sure the relevant modules are available in your classpath (like groovy-ant or groovy-tools depending on your version).

&gt;The adapter won’t always preserve original formatting or comments, but it does a solid job reconstructing syntactically correct Groovy code.

I asked one more question:

&gt;What use of groovy.console.ui.AstNodeToScriptAdapter class

Copilot replied as follows:

&gt;The groovy.console.ui.AstNodeToScriptAdapter class is a utility in the Groovy Console that serves a pretty neat purpose: it compiles Groovy source code to a specific phase of the compiler, then decompiles it back into readable Groovy source2.

&gt;Here’s how it’s typically used:

&gt;🧠 Primary Purpose

&gt;- AST Visualization: It powers the AST (Abstract Syntax Tree) Browser in the Groovy Console, helping developers inspect how Groovy transforms code during compilation.

&gt;- Debugging & Learning Tool: Useful for understanding AST transformations, especially when writing custom AST transforms.

&gt;🔧 Key Method
groovy

    String compileToScript(String script, int compilePhase, ClassLoader classLoader, boolean showScriptFreeForm, boolean showScriptClass, CompilerConfiguration config)

This method:

&gt;- Takes Groovy source code as input

&gt;- Compiles it to a specified phase (e.g., SEMANTIC\_ANALYSIS, INSTRUCTION\_SELECTION)

&gt;- Converts the AST back into Groovy-like source code for inspection

&gt;🖥️ Command-Line Use
You can also run it from the command line to compile a Groovy file and print the resulting source:

bash

    java groovy.console.ui.AstNodeToScriptAdapter [filename] [compilePhase]

&gt;🧪 Example Use Case
Say you’re using the @Immutable annotation and want to see what boilerplate code Groovy generates—this class lets you peek under the hood and see the transformed code.

I wanted to see how the AstNodeToScriptAdapter works given with a single source code with `@Immutable` annotated and with different values of Compilation Phases. So I made the `app/src/test/groovy/groovy/console/ui/AstNodeToScriptAdapterVariationsTest`.
== CompilePhase

Values of the Enum `org.codehaus.groovy.control.CompilePhase`

|name|value|
|----|-----|
|`INITIALIZATION`|1|
|`PARSING`|2|
|`CONVERSION`|3|
|`SEMANTIC_ANALYSIS`|4|
|`CANONICALIZATION`|5|
|`INSTRUCTION_SELECTION`|6|
|`CLASS_GENERATION`|7|
|`OUTPUT`|8|
|`FINALIZATION`|9|
== What is CompilePhasesDiffer class

The CompilePhasesDiffer accepts a \*.groovy source.
It drives Groovy Compiler to generate Abstract Syntax Tree (AST) for all CompilePhases and
decompiles the AST into human-readable \*.groovy source files.
It also generates a "CompilePhasesDiff" report where you can see the unified-diffs of
the decompiled ASTs. With this report, you can see how Groovy Compiler process a \*.groovy
source file to get a \*.class file. You can see the progress with human-readable diff of ASTs.
