package com.example.ezhr.util

import com.example.ezhr.fragments.EditPasswordFragment
import org.junit.Assert
import org.junit.Test

class ChangePasswordUnitTest {
    @Test
    fun `current password empty return true`() {
        val text = EditPasswordFragment.isCurrentPasswordEmpty("")
        Assert.assertEquals(true, text)
    }

    @Test
    fun `new password empty return true`() {
        val text = EditPasswordFragment.isNewPasswordEmpty("")
        Assert.assertEquals(true, text)
    }

    @Test
    fun `confirm new password empty return true`() {
        val text = EditPasswordFragment.isConfirmPasswordEmpty("")
        Assert.assertEquals(true, text)
    }

    @Test
    fun `new password does not match confirm new password return false`() {
        val text = EditPasswordFragment.isPasswordEqual("test", "123")
        Assert.assertEquals(false, text)
    }
}