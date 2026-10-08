package com.example.urok

import org.junit.Assert.assertEquals
import org.junit.Test

class InterestTest {
    @Test
    fun examplesFromAssignment() {
        assertEquals(432194.24, compoundInterest(100000.0, 5.0, 30), 0.01)
        assertEquals(63814.08, compoundInterest(50000.0, 5.0, 5), 0.01)
        assertEquals(50000.0, compoundInterest(50000.0, 0.0, 5), 0.01)
    }
}
