package com.example.ezhr.util

import com.example.ezhr.fragments.claims.ApplyClaimFragment
import junit.framework.Assert.assertEquals
import org.junit.Test

class ApplyClaimFragmentUnitTest {
    @Test
    fun `empty title field return true`() {
        val check = ApplyClaimFragment.isClaimTitleEmpty("")
        assertEquals(true, check)
    }

    @Test
    fun `empty amount field return true`() {
        val check = ApplyClaimFragment.isClaimAmountEmpty(0)
        assertEquals(true, check)
    }

    @Test
    fun `empty desc field return true`() {
        val check = ApplyClaimFragment.isClaimDescEmpty("")
        assertEquals(true, check)
    }
}