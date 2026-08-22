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

package com.firozmemon.customkeyboard.ui.keyboard

import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.delete
import androidx.compose.ui.text.TextRange
import com.firozmemon.customkeyboard.model.Key
import com.firozmemon.customkeyboard.model.KeyAction

/**
 * Applies a key press to the message.
 *
 * Enter and Backspace used to be sent as KeyEvents through the Activity so
 * the focused EditText would act on them, which is why the old listener
 * needed an Activity at all. A TextFieldState can be edited directly, so all
 * three editing keys are ordinary buffer edits here and none of this needs a
 * running app to test.
 *
 * [KeyAction.ToggleLayer] is not handled: it changes which keys are on
 * screen, not the text, and belongs to the caller's state.
 */
internal fun TextFieldState.press(key: Key) = edit {
    when (key.action) {
        KeyAction.Insert -> replaceSelection(key.label)
        KeyAction.Newline -> replaceSelection("\n")
        KeyAction.Backspace -> backspace()
        KeyAction.ToggleLayer -> Unit
    }
}

/**
 * Drops [text] in at the caret, or over the selection if there is one, and
 * leaves the caret after it.
 *
 * TextRange.min/max already order a selection dragged right to left, so
 * unlike the old listener there is nothing to normalise by hand.
 */
private fun TextFieldBuffer.replaceSelection(text: String) {
    val start = selection.min
    replace(start, selection.max, text)
    selection = TextRange(start + text.length)
}

/**
 * Deletes the selection, or one character back from the caret.
 *
 * One code point rather than one char, so a surrogate pair goes whole; and
 * one code point rather than one grapheme cluster, so a backspace undoes
 * exactly one keypress. That matters for these scripts, where a syllable is
 * built up from several keys and users expect to unwind it the same way.
 */
private fun TextFieldBuffer.backspace() {
    if (!selection.collapsed) {
        val start = selection.min
        delete(start, selection.max)
        selection = TextRange(start)
        return
    }

    val caret = selection.start
    if (caret == 0) return

    val trailing = if (
        caret >= 2 &&
        charAt(caret - 1).isLowSurrogate() &&
        charAt(caret - 2).isHighSurrogate()
    ) 2 else 1

    delete(caret - trailing, caret)
    selection = TextRange(caret - trailing)
}
