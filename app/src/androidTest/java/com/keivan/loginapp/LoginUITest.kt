package me.haj1.loginapp

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import me.haj1.loginapp.ui.MainActivity
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class LoginScreenEspressoTest {

    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun setup() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @Test
    fun loginButtonIsDisabledUntilBothFieldsAreFilled() {  // ← Fixed: No backticks
        // Initially disabled
        onView(withTagValue(`is`(equalTo("btn_login")))).check(matches(isNotEnabled()))

        // Type username only → still disabled
        onView(withTagValue(`is`(equalTo("et_userName")))).perform(
            typeText("kayvan"),
            closeSoftKeyboard()
        )
        onView(withTagValue(`is`(equalTo("btn_login")))).check(matches(isNotEnabled()))

        // Type password → now enabled
        onView(withTagValue(`is`(equalTo("et_password")))).perform(
            typeText("123456"),
            closeSoftKeyboard()
        )
        onView(withTagValue(`is`(equalTo("btn_login")))).check(matches(isEnabled()))
    }

    @Test
    fun successfulLoginShowsNoError() {  // ← Fixed
        typeUsername("kayvan")
        typePassword("123456")
        clickLogin()

        // No error message appears → success
        onView(withTagValue(`is`(equalTo("tv_error")))).check(doesNotExist())
    }

    @Test
    fun threeFailedAttemptsShowLockoutMessage() {  // ← Fixed (covers #3 & #4)
        typeUsername("fail")
        typePassword("wrong")

        repeat(3) {
            clickLogin()
            Thread.sleep(800) // Wait for UI update (use IdlingResource in production)

            if (it < 2) {
                onView(withTagValue(`is`(equalTo("tv_error"))))
                    .check(matches(withText(containsString("Attempt ${it + 1}/3"))))
            }
        }

        // After 3rd attempt → lockout
        onView(withTagValue(`is`(equalTo("tv_lockout"))))
            .check(matches(withText(containsString("Account locked"))))
        onView(withTagValue(`is`(equalTo("btn_login")))).check(matches(isNotEnabled()))
    }

    @Test
    fun offlineShowsNoInternetMessage() {  // ← Fixed: This is your problematic test
        typeUsername("kayvan")
        typePassword("123456")
        clickLogin()

        onView(withTagValue(`is`(equalTo("tv_error"))))
            .check(matches(withText("No internet connection")))
    }

    // Helpers (unchanged)
    private fun typeUsername(text: String) {
        onView(withTagValue(`is`(equalTo("et_userName"))))
            .perform(clearText(), typeText(text), closeSoftKeyboard())
    }

    private fun typePassword(text: String) {
        onView(withTagValue(`is`(equalTo("et_password"))))
            .perform(clearText(), typeText(text), closeSoftKeyboard())
    }

    private fun clickLogin() {
        onView(withTagValue(`is`(equalTo("btn_login")))).perform(click())
    }
}