package com.example.urokcv2

import org.junit.Assert.assertEquals
import org.junit.Test

class InterestTest {
    @Test
    fun compoundInterestMatchesAssignment() {
        assertEquals(432194.2375, compoundInterest(100000.0, 5.0, 30), 0.01)
        assertEquals(63814.0781, compoundInterest(50000.0, 5.0, 5), 0.01)
        assertEquals(50000.0, compoundInterest(50000.0, 0.0, 5), 0.01)
        assertEquals(50000.0, compoundInterest(50000.0, 5.0, 0), 0.01)
        assertEquals(0.0, compoundInterest(0.0, 5.0, 30), 0.01)
    }
}
