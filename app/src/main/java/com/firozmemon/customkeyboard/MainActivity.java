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
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.text.Editable;
import android.text.Layout;
import android.util.SparseArray;
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
        mKeyboardView.setOnKeyboardActionListener(new BasicOnKeyboardActionListener(
                MainActivity.this,
                messageEditText,
                mKeyboardView));

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

        hideSoftKeyboard(MainActivity.this);
        focusForCustomKeyboard();

        showKeyboardWithAnimation();
        mKeyboardView.setVisibility(View.VISIBLE);
        mKeyboardView.setKeyboard(keyboardFor(LAYOUT_FOR_POSITION[position]));

        messageEditText.setOnTouchListener(customKeyboardTouchListener);
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

        EditText editText;
        CustomKeyboardView displayKeyboardView;
        private Activity mTargetActivity;

        public BasicOnKeyboardActionListener(Activity targetActivity, EditText editText,
                                             CustomKeyboardView
                                                     displayKeyboardView) {
            mTargetActivity = targetActivity;
            this.editText = editText;
            this.displayKeyboardView = displayKeyboardView;
        }

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
            Editable content = editText.getText();
            int start = editText.getSelectionStart();
            int end = editText.getSelectionEnd();

            if (start < 0 || end < 0) {         // nothing focused yet, append
                start = end = content.length();
            } else if (start > end) {           // selected right to left
                int swap = start;
                start = end;
                end = swap;
            }

            content.replace(start, end, text);
            editText.setSelection(start + text.length());
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

                    mTargetActivity.dispatchKeyEvent(event);
                    break;
                case -106:
                    displayKeyboardView
                            .setKeyboard(keyboardFor(R.xml.kbd_hin2));
                    break;
                case -107:
                    displayKeyboardView
                            .setKeyboard(keyboardFor(R.xml.kbd_hin1));
                    break;
                case -108:
                    displayKeyboardView
                            .setKeyboard(keyboardFor(R.xml.kbd_guj2));
                    break;
                case -109:
                    displayKeyboardView.setKeyboard(keyboardFor(R.xml.kbd_guj1));
                    break;
                case -110:
                    displayKeyboardView.setKeyboard(keyboardFor(R.xml.kbd_mar2));
                    break;
                case -111:
                    displayKeyboardView.setKeyboard(keyboardFor(R.xml.kbd_mar1));
                    break;
                case -112:
                    displayKeyboardView.setKeyboard(keyboardFor(R.xml.kbd_knd2));
                    break;
                case -113:
                    displayKeyboardView.setKeyboard(keyboardFor(R.xml.kbd_knd1));
                    break;
                case -114:
                    displayKeyboardView.setKeyboard(keyboardFor(R.xml.kbd_tam2));
                    break;
                case -115:
                    displayKeyboardView.setKeyboard(keyboardFor(R.xml.kbd_tam1));
                    break;
                case -116:
                    displayKeyboardView.setKeyboard(keyboardFor(R.xml.kbd_punj2));
                    break;
                case -117:
                    displayKeyboardView.setKeyboard(keyboardFor(R.xml.kbd_punj1));
                    break;
                default:
                    break;
            }
        }
    }
}
