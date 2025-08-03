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
