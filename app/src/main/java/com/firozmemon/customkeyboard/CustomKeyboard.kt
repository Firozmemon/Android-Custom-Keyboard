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

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** The 45dip the <Keyboard> layouts asked for, kept so the grid looks the same. */
private val KeyHeight = 45.dp

/**
 * The layouts set horizontalGap and verticalGap to zero because KeyboardView's
 * key background was a nine-patch with the gap already inside it. Drawing the
 * keys ourselves, the inset has to be put back - and taking it out of the
 * padding rather than the size keeps the whole cell touchable.
 */
private val KeyInset = 2.dp
private val KeyCorner = 6.dp

/** KeyboardView's own repeat timings, so held keys feel unchanged. */
private const val REPEAT_START_DELAY_MS = 400L
private const val REPEAT_INTERVAL_MS = 50L

/**
 * The key grid, in place of android.inputmethodservice.KeyboardView.
 *
 * Rows are laid out by weight, and because every row's fractions add up to one
 * a key's share of the width is its weight as it stands.
 */
@Composable
internal fun CustomKeyboard(
    layer: KeyboardLayer,
    onKey: (Key) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        // The surface colour runs under the navigation bar; the keys do not.
        Column(Modifier.windowInsetsPadding(WindowInsets.navigationBars)) {
            layer.rows.forEach { row ->
                Row(Modifier.fillMaxWidth().height(KeyHeight)) {
                    row.forEach { key ->
                        KeyButton(
                            key = key,
                            onKey = onKey,
                            modifier = Modifier.weight(key.widthFraction).fillMaxHeight(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KeyButton(key: Key, onKey: (Key) -> Unit, modifier: Modifier) {
    /*
     * Remembered against the key, not just the slot it sits in. Toggling layer
     * swaps a different Key into this position, and the press that did the
     * toggling is the one whose release will never arrive - see below - so a
     * highlight kept per slot would stay lit under the key that replaced it.
     */
    var pressed by remember(key) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val description = key.description()

    /*
     * The gesture handler is keyed on the key, so it outlives the recompositions
     * that change onKey's identity and would otherwise leave it holding a stale
     * one.
     */
    val currentOnKey by rememberUpdatedState(onKey)

    Box(
        modifier
            .padding(KeyInset)
            .clip(RoundedCornerShape(KeyCorner))
            .background(
                if (pressed) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface
            )
            .pointerInput(key) {
                detectTapGestures(
                    /*
                     * KeyboardView sent a repeatable key on the way down and
                     * left it to the repeat timer after that, so a held key
                     * never fired an extra time on release. Acting on press
                     * keeps that, and every other key feels quicker for it.
                     */
                    onPress = {
                        pressed = true
                        currentOnKey(key)

                        val repeat = if (key.repeatable) {
                            scope.launch {
                                delay(REPEAT_START_DELAY_MS)
                                while (true) {
                                    currentOnKey(key)
                                    delay(REPEAT_INTERVAL_MS)
                                }
                            }
                        } else {
                            null
                        }

                        /*
                         * A key that changes the layer replaces itself, which
                         * restarts this handler and cancels the coroutine it
                         * runs in, so tryAwaitRelease() never returns normally.
                         * The cleanup has to happen on the way out either way.
                         */
                        try {
                            tryAwaitRelease()
                        } finally {
                            repeat?.cancel()
                            pressed = false
                        }
                    }
                )
            }
            /*
             * The gesture handler above is invisible to accessibility services,
             * and KeyboardView had its own. This puts the key back on TalkBack's
             * map - reading "space" rather than a blank, and typing on tap.
             */
            .semantics(mergeDescendants = true) {
                this.role = Role.Button
                this.contentDescription = description
                onClick { onKey(key); true }
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = key.label,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
        )
    }
}

/** What a screen reader should call the key, since most labels are the glyph. */
@Composable
private fun Key.description(): String = when (action) {
    KeyAction.Backspace -> stringResource(R.string.key_backspace)
    KeyAction.Newline -> stringResource(R.string.key_enter)
    KeyAction.ToggleLayer -> stringResource(R.string.key_switch_layer)
    KeyAction.Insert -> if (label.isBlank()) stringResource(R.string.key_space) else label
}
