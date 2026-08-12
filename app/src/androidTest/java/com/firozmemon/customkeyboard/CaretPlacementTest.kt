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

import android.os.SystemClock
import android.view.MotionEvent
import android.widget.EditText
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tapping the message field while a custom keyboard is up has to put the caret
 * where the finger landed. The touch listener owns that, because it consumes
 * the event and the EditText never gets to place the caret itself.
 */
@RunWith(AndroidJUnit4::class)
class CaretPlacementTest {

    private fun tapAtOffset(text: String, targetOffset: Int): Int {
        var actual = -1

        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            // a language other than Default installs the custom touch listener
            scenario.onActivity { it.switchLang.setSelection(1) }
            Espresso.onIdle()

            scenario.onActivity { it.messageEditText.setText(text) }
            Espresso.onIdle()

            scenario.onActivity { activity ->
                val field = activity.messageEditText
                val layout = requireNotNull(field.layout) { "field was never laid out" }

                val line = layout.getLineForOffset(targetOffset)
                val x = layout.getPrimaryHorizontal(targetOffset) +
                    field.totalPaddingLeft - field.scrollX
                val y = (layout.getLineTop(line) + layout.getLineBottom(line)) / 2f +
                    field.totalPaddingTop - field.scrollY

                tap(field, x, y)
                actual = field.selectionStart
            }
        }

        return actual
    }

    private fun tap(field: EditText, x: Float, y: Float) {
        val now = SystemClock.uptimeMillis()
        val down = MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN, x, y, 0)
        val up = MotionEvent.obtain(now, now + 1, MotionEvent.ACTION_UP, x, y, 0)
        try {
            field.dispatchTouchEvent(down)
            field.dispatchTouchEvent(up)
        } finally {
            down.recycle()
            up.recycle()
        }
    }

    @Test
    fun tappingMidLinePutsTheCaretWhereTheFingerLanded() {
        assertEquals(3, tapAtOffset("कखगघङ", targetOffset = 3))
    }

    @Test
    fun tappingAtTheStartPutsTheCaretAtTheStart() {
        assertEquals(0, tapAtOffset("कखगघङ", targetOffset = 0))
    }

    /**
     * The old arithmetic compared against line 0's width whichever line was
     * touched, so a tap on a later line landed on whichever branch line 0
     * happened to select.
     */
    @Test
    fun tappingOnALaterLineUsesThatLine() {
        assertEquals(4, tapAtOffset("कख\nगघङ", targetOffset = 4))
    }
}
