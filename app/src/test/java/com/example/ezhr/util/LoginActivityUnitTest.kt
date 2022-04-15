package com.example.ezhr.util


import com.example.ezhr.LoginActivity
import org.junit.Assert.assertEquals
import org.junit.Test


class LoginActivityUnitTest {


    @Test
    fun `correct email format return true`() {
        val text = LoginActivity.isEmailValid("test1234@gmail.com")
        assertEquals(true, text)
    }

    @Test
    fun `empty email or password field return true`() {
        val text = LoginActivity.isInputEmpty("", "")
        assertEquals(true, text)
    }

    @Test
    fun `empty email field return true`() {
        val text = LoginActivity.isEmailEmpty("")
        assertEquals(true, text)
    }

    @Test
    fun `empty password field return true`() {
        val text = LoginActivity.isEmailEmpty("")
        assertEquals(true, text)
    }


}
