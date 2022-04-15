package com.example.ezhr.util


import com.example.ezhr.ForgetPasswordActivity
import org.junit.Assert.assertEquals
import org.junit.Test


class ForgetPasswordActivityUnitTest {


    @Test
    fun `correct email format return true`() {
        val text = ForgetPasswordActivity.isEmailValid("test1234@gmail.com")
        assertEquals(true, text)
    }


    @Test
    fun `empty email field return true`() {
        val text = ForgetPasswordActivity.isEmailEmpty("")
        assertEquals(true, text)
    }
}
