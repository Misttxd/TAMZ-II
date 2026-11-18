package com.example.urokcv2

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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

    @Test
    fun monthlySavingsMatchesVideo() {
        val total = monthlySavings(5000.0, 2000.0, 4.0, 30)
        assertEquals(1404666.30, total, 0.01)
        assertEquals(679666.30, total - 725000.0, 0.01)
    }

    @Test
    fun taxReducesOnlyInterest() {
        assertEquals(1078467.331375, compoundInterest(1000000.0, 3.0, 3, 15.0), 0.01)
        assertEquals(2364.626544, monthlySavings(1000.0, 100.0, 12.0, 1, 15.0), 0.01)
    }

    @Test
    fun monthlySavingsHandlesZeroValues() {
        assertEquals(5000.0, monthlySavings(5000.0, 2000.0, 4.0, 0), 0.01)
        assertEquals(29000.0, monthlySavings(5000.0, 1000.0, 0.0, 2), 0.01)
        assertEquals(1268.250301, monthlySavings(0.0, 100.0, 12.0, 1), 0.01)
        assertEquals(0.0, monthlySavings(0.0, 0.0, 4.0, 30), 0.01)
    }

    @Test
    fun monthlyInterestAppliesWithZeroRegularDeposit() {
        assertEquals(446774.431401, monthlySavings(100000.0, 0.0, 5.0, 30), 0.01)
    }

    @Test
    fun stateKeepsCompoundingModeAndIgnoresDisabledMonthlyDeposit() {
        val state = SavingsState()
        state.deposit = 100000f
        state.monthlyDeposit = 0f
        state.rate = 5f
        state.years = 30f
        assertEquals(446774.431401, state.totalAmount(), 0.01)

        state.regularDeposit = false
        state.monthlyDeposit = 2000f
        assertEquals(432194.2375, state.totalAmount(), 0.01)
        assertEquals(100000.0, state.totalDeposits(), 0.01)
    }

    @Test
    fun stateAppliesDepositSettingsAndKeepsSavedSnapshot() {
        val state = SavingsState()
        state.deposit = 149000f
        state.changeDepositStep(10000f)
        assertEquals(150000f, state.deposit, 0.01f)
        state.changeDepositMaximum(100000f)
        assertEquals(100000f, state.deposit, 0.01f)

        state.saveCalculation()
        val savedCalculation = state.history[0]
        assertTrue(savedCalculation.contains("Počáteční vklad: ${money(100000.0)}"))
        assertTrue(savedCalculation.contains("Měsíční vklad: ${money(2000.0)}"))

        state.deposit = 5000f
        state.regularDeposit = false
        state.taxEnabled = true
        state.saveCalculation()
        assertEquals(2, state.history.size)
        assertEquals(savedCalculation, state.history[0])
        assertTrue(state.history[1].startsWith("Roční úročení"))
        assertTrue(state.history[1].contains("Měsíční vklad: ${money(0.0)}"))
        assertTrue(state.history[1].contains("Daň: 15 %"))
    }
}
