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
import android.content.Context
import android.graphics.Paint
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.os.Bundle
import android.util.SparseArray
import android.util.SparseIntArray
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

/**
 * Created by firoz
 */
class MainActivity : AppCompatActivity() {

    lateinit var messageEditText: EditText
    lateinit var switchLang: Spinner
    lateinit var mKeyboardView: CustomKeyboardView

    private lateinit var customKeyboardTouchListener: View.OnTouchListener
    private val keyboardCache = SparseArray<Keyboard>()
    private val glyphProbe = Paint()

    fun hideSoftKeyboard(activity: Activity) {
        val inputMethodManager =
            activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(messageEditText.windowToken, 0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        messageEditText = findViewById(R.id.messageEditText)
        switchLang = findViewById(R.id.switchLang)
        mKeyboardView = findViewById(R.id.keyboardView)

        // Neither of these ever changes, so they do not belong in selectKeyboard()
        mKeyboardView.isPreviewEnabled = false   // no preview balloons
        mKeyboardView.setOnKeyboardActionListener(BasicOnKeyboardActionListener())

        val langAdapter = ArrayAdapter.createFromResource(
            this, R.array.switchLangArr, android.R.layout.simple_spinner_item
        )
        langAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        switchLang.adapter = langAdapter

        /**
         * Set Keyboard on selection
         */
        switchLang.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, i: Int, l: Long) {
                selectKeyboard()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                /* no-op */
            }
        }

        /**
         * Placed on the EditText while a custom keyboard is active. Built once,
         * because it only has to move the caret - the keyboard itself is
         * already loaded by the time a touch can reach here.
         */
        customKeyboardTouchListener = View.OnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                showCustomKeyboard()

                val layout = (v as EditText).layout
                val x = event.x + messageEditText.scrollX
                val y = event.y + messageEditText.scrollY
                val line = layout.getLineForVertical(y.toInt())

