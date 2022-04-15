package com.example.ezhr

import android.util.Log
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.activityScenarioRule
import org.junit.Rule
import org.junit.Test

class ForgetPasswordActivityTest {
    @get:Rule
    var activityScenarioRule = activityScenarioRule<ForgetPasswordActivity>()

    /**
     * Test if the error message is displayed when the user enters blank email
     */
    @Test
    fun testEmailInput() {
        Log.d(TAG, "testEmailInput")
        onView(withId(R.id.email_edt_text)).check(matches(isDisplayed()))
            .perform(typeText(""), closeSoftKeyboard())
        onView(withId(R.id.reset_pass_btn)).check(matches(isDisplayed())).perform(click())
        onView(withId(R.id.email_edt_text)).check(matches(hasErrorText(ENTER_EMAIL)))
    }

    /**
     * Test if the error message is displayed when the user enters invalid email
     */
    @Test
    fun testValidEmail() {
        Log.d(TAG, "testValidEmail")
        onView(withId(R.id.email_edt_text)).check(matches(isDisplayed()))
            .perform(typeText("test"), closeSoftKeyboard())
        onView(withId(R.id.reset_pass_btn)).check(matches(isDisplayed())).perform(click())
        onView(withId(R.id.email_edt_text)).check(matches(hasErrorText(INVALID_EMAIL)))
    }


    companion object {
        private val TAG = ForgetPasswordActivityTest::class.simpleName
        private const val ENTER_EMAIL = "Please enter email address"
        private const val INVALID_EMAIL = "Please enter valid email address"
    }
}