package org.example

import groovy.transform.Immutable as Immutable
import groovy.transform.ToString as ToString

@groovy.transform.ToString(includePackage = false, includeSuperProperties = true, cache = true)
@groovy.transform.EqualsAndHashCode(cache = true)
@groovy.transform.ImmutableBase
@groovy.transform.ImmutableOptions
@groovy.transform.PropertyOptions(propertyHandler = groovy.transform.options.ImmutablePropertyHandler)
@groovy.transform.TupleConstructor(defaults = false)
@groovy.transform.MapConstructor(noArg = true, includeSuperProperties = true, includeFields = true)
@groovy.transform.KnownImmutable
public class org.example.Genius extends java.lang.Object { 

    private java.lang.String firstName 
    private java.lang.String lastName 

}
