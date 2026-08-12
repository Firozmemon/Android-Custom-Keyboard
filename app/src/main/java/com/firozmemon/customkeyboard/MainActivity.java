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

package com.firozmemon.customkeyboard;

import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.text.Editable;
import android.text.Layout;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by firoz
 */
public class MainActivity extends AppCompatActivity {

    EditText messageEditText;
    Spinner switchLang;
    CustomKeyboardView mKeyboardView;

    private View.OnTouchListener customKeyboardTouchListener;
    private final SparseArray<Keyboard> keyboardCache = new SparseArray<>();
    private final Paint glyphProbe = new Paint();

    /**
     * Indexed by the spinner position, where 0 is the device's own keyboard and
     * so has no layout of its own. Keep in step with R.array.switchLangArr.
     */
    private static final int[] LAYOUT_FOR_POSITION = {
            0,
            R.xml.kbd_hin1,
            R.xml.kbd_guj1,
            R.xml.kbd_mar1,
            R.xml.kbd_knd1,
            R.xml.kbd_tam1,
            R.xml.kbd_punj1,
    };

    /**
     * The layer-toggle keys (the shift and १२३ keys) carry these codes, one
     * pair per language, and each one only has to swap in another layout.
     */
    private static final SparseIntArray LAYER_FOR_KEYCODE = new SparseIntArray();

    static {
        LAYER_FOR_KEYCODE.put(-106, R.xml.kbd_hin2);
        LAYER_FOR_KEYCODE.put(-107, R.xml.kbd_hin1);
        LAYER_FOR_KEYCODE.put(-108, R.xml.kbd_guj2);
        LAYER_FOR_KEYCODE.put(-109, R.xml.kbd_guj1);
        LAYER_FOR_KEYCODE.put(-110, R.xml.kbd_mar2);
        LAYER_FOR_KEYCODE.put(-111, R.xml.kbd_mar1);
        LAYER_FOR_KEYCODE.put(-112, R.xml.kbd_knd2);
        LAYER_FOR_KEYCODE.put(-113, R.xml.kbd_knd1);
        LAYER_FOR_KEYCODE.put(-114, R.xml.kbd_tam2);
        LAYER_FOR_KEYCODE.put(-115, R.xml.kbd_tam1);
        LAYER_FOR_KEYCODE.put(-116, R.xml.kbd_punj2);
        LAYER_FOR_KEYCODE.put(-117, R.xml.kbd_punj1);
    }

    private static final String[] LANGUAGE_NAME = {
            null, "Hindi", "Gujarati", "Marathi", "Kannada", "Tamil", "Punjabi",
    };

    /**
     * One base letter per script. Deliberately a single character: hasGlyph()
     * asks whether the whole string maps to one glyph, so a word answers false
     * even on a device that renders the script perfectly well.
     */
    private static final String[] PROBE_CHAR = {
            null,
            "क",   // U+0915 Devanagari ka - Hindi
            "ક",   // U+0A95 Gujarati ka
            "क",   // U+0915 Devanagari ka - Marathi
            "ಕ",   // U+0C95 Kannada ka
            "க",   // U+0B95 Tamil ka
            "ਕ",   // U+0A15 Gurmukhi ka - Punjabi
    };

