package org.example

import groovy.transform.Immutable
import groovy.transform.ToString

@Immutable
@ToString(includePackage=false)
class Genius {
    String firstName, lastName
}
