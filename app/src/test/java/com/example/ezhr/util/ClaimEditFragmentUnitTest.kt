package com.example.ezhr.util

import com.example.ezhr.fragments.claims.ClaimEditFragment
import junit.framework.Assert.assertEquals
import org.junit.Test

class ClaimEditFragmentUnitTest {
    @Test
    fun `empty title field return true`() {
        val check = ClaimEditFragment.isClaimTitleEmpty("")
        assertEquals(true, check)
    }

    @Test
    fun `empty amount field return true`() {
        val check = ClaimEditFragment.isClaimAmountEmpty(0)
        assertEquals(true, check)
    }

    @Test
    fun `empty desc field return true`() {
        val check = ClaimEditFragment.isClaimDescEmpty("")
        assertEquals(true, check)
    }
}