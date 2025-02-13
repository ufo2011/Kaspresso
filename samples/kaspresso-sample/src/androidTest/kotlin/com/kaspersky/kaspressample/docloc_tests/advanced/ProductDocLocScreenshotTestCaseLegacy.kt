package com.kaspersky.kaspressample.docloc_tests.advanced

import androidx.test.rule.ActivityTestRule
import com.kaspersky.kaspressample.docloc.FragmentTestActivity
import com.kaspersky.kaspresso.testcases.api.testcase.DocLocScreenshotTestCase
import org.junit.Rule
import java.io.File

open class ProductDocLocScreenshotTestCaseLegacy : DocLocScreenshotTestCase(
    screenshotsDirectory = File("screenshots"),
    locales = "en,ru"
) {
    @get:Rule
    val activityTestRule = ActivityTestRule(FragmentTestActivity::class.java, false, true)

    protected val activity: FragmentTestActivity
        get() = activityTestRule.activity
}
