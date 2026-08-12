/*
 * Copyright (c) 2016 - Firoz Memon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *                       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.firozmemon.customkeyboard

import android.text.Selection
import android.widget.EditText
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * onText() carries the only non-trivial logic in the listener: where the typed
 * text lands and where the caret ends up. Run against a real EditText so the
 * Editable and Selection semantics are the real ones rather than a mock's.
 */
@RunWith(AndroidJUnit4::class)
class BasicOnKeyboardActionListenerTest {

    private fun withField(
        initialText: String,
        selection: Pair<Int, Int>? = null,
        block: (BasicOnKeyboardActionListener, EditText) -> Unit,
    ) {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val editText = EditText(activity)
                editText.setText(initialText)
                if (selection == null) {
                    Selection.removeSelection(editText.text)
                } else {
                    Selection.setSelection(editText.text, selection.first, selection.second)
                }

                val listener = BasicOnKeyboardActionListener(
                    activity = activity,
                    editText = editText,
                    keyboardView = activity.mKeyboardView,
                    keyboardFor = { error("the layer toggles are not part of this test") },
                )
                block(listener, editText)
            }
        }
    }

    @Test
    fun insertsIntoAnEmptyFieldAndLeavesTheCaretAfterIt() {
        withField("", 0 to 0) { listener, editText ->
            listener.onText("क")

            assertEquals("क", editText.text.toString())
            assertEquals(1, editText.selectionStart)
        }
    }

    @Test
    fun insertsAtTheCaretRatherThanAppending() {
        withField("कग", 1 to 1) { listener, editText ->
            listener.onText("ख")

            assertEquals("कखग", editText.text.toString())
            assertEquals(2, editText.selectionStart)
        }
    }

    @Test
    fun appendsWhenTheCaretIsAtTheEnd() {
        withField("कख", 2 to 2) { listener, editText ->
            listener.onText("ग")

            assertEquals("कखग", editText.text.toString())
            assertEquals(3, editText.selectionStart)
        }
    }

    /**
     * The caret used to advance by exactly one regardless of how much the key
     * emitted. No shipped layout has a multi-character key, so this pins
     * behaviour that is currently only reachable by adding one.
     */
    @Test
    fun movesTheCaretByTheWholeStringNotOneCharacter() {
        withField("", 0 to 0) { listener, editText ->
            listener.onText("क्ष")   // three UTF-16 units

            assertEquals("क्ष", editText.text.toString())
            assertEquals(3, editText.selectionStart)
        }
    }

    @Test
    fun replacesASelectedRange() {
        withField("कखग", 0 to 2) { listener, editText ->
            listener.onText("घ")

            assertEquals("घग", editText.text.toString())
            assertEquals(1, editText.selectionStart)
        }
    }

    @Test
    fun normalisesASelectionMadeRightToLeft() {
        withField("कखग", 2 to 0) { listener, editText ->
            listener.onText("घ")

            assertEquals("घग", editText.text.toString())
            assertEquals(1, editText.selectionStart)
        }
    }

    @Test
    fun appendsWhenThereIsNoSelectionAtAll() {
        withField("कख", selection = null) { listener, editText ->
            listener.onText("ग")

            assertEquals("कखग", editText.text.toString())
            assertEquals(3, editText.selectionStart)
        }
    }
}
