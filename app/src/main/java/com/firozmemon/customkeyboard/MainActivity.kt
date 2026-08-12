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
import android.os.Bundle
import android.util.SparseArray
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
        mKeyboardView.setOnKeyboardActionListener(
            BasicOnKeyboardActionListener(this, messageEditText, mKeyboardView, ::keyboardFor)
        )

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

                /*
                 * This listener consumes the event, so the EditText never gets
                 * to place the caret itself. getOffsetForPosition() is the
                 * framework's own hit-testing and accounts for padding, scroll
                 * and the line actually touched - the arithmetic that used to
                 * live here got all three wrong.
                 */
                val field = v as EditText
                val offset = field.getOffsetForPosition(event.x, event.y)
                if (offset >= 0) {
                    field.setSelection(offset)
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
}
