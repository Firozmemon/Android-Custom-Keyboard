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
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

/**
 * Created by firoz
 */
public class MainActivity extends AppCompatActivity {

    TextInputLayout messageInputLayout;
    EditText messageEditText;
    Spinner switchLang;
    CustomKeyboardView mKeyboardView;

    private Keyboard mKeyboard;
    private ArrayAdapter<CharSequence> langAdapter;

    public void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(messageEditText.getWindowToken(), 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        messageInputLayout = findViewById(R.id.messageInputLayout);
        messageEditText = findViewById(R.id.messageEditText);
        switchLang = findViewById(R.id.switchLang);
        mKeyboardView = findViewById(R.id.keyboardView);

        langAdapter = ArrayAdapter.createFromResource(this, R.array.switchLangArr, android.R.layout.simple_spinner_item);
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
        mKeyboardView.setVisibility(View.GONE);
    }

    public void selectKeyboard() {

        // Do not show the preview balloons
        mKeyboardView.setPreviewEnabled(false);

        if (switchLang.getSelectedItemPosition() != 0) {
            hideSoftKeyboard(MainActivity.this);

            if (switchLang.getSelectedItemPosition() == 1) {
                mKeyboard = new Keyboard(MainActivity.this, R.xml
                        .kbd_hin1);
                showKeyboardWithAnimation();
                mKeyboardView.setVisibility(View.VISIBLE);
                mKeyboardView.setKeyboard(mKeyboard);

            } else if (switchLang.getSelectedItemPosition() == 2) {
                if (Util.isLangSupported(MainActivity.this, "ગુજરાતી")) {
                    mKeyboard = new Keyboard(MainActivity.this, R.xml.kbd_guj1);
                    showKeyboardWithAnimation();
                    mKeyboardView.setVisibility(View.VISIBLE);
                    mKeyboardView.setKeyboard(mKeyboard);
                } else {
                    Util.displayAlert(MainActivity.this, getResources().getString(R.string.app_name), "Gujarati keyboard is not supported "
                            + "by your device");
                    //Reset language selection
                    switchLang.setSelection(0);
                    mKeyboard = new Keyboard(MainActivity.this, R.xml
                            .kbd_hin1);
                    mKeyboardView.setVisibility(View.GONE);

                    //Show Default Keyboard
                    InputMethodManager imm =
                            (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(messageEditText, 0);
                    messageEditText.setOnTouchListener(null);
                }
            } else if (switchLang.getSelectedItemPosition() == 3) {
                mKeyboard =
                        new Keyboard(MainActivity.this, R.xml.kbd_mar1);
                showKeyboardWithAnimation();
                mKeyboardView.setVisibility(View.VISIBLE);
                mKeyboardView.setKeyboard(mKeyboard);
            } else if (switchLang.getSelectedItemPosition() == 4) {
                mKeyboard =
                        new Keyboard(MainActivity.this, R.xml.kbd_knd1);
                showKeyboardWithAnimation();
                mKeyboardView.setVisibility(View.VISIBLE);
                mKeyboardView.setKeyboard(mKeyboard);
            } else if (switchLang.getSelectedItemPosition() == 5) {
                mKeyboard = new Keyboard(MainActivity.this, R.xml
                        .kbd_tam1);
                showKeyboardWithAnimation();
                mKeyboardView.setVisibility(View.VISIBLE);
                mKeyboardView.setKeyboard(mKeyboard);
            } else if (switchLang.getSelectedItemPosition() == 6) {
                if (Util.isLangSupported(MainActivity.this, "ਪੰਜਾਬੀ ਦੇ")) {

                    mKeyboard = new Keyboard(MainActivity.this, R.xml
                            .kbd_punj1);
                    showKeyboardWithAnimation();
                    mKeyboardView.setVisibility(View.VISIBLE);
                    mKeyboardView.setKeyboard(mKeyboard);
                } else {
                    Util.displayAlert(MainActivity.this, getResources().getString(R.string.app_name), "Punjabi keyboard is not supported "
                            + "by your device");

                    //Reset language selection
                    switchLang.setSelection(0);
                    mKeyboard = new Keyboard(MainActivity.this, R.xml
                            .kbd_hin1);
                    mKeyboardView.setVisibility(View.GONE);

                    //Show Default Keyboard
                    InputMethodManager imm =
                            (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(messageEditText, 0);
                    messageEditText.setOnTouchListener(null);
                }
            }

            mKeyboardView.setOnKeyboardActionListener(new BasicOnKeyboardActionListener(
                    MainActivity.this,
                    messageEditText,
                    mKeyboardView));

            messageEditText.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    selectKeyboard();

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_UP:
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

                            messageEditText.setCursorVisible(true);
                            break;
                    }
                    return true;
                }
            });
        } else {
            mKeyboardView.setVisibility(View.GONE);

            //Show Default Keyboard
            InputMethodManager imm =
                    (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(messageEditText, 0);
            messageEditText.setOnTouchListener(null);
        }
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
            int cursorPosition = editText.getSelectionEnd();
            String previousText = editText.getText().toString();
            String before, after;
            if (cursorPosition < previousText.length()) {
                before = previousText.substring(0, cursorPosition);
                after = previousText.substring(cursorPosition);
            } else {
                before = previousText;
                after = "";
            }
            editText.setText(before + text + after);
            editText.setSelection(cursorPosition + 1);
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
                            .setKeyboard(new Keyboard(mTargetActivity, R.xml.kbd_hin2));
                    break;
                case -107:
                    displayKeyboardView
                            .setKeyboard(new Keyboard(mTargetActivity, R.xml.kbd_hin1));
                    break;
                case -108:
                    displayKeyboardView
                            .setKeyboard(new Keyboard(mTargetActivity, R.xml.kbd_guj2));
                    break;
                case -109:
                    displayKeyboardView.setKeyboard(new Keyboard(mTargetActivity, R.xml.kbd_guj1));
                    break;
                case -110:
                    displayKeyboardView.setKeyboard(new Keyboard(mTargetActivity, R.xml.kbd_mar2));
                    break;
                case -111:
                    displayKeyboardView.setKeyboard(new Keyboard(mTargetActivity, R.xml.kbd_mar1));
                    break;
                case -112:
                    displayKeyboardView.setKeyboard(new Keyboard(mTargetActivity, R.xml.kbd_knd2));
                    break;
                case -113:
                    displayKeyboardView.setKeyboard(new Keyboard(mTargetActivity, R.xml.kbd_knd1));
                    break;
                case -114:
                    displayKeyboardView.setKeyboard(new Keyboard(mTargetActivity, R.xml.kbd_tam2));
                    break;
                case -115:
                    displayKeyboardView.setKeyboard(new Keyboard(mTargetActivity, R.xml.kbd_tam1));
                    break;
                case -116:
                    displayKeyboardView.setKeyboard(new Keyboard(mTargetActivity, R.xml.kbd_punj2));
                    break;
                case -117:
                    displayKeyboardView.setKeyboard(new Keyboard(mTargetActivity, R.xml.kbd_punj1));
                    break;
                default:
                    break;
            }
        }
    }
}
