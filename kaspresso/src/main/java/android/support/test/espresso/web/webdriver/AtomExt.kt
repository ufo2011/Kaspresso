package android.support.test.espresso.web.webdriver

import android.support.test.espresso.web.model.Atom
import android.support.test.espresso.web.model.Evaluation
import android.support.test.espresso.web.webdriver.WebDriverAtomScripts.*

/**
 * Uses [WebDriverAtomScripts] class, that has package-local access in Espresso, so it has to be in the same package.
 *
 * @return a string description of [Atom].
 */
fun Atom<*>.describeTo(
    builder: StringBuilder,
    evaluation: Evaluation?
) {
    builder
        .append(" \"${getActionDescription()}\"")
        .apply {
            evaluation?.let { eval: Evaluation ->
                if (eval.hasMessage()) {
                    append(" with message=\"${eval.message}\"")
                }
                eval.value?.let { result: Any ->
                    append(" with result=\"$result\"")
                }
            }
        }
}

private fun Atom<*>.getActionDescription(): String {
    return when (script) {
        GET_VISIBLE_TEXT_ANDROID -> "get visible text"
        CLICK_ANDROID -> "click on element"
        SCROLL_INTO_VIEW_ANDROID -> "scroll into view"
        CLEAR_ANDROID -> "clear"
        SEND_KEYS_ANDROID -> "send keys"
        ACTIVE_ELEMENT_ANDROID -> "active element"
        FRAME_BY_ID_OR_NAME_ANDROID -> "frame by id or name"
        FRAME_BY_INDEX_ANDROID -> "frame by index android"
        FIND_ELEMENT_ANDROID -> "find element"
        FIND_ELEMENTS_ANDROID -> "find elements"
        else -> ""
    }
}