    public void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(messageEditText.getWindowToken(), 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        messageEditText = findViewById(R.id.messageEditText);
        switchLang = findViewById(R.id.switchLang);
        mKeyboardView = findViewById(R.id.keyboardView);

        // Neither of these ever changes, so they do not belong in selectKeyboard()
        mKeyboardView.setPreviewEnabled(false);   // no preview balloons
        mKeyboardView.setOnKeyboardActionListener(new BasicOnKeyboardActionListener());

        ArrayAdapter<CharSequence> langAdapter = ArrayAdapter.createFromResource(this, R.array.switchLangArr, android.R.layout.simple_spinner_item);
        langAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        switchLang.setAdapter(langAdapter);

        /**
         * Set Keyboard on selection
         */
        switchLang.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectKeyboard();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                /* no-op */
            }
        });

        /**
         * Placed on the EditText while a custom keyboard is active. Built once,
         * because it only has to move the caret - the keyboard itself is
         * already loaded by the time a touch can reach here.
         */
        customKeyboardTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    showCustomKeyboard();

                    Layout layout = ((EditText) v).getLayout();
                    float x = event.getX() + messageEditText.getScrollX();
                    float y = event.getY() + messageEditText.getScrollY();
                    int line = layout.getLineForVertical((int) y);

                    int offset = layout.getOffsetForHorizontal(line, x);
                    if (offset > 0)
                        if (x > layout.getLineMax(0))
                            messageEditText
                                    .setSelection(offset);     // touch was at end of text
                        else
                            messageEditText.setSelection(offset - 1);
                }
                return true;
            }
        };

        /**
         * If User clicks outside the Edittext block
         * then hide keyboard
         */
        messageEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (v.getId() == R.id.messageEditText) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_UP:
                            v.getParent().requestDisallowInterceptTouchEvent(false);
                            break;
                    }
                }
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        /**
         * A selected language still owns the EditText after coming back from
         * the background, so restore its keyboard rather than dropping the
         * user onto the system one.
         */
        if (switchLang.getSelectedItemPosition() != 0) {
            showCustomKeyboard();
        } else {
            mKeyboardView.setVisibility(View.GONE);
        }
    }

    public void selectKeyboard() {
        int position = switchLang.getSelectedItemPosition();

        if (position <= 0 || position >= LAYOUT_FOR_POSITION.length) {
            mKeyboardView.setVisibility(View.GONE);

            //Show Default Keyboard
            showSystemKeyboard();
            return;
        }

        /**
         * Without the font the keys draw as tofu and the keyboard is unusable,
         * so hand back to the device's own rather than show it.
         */
        if (!canRenderScript(position)) {
            showUnsupportedAlert(LANGUAGE_NAME[position]);

            //Reset language selection
            switchLang.setSelection(0);
            mKeyboardView.setVisibility(View.GONE);

            //Show Default Keyboard
            showSystemKeyboard();
            return;
        }

        hideSoftKeyboard(MainActivity.this);
        focusForCustomKeyboard();

        showKeyboardWithAnimation();
        mKeyboardView.setVisibility(View.VISIBLE);
        mKeyboardView.setKeyboard(keyboardFor(LAYOUT_FOR_POSITION[position]));

        messageEditText.setOnTouchListener(customKeyboardTouchListener);
    }

    /**
     * Whether the device can actually draw this script. Paint.hasGlyph() checks
     * the font directly, so unlike drawing to a bitmap and looking for pixels it
     * can tell a real letter from the tofu box drawn in its place.
     */
    private boolean canRenderScript(int position) {
        String probe = PROBE_CHAR[position];
        return probe == null || glyphProbe.hasGlyph(probe);
    }

    private void showUnsupportedAlert(String language) {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(R.string.app_name)
                .setMessage(language + " keyboard is not supported by your device")
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    /**
     * Building a Keyboard means parsing its XML and allocating every Key in it,
     * and the layer toggles do that on a keypress with the user waiting. The
     * layouts never change, so each one is built at most once. Key sizes are
     * computed from the display width at parse time, but a configuration change
     * recreates the Activity and with it this cache.
     */
    private Keyboard keyboardFor(int xmlResId) {
        Keyboard keyboard = keyboardCache.get(xmlResId);
        if (keyboard == null) {
            keyboard = new Keyboard(MainActivity.this, xmlResId);
            keyboardCache.put(xmlResId, keyboard);
        }
        return keyboard;
    }

    /**
     * The custom keyboard drives the EditText, so keep the system keyboard from
     * popping up on focus, but still give the EditText focus ourselves. Without
     * focus the caret is never drawn and dispatchKeyEvent() has no target for
     * Enter/Backspace.
     */
    private void focusForCustomKeyboard() {
        /**
         * setShowSoftInputOnFocus() only covers focus changes. The window
         * restores the system keyboard on its own when the activity returns to
         * the foreground with an editor focused, so it has to be told to stay
         * hidden as well.
         */
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        messageEditText.setShowSoftInputOnFocus(false);
        messageEditText.requestFocus();
        messageEditText.setCursorVisible(true);
    }

    /**
     * Hands the EditText back to the system keyboard, undoing everything
     * focusForCustomKeyboard() put in place.
     */
    private void showSystemKeyboard() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED);

        messageEditText.setShowSoftInputOnFocus(true);
        InputMethodManager imm =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(messageEditText, 0);
        messageEditText.setOnTouchListener(null);
    }

    /**
     * Brings the already selected keyboard back, either after a touch or when
     * returning from the background. Unlike selectKeyboard() this re-parses
     * nothing, so it is cheap enough to run from a touch handler.
     */
    private void showCustomKeyboard() {
        hideSoftKeyboard(MainActivity.this);
        focusForCustomKeyboard();

        showKeyboardWithAnimation();
        mKeyboardView.setVisibility(View.VISIBLE);
    }

    private void showKeyboardWithAnimation() {
        if (mKeyboardView.getVisibility() == View.GONE) {
            Animation animation = AnimationUtils
                    .loadAnimation(MainActivity.this,
                            R.anim.slide_from_bottom);
            mKeyboardView.showWithAnimation(animation);
        }
    }


    public class BasicOnKeyboardActionListener implements KeyboardView.OnKeyboardActionListener {

        @Override
        public void swipeUp() {
            /* no-op */
        }

        @Override
        public void swipeRight() {
            /* no-op */
        }

        @Override
        public void swipeLeft() {
            /* no-op */
        }

        @Override
        public void swipeDown() {
            /* no-op */
        }

        @Override
        public void onText(CharSequence text) {
            /*
             * Editable.replace() edits in place. Rebuilding the message as a
             * String and handing it back through setText() copied the whole
             * thing twice per keystroke and discarded the Editable, and the
             * old caret arithmetic assumed every key emits one character.
             */
            Editable content = messageEditText.getText();
            int start = messageEditText.getSelectionStart();
            int end = messageEditText.getSelectionEnd();

            if (start < 0 || end < 0) {         // nothing focused yet, append
                start = end = content.length();
            } else if (start > end) {           // selected right to left
                int swap = start;
                start = end;
                end = swap;
            }

            content.replace(start, end, text);
            messageEditText.setSelection(start + text.length());
        }

        @Override
        public void onRelease(int primaryCode) {
            /* no-op */
        }

        @Override
        public void onPress(int primaryCode) {
            /* no-op */
        }

        @Override
        public void onKey(int primaryCode, int[] keyCodes) {
            switch (primaryCode) {
                case 66:
                case 67:
                    long eventTime = System.currentTimeMillis();
                    KeyEvent event =
                            new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, primaryCode, 0, 0, 0, 0,
                                    KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE);

                    MainActivity.this.dispatchKeyEvent(event);
                    break;
                default:
                    int layout = LAYER_FOR_KEYCODE.get(primaryCode);
                    if (layout != 0) {
                        mKeyboardView.setKeyboard(keyboardFor(layout));
                    }
                    break;
            }
        }
    }
}
