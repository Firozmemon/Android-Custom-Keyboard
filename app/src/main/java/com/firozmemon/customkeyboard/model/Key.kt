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

package com.firozmemon.customkeyboard.model

import androidx.compose.runtime.Immutable

/** An ordinary key's share of its row: ten to a row. */
internal const val KEY_WIDTH = 0.1f

/** Twice the usual room, for the space bar and a few wide Tamil glyphs. */
internal const val WIDE_KEY_WIDTH = 0.2f

/**
 * What pressing a key does.
 *
 * The layouts used to say this with an int in android:codes - 66 and 67 were
 * the framework key codes for Enter and Delete, and a dozen negative numbers
 * picked out layer switches. Every layer switch turned out to point at the
 * other layer of the same language, so one [ToggleLayer] replaces all twelve.
 */
internal enum class KeyAction {
    /** Types the key's own label; every layout's letters work this way. */
    Insert,
    Backspace,
    Newline,

    /** Flips between the language's two layers - letters and digits/marks. */
    ToggleLayer,
}

@Immutable
internal data class Key(
    val label: String,
    val action: KeyAction = KeyAction.Insert,
    val widthFraction: Float = KEY_WIDTH,

    /** Held down, the key keeps firing. True for space and backspace. */
    val repeatable: Boolean = false,
)