                val offset = layout.getOffsetForHorizontal(line, x)
                if (offset > 0) {
                    if (x > layout.getLineMax(0)) {
                        messageEditText.setSelection(offset)     // touch was at end of text
                    } else {
                        messageEditText.setSelection(offset - 1)
                    }
                }
            }
            true
        }

        /**
         * If User clicks outside the Edittext block
         * then hide keyboard
         */
        messageEditText.setOnTouchListener { v, event ->
            if (v.id == R.id.messageEditText) {
                v.parent.requestDisallowInterceptTouchEvent(true)
                if ((event.action and MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
                    v.parent.requestDisallowInterceptTouchEvent(false)
                }
            }
            false
        }
    }

    override fun onResume() {
        super.onResume()

        /**
         * A selected language still owns the EditText after coming back from
         * the background, so restore its keyboard rather than dropping the
         * user onto the system one.
         */
        if (switchLang.selectedItemPosition != 0) {
            showCustomKeyboard()
        } else {
            mKeyboardView.visibility = View.GONE
        }
    }

    fun selectKeyboard() {
        // position 0 is the device's own keyboard, and has no entry
        val language = LANGUAGES.getOrNull(switchLang.selectedItemPosition - 1)

        if (language == null) {
            mKeyboardView.visibility = View.GONE

            //Show Default Keyboard
            showSystemKeyboard()
            return
        }

        /**
         * Without the font the keys draw as tofu and the keyboard is unusable,
         * so hand back to the device's own rather than show it.
         */
        if (!glyphProbe.hasGlyph(language.probeChar)) {
            showUnsupportedAlert(language.name)

            //Reset language selection
            switchLang.setSelection(0)
            mKeyboardView.visibility = View.GONE

            //Show Default Keyboard
            showSystemKeyboard()
            return
        }

        hideSoftKeyboard(this)
        focusForCustomKeyboard()

        showKeyboardWithAnimation()
        mKeyboardView.visibility = View.VISIBLE
        mKeyboardView.keyboard = keyboardFor(language.mainLayout)

        messageEditText.setOnTouchListener(customKeyboardTouchListener)
    }

    private fun showUnsupportedAlert(language: String) {
        AlertDialog.Builder(this)
            .setTitle(R.string.app_name)
            .setMessage("$language keyboard is not supported by your device")
            .setCancelable(false)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    /**
     * Building a Keyboard means parsing its XML and allocating every Key in it,
     * and the layer toggles do that on a keypress with the user waiting. The
     * layouts never change, so each one is built at most once. Key sizes are
     * computed from the display width at parse time, but a configuration change
     * recreates the Activity and with it this cache.
     */
    private fun keyboardFor(xmlResId: Int): Keyboard =
        keyboardCache.get(xmlResId)
            ?: Keyboard(this, xmlResId).also { keyboardCache.put(xmlResId, it) }

    /**
     * The custom keyboard drives the EditText, so keep the system keyboard from
     * popping up on focus, but still give the EditText focus ourselves. Without
     * focus the caret is never drawn and dispatchKeyEvent() has no target for
     * Enter/Backspace.
     */
    private fun focusForCustomKeyboard() {
        /**
         * setShowSoftInputOnFocus() only covers focus changes. The window
         * restores the system keyboard on its own when the activity returns to
         * the foreground with an editor focused, so it has to be told to stay
         * hidden as well.
         */
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        messageEditText.showSoftInputOnFocus = false
        messageEditText.requestFocus()
        messageEditText.isCursorVisible = true
    }

    /**
     * Hands the EditText back to the system keyboard, undoing everything
     * focusForCustomKeyboard() put in place.
     */
    private fun showSystemKeyboard() {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED)

        messageEditText.showSoftInputOnFocus = true
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(messageEditText, 0)
        messageEditText.setOnTouchListener(null)
    }

    /**
     * Brings the already selected keyboard back, either after a touch or when
     * returning from the background. Unlike selectKeyboard() this re-parses
     * nothing, so it is cheap enough to run from a touch handler.
     */
    private fun showCustomKeyboard() {
        hideSoftKeyboard(this)
        focusForCustomKeyboard()

        showKeyboardWithAnimation()
        mKeyboardView.visibility = View.VISIBLE
    }

    private fun showKeyboardWithAnimation() {
        if (mKeyboardView.visibility == View.GONE) {
            val animation = AnimationUtils.loadAnimation(this, R.anim.slide_from_bottom)
            mKeyboardView.showWithAnimation(animation)
        }
    }

    inner class BasicOnKeyboardActionListener : KeyboardView.OnKeyboardActionListener {

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
            val content = messageEditText.text
            var start = messageEditText.selectionStart
            var end = messageEditText.selectionEnd

            if (start < 0 || end < 0) {         // nothing focused yet, append
                start = content.length
                end = start
            } else if (start > end) {           // selected right to left
                val swap = start
                start = end
                end = swap
            }

            content.replace(start, end, text)
            messageEditText.setSelection(start + text.length)
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

                    this@MainActivity.dispatchKeyEvent(event)
                }

                else -> {
                    val layout = LAYER_FOR_KEYCODE.get(primaryCode)
                    if (layout != 0) {
                        mKeyboardView.keyboard = keyboardFor(layout)
                    }
                }
            }
        }
    }

    /**
     * Everything that varies between the keyboards, in one row each.
     *
     * @param probeChar a single base letter of the script, used to check the
     *   device can draw it. Deliberately one character: hasGlyph() asks whether
     *   the whole string maps to one glyph, so a word answers false even where
     *   the script renders perfectly well. Checking the font directly is what
     *   distinguishes a real letter from the tofu box drawn in its place.
     */
    private data class Language(
        val name: String,
        val mainLayout: Int,
        val altLayout: Int,
        val toAltKeyCode: Int,
        val toMainKeyCode: Int,
        val probeChar: String,
    )

    companion object {
        /**
         * One row per spinner position after Default, in the order
         * R.array.switchLangArr lists them.
         */
        private val LANGUAGES = listOf(
            Language("Hindi", R.xml.kbd_hin1, R.xml.kbd_hin2, -106, -107, "क"),   // U+0915 Devanagari ka
            Language("Gujarati", R.xml.kbd_guj1, R.xml.kbd_guj2, -108, -109, "ક"),   // U+0A95 Gujarati ka
            Language("Marathi", R.xml.kbd_mar1, R.xml.kbd_mar2, -110, -111, "क"),   // U+0915 Devanagari ka
            Language("Kannada", R.xml.kbd_knd1, R.xml.kbd_knd2, -112, -113, "ಕ"),   // U+0C95 Kannada ka
            Language("Tamil", R.xml.kbd_tam1, R.xml.kbd_tam2, -114, -115, "க"),   // U+0B95 Tamil ka
            Language("Punjabi", R.xml.kbd_punj1, R.xml.kbd_punj2, -116, -117, "ਕ"),   // U+0A15 Gurmukhi ka
        )

        /**
         * Derived so the toggle keys cannot drift out of step with the layouts
         * they switch between.
         */
        private val LAYER_FOR_KEYCODE = SparseIntArray().apply {
            LANGUAGES.forEach { language ->
                put(language.toAltKeyCode, language.altLayout)
                put(language.toMainKeyCode, language.mainLayout)
            }
        }
    }
}
