package com.kazurayam.example

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
