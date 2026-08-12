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

import android.app.Activity
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.view.KeyEvent
import android.widget.EditText

/**
 * Turns presses on the custom keyboard into edits on the target field.
 *
 * Its dependencies are explicit rather than reached through an enclosing
 * Activity, so it can be constructed - and its caret arithmetic tested -
 * without one.
 *
 * @param keyboardFor supplies a parsed layout by XML resource id, so the
 *   layer toggles reuse the caller's cache rather than re-parsing.
 */
internal class BasicOnKeyboardActionListener(
    private val activity: Activity,
    private val editText: EditText,
    private val keyboardView: CustomKeyboardView,
    private val keyboardFor: (Int) -> Keyboard,
) : KeyboardView.OnKeyboardActionListener {

    override fun swipeUp() {
        /* no-op */
    }

    override fun swipeRight() {
        /* no-op */
    }

    override fun swipeLeft() {
        /* no-op */
    }

    override fun swipeDown() {
        /* no-op */
    }

    override fun onText(text: CharSequence) {
        /*
         * Editable.replace() edits in place. Rebuilding the message as a
         * String and handing it back through setText() copied the whole
         * thing twice per keystroke and discarded the Editable, and the
         * old caret arithmetic assumed every key emits one character.
         */
        val content = editText.text
        var start = editText.selectionStart
        var end = editText.selectionEnd

        if (start < 0 || end < 0) {         // nothing focused yet, append
            start = content.length
            end = start
        } else if (start > end) {           // selected right to left
            val swap = start
            start = end
            end = swap
        }

        content.replace(start, end, text)
        editText.setSelection(start + text.length)
    }

    override fun onRelease(primaryCode: Int) {
        /* no-op */
    }

    override fun onPress(primaryCode: Int) {
        /* no-op */
    }

    override fun onKey(primaryCode: Int, keyCodes: IntArray?) {
        when (primaryCode) {
            66, 67 -> {
                val eventTime = System.currentTimeMillis()
                val event = KeyEvent(
                    eventTime, eventTime, KeyEvent.ACTION_DOWN, primaryCode, 0, 0, 0, 0,
                    KeyEvent.FLAG_SOFT_KEYBOARD or KeyEvent.FLAG_KEEP_TOUCH_MODE
                )

                activity.dispatchKeyEvent(event)
            }

            else -> {
                val layout = LAYER_FOR_KEYCODE.get(primaryCode)
                if (layout != 0) {
                    keyboardView.keyboard = keyboardFor(layout)
                }
            }
        }
    }
}
