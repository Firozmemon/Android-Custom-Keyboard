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

/**
 * Everything that varies between the keyboards, in one row each.
 *
 * @param probeChar a single base letter of the script, used to check the
 *   device can draw it. Deliberately one character: hasGlyph() asks whether
 *   the whole string maps to one glyph, so a word answers false even where
 *   the script renders perfectly well. Checking the font directly is what
 *   distinguishes a real letter from the tofu box drawn in its place.
 */
@Immutable
internal data class Language(
    val name: String,
    val main: KeyboardLayer,
    val alt: KeyboardLayer,
    val probeChar: String,
)
