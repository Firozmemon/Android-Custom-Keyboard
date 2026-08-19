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

import android.graphics.Paint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.InterceptPlatformTextInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.PlatformTextInputInterceptor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.awaitCancellation

/** Menu position of the device's own keyboard, which has no [Language]. */
private const val DEFAULT_KEYBOARD = 0

internal const val MESSAGE_FIELD_TAG = "messageField"
internal const val LANGUAGE_PICKER_TAG = "languagePicker"

/**
 * The message field, the language menu, and the keyboard itself.
 *
 * Two things the View version had to arrange by hand are gone. It turned the
 * system keyboard off with showSoftInputOnFocus and had to put the caret where
 * the finger landed itself, because its touch listener swallowed the event
 * before the EditText could; a text field that never asks for the IME goes on
 * handling its own taps. And it restored the custom keyboard in onResume,
 * where here the state simply outlives the pause.
 */
@Composable
internal fun KeyboardScreen(modifier: Modifier = Modifier) {
    val message = rememberTextFieldState()
    var selected by rememberSaveable { mutableIntStateOf(DEFAULT_KEYBOARD) }
    var showingAlt by rememberSaveable { mutableStateOf(false) }
    var unsupported by rememberSaveable { mutableStateOf<String?>(null) }

    val language = LANGUAGES.getOrNull(selected - 1)
    val glyphProbe = remember { Paint() }
    val softwareKeyboard = LocalSoftwareKeyboardController.current
    val messageFocus = remember { FocusRequester() }

    /*
     * Runs after the field has been told to stop asking for the IME, so the
     * two keyboards are never up at once. A no-op on the first composition,
     * when nothing is selected yet.
     */
    LaunchedEffect(language) {
        if (language != null) {
            softwareKeyboard?.hide()
            messageFocus.requestFocus()
        }
    }

    Column(modifier.fillMaxSize().imePadding()) {
        Column(
            Modifier
                .weight(1f)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 16.dp),
        ) {
            /*
             * Turns the system IME off at the source while a language is
             * selected. The field goes on running its own gestures, so taps
             * still move the caret and drags still select - it is only the
             * request for a keyboard that is dropped.
             *
             * KeyboardOptions.showKeyboardOnFocus is not enough on its own:
             * it covers the focus the field gains, and a tap on a field that
             * already has focus asks for the IME in its own right.
             */
            SuppressSystemKeyboard(while_ = language != null) {
                OutlinedTextField(
                    state = message,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 15.dp)
                        .focusRequester(messageFocus)
                        .testTag(MESSAGE_FIELD_TAG),
                    label = { Text(stringResource(R.string.compose_message_hint)) },
                    lineLimits = TextFieldLineLimits.MultiLine(
                        minHeightInLines = 5,
                        maxHeightInLines = 5,
                    ),
                )
            }

            /*
             * The View layout put a TextView above a bare Spinner to name it.
             * The picker carries its own floating label, so that caption would
             * only say the same thing twice.
             */
            LanguagePicker(
                selected = selected,
                onSelect = { position ->
                    val picked = LANGUAGES.getOrNull(position - 1)

                    /*
                     * Without the font the keys draw as tofu and the keyboard
                     * is unusable, so hand back to the device's own rather
                     * than show it.
                     */
                    if (picked != null && !glyphProbe.hasGlyph(picked.probeChar)) {
                        unsupported = picked.name
                        selected = DEFAULT_KEYBOARD
                    } else {
                        selected = position
                        showingAlt = false
                    }
                },
                modifier = Modifier.padding(top = 16.dp),
            )
        }

        AnimatedVisibility(
            visible = language != null,
            enter = slideInVertically(tween(ANIMATION_MS)) { it } + fadeIn(tween(ANIMATION_MS)),
            exit = slideOutVertically(tween(ANIMATION_MS)) { it } + fadeOut(tween(ANIMATION_MS)),
        ) {
            /*
             * Held so the keys stay on screen while the exit animation runs,
             * rather than blanking the moment the selection clears.
             */
            val showing = remember(language, showingAlt) {
                language?.let { if (showingAlt) it.alt else it.main }
            }

            if (showing != null) {
                CustomKeyboard(
                    layer = showing,
                    onKey = { key ->
                        if (key.action == KeyAction.ToggleLayer) {
                            showingAlt = !showingAlt
                        } else {
                            message.press(key)
                        }
                    },
                )
            }
        }
    }

    unsupported?.let { name ->
        AlertDialog(
            onDismissRequest = { unsupported = null },
            title = { Text(stringResource(R.string.app_name)) },
            text = { Text(stringResource(R.string.unsupported_keyboard, name)) },
            confirmButton = {
                TextButton(onClick = { unsupported = null }) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            // the dialog was setCancelable(false), so it takes an acknowledgement
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
            ),
        )
    }
}

/** Matches slide_from_bottom.xml, which translated and faded over 200ms. */
private const val ANIMATION_MS = 200

/**
 * Withholds the platform keyboard from [content] while [while_] holds.
 *
 * Interception is the hook the platform offers for exactly this - an app that
 * brings its own keys - and it sits below every route to the IME rather than
 * beside one of them. The session is left waiting instead of started; flipping
 * [while_] back rebuilds the interceptor, and the IME returns with it.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun SuppressSystemKeyboard(while_: Boolean, content: @Composable () -> Unit) {
    // Rebuilt only when the answer changes, so typing does not restart the session.
    val interceptor = remember(while_) {
        PlatformTextInputInterceptor { request, nextHandler ->
            if (while_) awaitCancellation() else nextHandler.startInputMethod(request)
        }
    }

    InterceptPlatformTextInput(interceptor = interceptor, content = content)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguagePicker(
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val names = listOf(stringResource(R.string.default_keyboard)) + LANGUAGES.map { it.name }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = names[selected],
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.select_keyboard)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
                .testTag(LANGUAGE_PICKER_TAG),
        )

        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            names.forEachIndexed { position, name ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        expanded = false
                        onSelect(position)
                    },
                )
            }
        }
    }
}

/** Wraps the screen so previews and tests get the app's colours. */
@Composable
internal fun CustomKeyboardApp() {
    CustomKeyboardTheme {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            KeyboardScreen()
        }
    }
}
