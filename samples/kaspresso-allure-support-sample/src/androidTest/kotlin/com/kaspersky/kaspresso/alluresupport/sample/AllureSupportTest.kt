package com.kaspersky.kaspresso.alluresupport.sample

import android.Manifest
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import com.kaspersky.components.alluresupport.withAllureSupport
import com.kaspersky.kaspresso.alluresupport.sample.screen.MainScreen
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

/**
 * Use [withAllureSupport] function to add the all available allure interceptors.
 */
class AllureSupportTest : TestCase(
    kaspressoBuilder = Kaspresso.Builder.withAllureSupport()
) {
    @get:Rule
    val runtimePermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java, true, false)

    @Test
    fun counter() = run {
        step("Launch the app") {
            activityRule.launchActivity(null)

            MainScreen {
                incrementButton.isDisplayed()
                decrementButton.isDisplayed()
                clearButton.isDisplayed()
                valueText.isDisplayed()
            }
        }

        step("Check increase and decrease buttons") {
            step("Increase value up to five") {
                MainScreen {
                    incrementButton {
                        repeat(5) {
                            click()
                        }
                    }

                    assertValue(5)
                }
            }

            step("Decrease value down to three") {
                MainScreen {
                    decrementButton {
                        repeat(2) {
                            click()
                        }
                    }

                    assertValue(3)
                }
            }
        }

        step("Clear the value") {
            MainScreen {
                clearButton.click()
                assertValue(0)
            }
        }
    }
}
