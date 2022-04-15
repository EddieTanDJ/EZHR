package com.example.ezhr

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginActivityTest {
    // Get the login activity Scenario
    @get:Rule
    var activityScenarioRule = activityScenarioRule<LoginActivity>()

    /**
     * Test if the error message is displayed when the user enters blank email
     */
    @Test
    fun testEmailInput() {
        Log.d(TAG, "testEmailInput: ")
        onView(withId(R.id.email_edt_text)).check(matches(isDisplayed()))
            .perform(typeText(""), closeSoftKeyboard())
        onView(withId(R.id.login_btn)).check(matches(isDisplayed())).perform(click())
        onView(withId(R.id.email_edt_text)).check(matches(hasErrorText(ENTER_EMAIL)))
    }

    /**
     * Test if the error message is displayed when the user enters blank password
     */
    @Test
    fun testPasswordInput() {
        Log.d(TAG, "testPasswordInput: ")
        onView(withId(R.id.pass_edt_text)).check(matches(isDisplayed()))
            .perform(typeText(""), closeSoftKeyboard())
        onView(withId(R.id.login_btn)).check(matches(isDisplayed())).perform(click())
        onView(withId(R.id.pass_edt_text)).check(matches(hasErrorText(ENTER_PASSWORD)))
    }

    /**
     * Test if the error message is displayed when the user enters invalid email
     */
    @Test
    fun testValidEmail() {
        Log.d(TAG, "testValidEmail: ")
        onView(withId(R.id.email_edt_text)).check(matches(isDisplayed()))
            .perform(typeText("test"), closeSoftKeyboard())
        onView(withId(R.id.pass_edt_text)).check(matches(isDisplayed()))
            .perform(typeText("test"), closeSoftKeyboard())
        onView(withId(R.id.login_btn)).check(matches(isDisplayed())).perform(click())
        onView(withId(R.id.email_edt_text)).check(matches(hasErrorText(INVALID_EMAIL)))
    }

    /**
     * Test login and home fragment is displayed when the user enters valid email and password
     */
    @Test
    fun testHomeFragment() {
        val navController = TestNavHostController(
            ApplicationProvider.getApplicationContext()
        )
        Log.d(TAG, "testHomeFragment: ")
        onView(withId(R.id.email_edt_text)).check(matches(isDisplayed()))
            .perform(typeText("test1234@gmail.com"), closeSoftKeyboard())
        onView(withId(R.id.pass_edt_text)).check(matches(isDisplayed()))
            .perform(typeText("123456"), closeSoftKeyboard())
        onView(withId(R.id.login_btn)).check(matches(isDisplayed())).perform(click())
        Thread.sleep(5000)
        onView(withId(R.id.homeFragmentScrollView)).check(matches(isDisplayed()))
        onView(withId(R.id.welcome)).check(matches(isDisplayed())).check(matches(withText(WELCOME)))
        onView(withId(R.id.linearLayoutChart)).check(matches(isDisplayed()))
        onView(withId(R.id.buttonAttendance)).check(matches(isDisplayed()))
        onView(withId(R.id.buttonLeave)).check(matches(isDisplayed()))
    }

    companion object {
        private val TAG = LoginActivityTest::class.simpleName
        private const val ENTER_EMAIL = "Please enter email address"
        private const val ENTER_PASSWORD = "Please enter password"
        private const val INVALID_EMAIL = "Please enter valid email address"
        private const val WELCOME = "Hello John Doe"
    }
}