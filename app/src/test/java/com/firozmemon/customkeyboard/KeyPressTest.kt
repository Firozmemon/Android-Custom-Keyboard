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

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.text.TextRange
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Where a key press puts text and where it leaves the caret.
 *
 * These ran on a device when the same logic went through an EditText and a
 * dispatched KeyEvent, which needed an Activity to dispatch from. Editing a
 * TextFieldState needs nothing but the JVM.
 */
class KeyPressTest {

    private fun field(text: String, selection: IntRange) =
        TextFieldState(text, TextRange(selection.first, selection.last))

    private fun TextFieldState.assertIs(text: String, caret: Int) {
        assertEquals(text, this.text.toString())
        assertEquals(TextRange(caret), selection)
    }

    // ---- typing -----------------------------------------------------------

    @Test
    fun insertsIntoAnEmptyFieldAndLeavesTheCaretAfterIt() {
        val state = field("", 0..0)

        state.press(Key("क"))

        state.assertIs("क", caret = 1)
    }

    @Test
    fun insertsAtTheCaretRatherThanAppending() {
        val state = field("कग", 1..1)

        state.press(Key("ख"))

        state.assertIs("कखग", caret = 2)
    }

    @Test
    fun appendsWhenTheCaretIsAtTheEnd() {
        val state = field("कख", 2..2)

        state.press(Key("ग"))

        state.assertIs("कखग", caret = 3)
    }

    /**
     * The old listener advanced the caret by exactly one however much the key
     * emitted. No shipped layout has a multi-character key, so this pins
     * behaviour that is only reachable by adding one.
     */
    @Test
    fun movesTheCaretByTheWholeLabelNotOneCharacter() {
        val state = field("", 0..0)

        state.press(Key("क्ष"))   // three UTF-16 units

        state.assertIs("क्ष", caret = 3)
    }

    @Test
    fun replacesASelectedRange() {
        val state = field("कखग", 0..2)

        state.press(Key("घ"))

        state.assertIs("घग", caret = 1)
    }

    @Test
    fun normalisesASelectionMadeRightToLeft() {
        val state = field("कखग", 2..0)

        state.press(Key("घ"))

        state.assertIs("घग", caret = 1)
    }

    @Test
    fun enterInsertsANewline() {
        val state = field("कख", 2..2)

        state.press(Key("↵", KeyAction.Newline))

        state.assertIs("कख\n", caret = 3)
    }

    // ---- backspace --------------------------------------------------------

    @Test
    fun backspaceDeletesTheCharacterBeforeTheCaret() {
        val state = field("कखग", 2..2)

        state.press(Key("←", KeyAction.Backspace))

        state.assertIs("कग", caret = 1)
    }

    @Test
    fun backspaceAtTheStartDoesNothing() {
        val state = field("कख", 0..0)

        state.press(Key("←", KeyAction.Backspace))

        state.assertIs("कख", caret = 0)
    }

    @Test
    fun backspaceOnAnEmptyFieldDoesNothing() {
        val state = field("", 0..0)

        state.press(Key("←", KeyAction.Backspace))

        state.assertIs("", caret = 0)
    }

    @Test
    fun backspaceDeletesTheSelectionRatherThanOneCharacter() {
        val state = field("कखगघ", 1..3)

        state.press(Key("←", KeyAction.Backspace))

        state.assertIs("कघ", caret = 1)
    }

    /**
     * Nothing in these layouts is outside the BMP, but a paste can be, and
     * half a surrogate pair is not a character anyone meant to leave behind.
     */
    @Test
    fun backspaceTakesASurrogatePairWhole() {
        val state = field("क😀", 3..3)

        state.press(Key("←", KeyAction.Backspace))

        state.assertIs("क", caret = 1)
    }

    @Test
    fun switchingLayerLeavesTheTextAlone() {
        val state = field("कख", 1..1)

        state.press(Key("⇧", KeyAction.ToggleLayer))

        state.assertIs("कख", caret = 1)
    }
}
