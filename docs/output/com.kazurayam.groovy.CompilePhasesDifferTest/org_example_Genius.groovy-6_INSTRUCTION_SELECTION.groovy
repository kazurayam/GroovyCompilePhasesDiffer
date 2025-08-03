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
public final class org.example.Genius extends java.lang.Object { 

    private java.lang.String $to$string 
    private int $hash$code 
    private final java.lang.String firstName 
    private final java.lang.String lastName 

    @groovy.transform.Generated
    public org.example.Genius(java.lang.String firstName, java.lang.String lastName) {
        this .firstName = (( firstName ) as java.lang.String)
        this .lastName = (( lastName ) as java.lang.String)
    }

    @groovy.transform.Generated
    public org.example.Genius(java.util.Map args) {
        if ( args == null) {
            args = [:]
        }
        org.codehaus.groovy.transform.ImmutableASTTransformation.checkPropNames(this, args)
        if (args.containsKey('firstName')) {
            this .firstName = (( args .firstName) as java.lang.String)
        } else {
            this .firstName = null
        }
        if (args.containsKey('lastName')) {
            this .lastName = (( args .lastName) as java.lang.String)
        } else {
            this .lastName = null
        }
    }

    @groovy.transform.Generated
    public org.example.Genius() {
        this ([:])
    }

    @groovy.transform.Generated
    public java.lang.String toString() {
        java.lang.Object _result = new java.lang.StringBuilder()
        java.lang.Object $toStringFirst = true
        _result.append('Genius(')
        if ( $toStringFirst ) {
            $toStringFirst = false
        } else {
            _result.append(', ')
        }
        _result.append(org.codehaus.groovy.runtime.InvokerHelper.toString(this.getFirstName()))
        if ( $toStringFirst ) {
            $toStringFirst = false
        } else {
            _result.append(', ')
        }
        _result.append(org.codehaus.groovy.runtime.InvokerHelper.toString(this.getLastName()))
        _result.append(')')
        if ( $to$string == null) {
            $to$string = _result.toString()
        }
        return $to$string 
    }

    @groovy.transform.Generated
    public int hashCode() {
        if ( $hash$code == 0) {
            java.lang.Object _result = org.codehaus.groovy.util.HashCodeHelper.initHash()
            if (this.getFirstName() !== this ) {
                _result = org.codehaus.groovy.util.HashCodeHelper.updateHash(_result, this.getFirstName())
            }
            if (this.getLastName() !== this ) {
                _result = org.codehaus.groovy.util.HashCodeHelper.updateHash(_result, this.getLastName())
            }
            $hash$code = _result 
        }
        return $hash$code 
    }

    @groovy.transform.Generated
    public boolean canEqual(java.lang.Object other) {
        return other instanceof org.example.Genius
    }

    @groovy.transform.Generated
    public boolean equals(java.lang.Object other) {
        if ( other == null) {
            return false
        }
        if (this.is(other)) {
            return true
        }
        if (!( other instanceof org.example.Genius)) {
            return false
        }
        org.example.Genius otherTyped = (( other ) as org.example.Genius)
        if (!(otherTyped.canEqual( this ))) {
            return false
        }
        if (!(this.getFirstName() == otherTyped.getFirstName())) {
            return false
        }
        if (!(this.getLastName() == otherTyped.getLastName())) {
            return false
        }
        return true
    }

}
