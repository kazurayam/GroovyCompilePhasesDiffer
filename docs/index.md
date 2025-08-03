# Groovy Compile Phases Differ

This is a memorandum by @kazurayam just for his own sake.

## How Groovy Compiler processes the @Immutable annotation

One day, I wanted to learn something about Abstract Syntax Tree Transformation performed by Groovy Compiler. I read a book [Groovy in Action, Second edition, Chapter 9, Compile-time metaprogramming and AST transformations](https://livebook.manning.com/book/groovy-in-action-second-edition/chapter-9#96)

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
        void testEquals() {
            Genius a = new Genius('Albert', 'Einstein')
            Genius b = new Genius('Albert', 'Einstein')
            assertEquals(a, b)
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

The tests passed. The tests proved that:

1.  `@Immutable` annotation resulted a Map constructor for the `Genuin` class; like `new Geniums(firstName:'Albert',lastName:'Einstein')`

2.  `@Immutable` annotation resulted a Tuple-style constructor; like `new Genius('Albert','Einsten')`

3.  `@Immutable` annotation resulted `boolean equals(Genius a, Genius b)` method

4.  `@Immutable` annotation resulted Getter methods, like `instance.getFirstName()`

5.  `@Immutable` annotation resulted `toString` method, like `instance.toString()`

I could examine the description in the book is right. No surprise. But I was not fully convinced. The book talked much about **Abstract Syntax Tree**. Groovy Compiler parses a \*.groovy file into an AST object; transforms the AST object into another phase of AST until it ends up with a `.class` file. I wanted to see how the AST objects of every compile phases look like.

So, I wanted to **unparse** the AST objects into a human-readable source code in Groovy which explicitly declares all the methods and properties in the AST.

But, how can I **unparse** the AST object into a human-readable text?

## AstNodeToScriptAdapter

One day, I asked a question to Copilot:

> In Groovy, I want to unparse an AST into source. How can I do it?

Copilot replied as follows:

> ✨ If you’re working with Groovy’s abstract syntax tree (AST) and want to turn it back into source code (a process often called “unparsing”), you’ve got a few tools at your disposal—though Groovy doesn’t have a built-in one-size-fits-all solution for this.
>
> Here’s how you can go about it:
>
> 🛠 Option 1:
>
> Use Groovy’s AstNodeToScriptAdapter
> Groovy includes an adapter class that converts AST nodes back into Groovy source code. It’s found in the org.codehaus.groovy.tools.ast package.
>
>     import org.codehaus.groovy.ast.ClassNode
>     import org.codehaus.groovy.control.CompilationUnit
>     import org.codehaus.groovy.tools.ast.AstNodeToScriptAdapter
>
>     def sourceCode = '''
>     class HelloWorld {
>         static void main(String[] args) {
>             println "Hello, Groovy!"
>         }
>     }
>     '''
>
>     def cu = new CompilationUnit()
>     cu.addSource('script.groovy', sourceCode)
>     cu.compile()
>
>     def classes = cu.ast.getClasses()
>     def adapter = new AstNodeToScriptAdapter()
>
>     classes.each { ClassNode classNode ->
>         def reconstructed = adapter.print(classNode)
>         println reconstructed
>     }
>
> 🧩 Notes
>
> You’ll need access to the Groovy internals, so make sure the relevant modules are available in your classpath (like groovy-ant or groovy-tools depending on your version).
>
> The adapter won’t always preserve original formatting or comments, but it does a solid job reconstructing syntactically correct Groovy code.

I asked Copilot one more question:

> What use of groovy.console.ui.AstNodeToScriptAdapter class?

Copilot replied as follows:

> The groovy.console.ui.AstNodeToScriptAdapter class is a utility in the Groovy Console that serves a pretty neat purpose: it compiles Groovy source code to a specific phase of the compiler, then decompiles it back into readable Groovy source.
>
> Here’s how it’s typically used:
>
> 🧠 Primary Purpose
>
> -   AST Visualization: It powers the AST (Abstract Syntax Tree) Browser in the Groovy Console, helping developers inspect how Groovy transforms code during compilation.
>
> -   Debugging & Learning Tool: Useful for understanding AST transformations, especially when writing custom AST transforms.
>
> 🔧 Key Method
>
>     String compileToScript(String script, int compilePhase, ClassLoader classLoader, boolean showScriptFreeForm, boolean showScriptClass, CompilerConfiguration config)
>
> This method:
>
> -   Takes Groovy source code as input
>
> -   Compiles it to a specified phase (e.g., SEMANTIC\_ANALYSIS, INSTRUCTION\_SELECTION)
>
> -   Converts the AST back into Groovy-like source code for inspection
>
> 🖥️ Command-Line Use
> You can also run it from the command line to compile a Groovy file and print the resulting source:
>
> bash
>
>     java groovy.console.ui.AstNodeToScriptAdapter [filename] [compilePhase]`
>
> 🧪 Example Use Case
>
> Say you’re using the @Immutable annotation and want to see what boilerplate code Groovy generates—this class lets you peek under the hood and see the transformed code.

This conversation with Copilot was useful. I wanted to invent a utility that applies the `AstNodeToScriptAdapter` to a single source code with a `@Immutable` annotation for every values of Compilation Phases to unparse the transient AST objects into human-readable \*.groovy source codes.

So I developed the `com.kazurayam.groovy.CompilePhasesDiffer`.

## CompilePhase

See the javadoc of `groovy.console.ui.AstNodeToScriptAdapter` at [String compileToScript(String int, ClassLoader, boolean boolean, CompilerConfiguration](https://docs.groovy-lang.org/3.0.17/html/gapi//groovy/console/ui/AstNodeToScriptAdapter.html#compileToScript(java.lang.String,%20int,%20java.lang.ClassLoader,%20boolean,%20boolean,%20org.codehaus.groovy.control.CompilerConfiguration))

The second argument `int compilePhase` looked mysterious to mee. I checked the javadoc of `org.codehaus.groovy.control.CompilePhase` at [CompilePhase](https://docs.groovy-lang.org/3.0.17/html/gapi//org/codehaus/groovy/control/CompilePhase.html)

The following is the essence of the Enum `org.codehaus.groovy.control.CompilePhase`.

<table>
<colgroup>
<col style="width: 33%" />
<col style="width: 33%" />
<col style="width: 33%" />
</colgroup>
<thead>
<tr class="header">
<th style="text-align: left;">name</th>
<th style="text-align: left;">value</th>
<th style="text-align: left;">description</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td style="text-align: left;"><p><code>INITIALIZATION</code></p></td>
<td style="text-align: left;"><p>1</p></td>
<td style="text-align: left;"><p>source files are opened and environment configured</p></td>
</tr>
<tr class="even">
<td style="text-align: left;"><p><code>PARSING</code></p></td>
<td style="text-align: left;"><p>2</p></td>
<td style="text-align: left;"><p>the grammar is used to produce tree of tokens representing the source code</p></td>
</tr>
<tr class="odd">
<td style="text-align: left;"><p><code>CONVERSION</code></p></td>
<td style="text-align: left;"><p>3</p></td>
<td style="text-align: left;"><p>An abstract syntax tree (AST) is created from token trees</p></td>
</tr>
<tr class="even">
<td style="text-align: left;"><p><code>SEMANTIC_ANALYSIS</code></p></td>
<td style="text-align: left;"><p>4</p></td>
<td style="text-align: left;"><p>Performs consistency and validity checks that the grammar can’t check for, and resolves classes</p></td>
</tr>
<tr class="odd">
<td style="text-align: left;"><p><code>CANONICALIZATION</code></p></td>
<td style="text-align: left;"><p>5</p></td>
<td style="text-align: left;"><p>Complete building the AST</p></td>
</tr>
<tr class="even">
<td style="text-align: left;"><p><code>INSTRUCTION_SELECTION</code></p></td>
<td style="text-align: left;"><p>6</p></td>
<td style="text-align: left;"><p>nstruction set is chosen, for example java5 or pre java5</p></td>
</tr>
<tr class="odd">
<td style="text-align: left;"><p><code>CLASS_GENERATION</code></p></td>
<td style="text-align: left;"><p>7</p></td>
<td style="text-align: left;"><p>creates the binary output in memory</p></td>
</tr>
<tr class="even">
<td style="text-align: left;"><p><code>OUTPUT</code></p></td>
<td style="text-align: left;"><p>8</p></td>
<td style="text-align: left;"><p>write the binary output to the file system</p></td>
</tr>
<tr class="odd">
<td style="text-align: left;"><p><code>FINALIZATION</code></p></td>
<td style="text-align: left;"><p>9</p></td>
<td style="text-align: left;"><p>Perform any last cleanup</p></td>
</tr>
</tbody>
</table>

## What is CompilePhasesDiffer class

I have developed a tool class `com.kazurayam.groovy.CompilePhasesDiffer` in Groovy. I tested it with groovy v3.0.17. It should run on JDK 17 and newer.

You can get the jar which contains the `CompilePhasesDiffer` in the GitHub Releases page at

-   <https://github.com/kazurayam/GroovyCompilePhasesDiffer/releases>

The `CompilePhasesDiffer` class depends on the following external library:

-   [java-diff-utils v4.16](https://mvnrepository.com/artifact/io.github.java-diff-utils/java-diff-utils/4.16)

### Sample use

[src/test/groovy/com/kazurayam/groovy/CompilePhasesDifferTest.groovy](https://github.com/kazurayam/GroovyCompilePhasesDiffer/blob/master/app/src/test/groovy/com/kazurayam/groovy/CompilePhasesDifferTest.groovy)

    package com.kazurayam.groovy

    import com.kazurayam.unittest.DeleteDir
    import com.kazurayam.unittest.TestOutputOrganizer
    import org.junit.jupiter.api.BeforeAll
    import org.junit.jupiter.api.Test
    import org.slf4j.Logger
    import org.slf4j.LoggerFactory

    import java.nio.file.Files
    import java.nio.file.Path
    import java.nio.file.Paths
    import java.util.stream.Stream

    import static org.junit.jupiter.api.Assertions.assertEquals
    import static org.junit.jupiter.api.Assertions.assertTrue

    class CompilePhasesDifferTest {

        private static final Logger log = LoggerFactory.getLogger(CompilePhasesDifferTest)
        private static final TestOutputOrganizer too =
                new TestOutputOrganizer.Builder(CompilePhasesDifferTest.class)
                        .outputDirectoryRelativeToProject("build/tmp/testOutput")
                        .subOutputDirectory(CompilePhasesDifferTest.class)
                        .build();
        private static Path outDir

        @BeforeAll
        static void beforeAll() {
            outDir = too.cleanClassOutputDirectory()
            DeleteDir.deleteDirectoryRecursively(outDir)
            Files.createDirectories(outDir)
        }

        @Test
        void testReport() {
            String identifier = "org/example/Genius.groovy"
            Path sourceDir = Paths.get("./src/main/groovy")
            String sourceCode = sourceDir.resolve(identifier).text
            //
            Path report = CompilePhasesDiffer.report(identifier, sourceCode, outDir)
            assertTrue(Files.exists(report))
            assertTrue(report.getFileName().toString().startsWith('org_example_Genius.groovy'))
            assertTrue(report.getFileName().toString().endsWith('-CompilePhasesDiff.md'))
            try (Stream<Path> files = Files.list(outDir)) {
                // outDir should contain 9 .groovy files and 1 .md file
                assertEquals(10, files.count())
            }
        }
    }

When I ran this test, I got several files created in the outputDir specified.

    ~/github/GroovyCompilePhasesDiffer git:[master]
    tree app/build/tmp/testOutput/com.kazurayam.groovy.CompilePhasesDifferTest/
    app/build/tmp/testOutput/com.kazurayam.groovy.CompilePhasesDifferTest/
    ├── org_example_Genius.groovy-1_INITIALIZATION.groovy
    ├── org_example_Genius.groovy-2_PARSING.groovy
    ├── org_example_Genius.groovy-3_CONVERSION.groovy
    ├── org_example_Genius.groovy-4_SEMANTIC_ANALYSIS.groovy
    ├── org_example_Genius.groovy-5_CANONICALIZATION.groovy
    ├── org_example_Genius.groovy-6_INSTRUCTION_SELECTION.groovy
    ├── org_example_Genius.groovy-7_CLASS_GENERATION.groovy
    ├── org_example_Genius.groovy-8_OUTPUT.groovy
    ├── org_example_Genius.groovy-9_FINALIZATION.groovy
    └── org_example_Genius.groovy-CompilePhasesDiff.md

    1 directory, 10 files

The `org_example_Genius.groovy-3_CONVERSION.groovy` and other **.groovy files are the \*unparsed** AST of each CompilePhases. You can read the ASTs as text there.

The final file named\`\*-CompilePhasesDiff.md\` is most interesting.

### The output

    # How Groovy compiler transforms a source

    Groovy Compiler transforms the Abstract Syntax Tree of "org/example/Genius.groovy" gradually. This report shows how the AST Transformation progresses by looking at the diffs each CompilePhase.

    ## 1_INITIALIZATION vs 2_PARSING

    ```
    ```


    ## 2_PARSING vs 3_CONVERSION

    ```
    --- org_example_Genius.groovy-2_PARSING.groovy
    +++ org_example_Genius.groovy-3_CONVERSION.groovy
    @@ -1,0 +1,13 @@
    +package org.example
    +
    +import groovy.transform.Immutable as Immutable
    +import groovy.transform.ToString as ToString
    +
    +@Immutable
    +@ToString(includePackage = false)
    +public class org.example.Genius extends java.lang.Object { 
    +
    +    private String firstName 
    +    private String lastName 
    +
    +}
    ```


    ## 3_CONVERSION vs 4_SEMANTIC_ANALYSIS

    ```
    --- org_example_Genius.groovy-3_CONVERSION.groovy
    +++ org_example_Genius.groovy-4_SEMANTIC_ANALYSIS.groovy
    @@ -3,11 +3,17 @@
     import groovy.transform.Immutable as Immutable
     import groovy.transform.ToString as ToString
     
    -@Immutable
    -@ToString(includePackage = false)
    +@groovy.transform.ToString(includePackage = false, includeSuperProperties = true, cache = true)
    +@groovy.transform.EqualsAndHashCode(cache = true)
    +@groovy.transform.ImmutableBase
    +@groovy.transform.ImmutableOptions
    +@groovy.transform.PropertyOptions(propertyHandler = groovy.transform.options.ImmutablePropertyHandler)
    +@groovy.transform.TupleConstructor(defaults = false)
    +@groovy.transform.MapConstructor(noArg = true, includeSuperProperties = true, includeFields = true)
    +@groovy.transform.KnownImmutable
     public class org.example.Genius extends java.lang.Object { 
     
    -    private String firstName 
    -    private String lastName 
    +    private java.lang.String firstName 
    +    private java.lang.String lastName 
     
     }
    ```


    ## 4_SEMANTIC_ANALYSIS vs 5_CANONICALIZATION

    ```
    --- org_example_Genius.groovy-4_SEMANTIC_ANALYSIS.groovy
    +++ org_example_Genius.groovy-5_CANONICALIZATION.groovy
    @@ -11,9 +11,108 @@
     @groovy.transform.TupleConstructor(defaults = false)
     @groovy.transform.MapConstructor(noArg = true, includeSuperProperties = true, includeFields = true)
     @groovy.transform.KnownImmutable
    -public class org.example.Genius extends java.lang.Object { 
    +public final class org.example.Genius extends java.lang.Object { 
     
    -    private java.lang.String firstName 
    -    private java.lang.String lastName 
    +    private java.lang.String $to$string 
    +    private int $hash$code 
    +    private final java.lang.String firstName 
    +    private final java.lang.String lastName 
     
    +    @groovy.transform.Generated
    +    public org.example.Genius(java.lang.String firstName, java.lang.String lastName) {
    +        this .firstName = (( firstName ) as java.lang.String)
    +        this .lastName = (( lastName ) as java.lang.String)
    +    }
    +
    +    @groovy.transform.Generated
    +    public org.example.Genius(java.util.Map args) {
    +        if ( args == null) {
    +            args = [:]
    +        }
    +        org.codehaus.groovy.transform.ImmutableASTTransformation.checkPropNames(this, args)
    +        if (args.containsKey('firstName')) {
    +            this .firstName = (( args .firstName) as java.lang.String)
    +        } else {
    +            this .firstName = null
    +        }
    +        if (args.containsKey('lastName')) {
    +            this .lastName = (( args .lastName) as java.lang.String)
    +        } else {
    +            this .lastName = null
    +        }
    +    }
    +
    +    @groovy.transform.Generated
    +    public org.example.Genius() {
    +        this ([:])
    +    }
    +
    +    @groovy.transform.Generated
    +    public java.lang.String toString() {
    +        java.lang.Object _result = new java.lang.StringBuilder()
    +        java.lang.Object $toStringFirst = true
    +        _result.append('Genius(')
    +        if ( $toStringFirst ) {
    +            $toStringFirst = false
    +        } else {
    +            _result.append(', ')
    +        }
    +        _result.append(org.codehaus.groovy.runtime.InvokerHelper.toString(this.getFirstName()))
    +        if ( $toStringFirst ) {
    +            $toStringFirst = false
    +        } else {
    +            _result.append(', ')
    +        }
    +        _result.append(org.codehaus.groovy.runtime.InvokerHelper.toString(this.getLastName()))
    +        _result.append(')')
    +        if ( $to$string == null) {
    +            $to$string = _result.toString()
    +        }
    +        return $to$string 
    +    }
    +
    +    @groovy.transform.Generated
    +    public int hashCode() {
    +        if ( $hash$code == 0) {
    +            java.lang.Object _result = org.codehaus.groovy.util.HashCodeHelper.initHash()
    +            if (this.getFirstName() !== this ) {
    +                _result = org.codehaus.groovy.util.HashCodeHelper.updateHash(_result, this.getFirstName())
    +            }
    +            if (this.getLastName() !== this ) {
    +                _result = org.codehaus.groovy.util.HashCodeHelper.updateHash(_result, this.getLastName())
    +            }
    +            $hash$code = _result 
    +        }
    +        return $hash$code 
    +    }
    +
    +    @groovy.transform.Generated
    +    public boolean canEqual(java.lang.Object other) {
    +        return other instanceof org.example.Genius
    +    }
    +
    +    @groovy.transform.Generated
    +    public boolean equals(java.lang.Object other) {
    +        if ( other == null) {
    +            return false
    +        }
    +        if (this.is(other)) {
    +            return true
    +        }
    +        if (!( other instanceof org.example.Genius)) {
    +            return false
    +        }
    +        org.example.Genius otherTyped = (( other ) as org.example.Genius)
    +        if (!(otherTyped.canEqual( this ))) {
    +            return false
    +        }
    +        if (!(this.getFirstName() == otherTyped.getFirstName())) {
    +            return false
    +        }
    +        if (!(this.getLastName() == otherTyped.getLastName())) {
    +            return false
    +        }
    +        return true
    +    }
    +
     }
    ```


    ## 5_CANONICALIZATION vs 6_INSTRUCTION_SELECTION

    ```
    ```


    ## 6_INSTRUCTION_SELECTION vs 7_CLASS_GENERATION

    ```
    --- org_example_Genius.groovy-6_INSTRUCTION_SELECTION.groovy
    +++ org_example_Genius.groovy-7_CLASS_GENERATION.groovy
    @@ -11,21 +11,26 @@
     @groovy.transform.TupleConstructor(defaults = false)
     @groovy.transform.MapConstructor(noArg = true, includeSuperProperties = true, includeFields = true)
     @groovy.transform.KnownImmutable
    -public final class org.example.Genius extends java.lang.Object { 
    +public final class org.example.Genius extends java.lang.Object implements groovy.lang.GroovyObject { 
     
         private java.lang.String $to$string 
         private int $hash$code 
         private final java.lang.String firstName 
         private final java.lang.String lastName 
    +    private static org.codehaus.groovy.reflection.ClassInfo $staticClassInfo 
    +    public static transient boolean __$stMC 
    +    private transient groovy.lang.MetaClass metaClass 
     
         @groovy.transform.Generated
         public org.example.Genius(java.lang.String firstName, java.lang.String lastName) {
    +        metaClass = /*BytecodeExpression*/
             this .firstName = (( firstName ) as java.lang.String)
             this .lastName = (( lastName ) as java.lang.String)
         }
     
         @groovy.transform.Generated
         public org.example.Genius(java.util.Map args) {
    +        metaClass = /*BytecodeExpression*/
             if ( args == null) {
                 args = [:]
             }
    @@ -115,4 +120,37 @@
             return true
         }
     
    +    protected groovy.lang.MetaClass $getStaticMetaClass() {
    +    }
    +
    +    @groovy.transform.Generated
    +    @groovy.transform.Internal
    +    @java.beans.Transient
    +    public groovy.lang.MetaClass getMetaClass() {
    +    }
    +
    +    @groovy.transform.Generated
    +    @groovy.transform.Internal
    +    public void setMetaClass(groovy.lang.MetaClass mc) {
    +    }
    +
    +    @groovy.transform.Generated
    +    public final java.lang.String getFirstName() {
    +        return firstName 
    +    }
    +
    +    @groovy.transform.Generated
    +    public final java.lang.String getLastName() {
    +        return lastName 
    +    }
    +
    +    public boolean super$1$equals(java.lang.Object arg0) {
    +    }
    +
    +    public java.lang.String super$1$toString() {
    +    }
    +
    +    public int super$1$hashCode() {
    +    }
    +
     }
    ```


    ## 7_CLASS_GENERATION vs 8_OUTPUT

    ```
    ```


    ## 8_OUTPUT vs 9_FINALIZATION

    ```
    ```

If you read this report carefully, you would find how Groovy Compiler process a \*.groovy source to generate a binary .class with concreate details how the `Immutable` annotation affects the AST object of each individual CompilePhases.

For example,

1.  at the "3 CONVERSION" phase, the compiler finished parsing the given source and generate the initial AST object.

2.  at the "4 SEMANTIC\_ANALYSIS", "5 CANONICALIZATION" and "7 CLASS\_GENERATION" phases, the compiler gradually transforms the AST. It adds constructors, getters, equals and other fundamental methods as specified by the given `@Immutable` annotation.
