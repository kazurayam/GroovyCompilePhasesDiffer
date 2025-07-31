package org.example

import org.junit.jupiter.api.Test
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull

class GeniousTest {

    @Test
    void testMapConstructor() {
        Genius instance = new Genius(firstName: 'Isaac', lastName: 'Newton')
        assertNotNull(instance)
    }

    @Test
    void testTupleStyleConstructor() {
        Genius instance = new Genius('Isaac', 'Newton')
        assertNotNull(instance)
    }

    @Test
    void testGetters() {
        Genius instance = new Genius('Isaac', 'Newton')
        assertEquals('Isaac', instance.getFirstName())
        assertEquals('Newton', instance.getLastName())
    }

    @Test
    void testToString() {
        Genius instance = new Genius('Isaac', 'Newton')
        assertEquals('Genius(Isaac, Newton)',
                instance.toString())
    }
}
