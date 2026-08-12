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

import android.util.SparseIntArray

/**
 * Everything that varies between the keyboards, in one row each.
 *
 * @param probeChar a single base letter of the script, used to check the
 *   device can draw it. Deliberately one character: hasGlyph() asks whether
 *   the whole string maps to one glyph, so a word answers false even where
 *   the script renders perfectly well. Checking the font directly is what
 *   distinguishes a real letter from the tofu box drawn in its place.
 */
internal data class Language(
    val name: String,
    val mainLayout: Int,
    val altLayout: Int,
    val toAltKeyCode: Int,
    val toMainKeyCode: Int,
    val probeChar: String,
)

/**
 * One row per spinner position after Default, in the order
 * R.array.switchLangArr lists them.
 */
internal val LANGUAGES = listOf(
    Language("Hindi", R.xml.kbd_hin1, R.xml.kbd_hin2, -106, -107, "क"),   // U+0915 Devanagari ka
    Language("Gujarati", R.xml.kbd_guj1, R.xml.kbd_guj2, -108, -109, "ક"),   // U+0A95 Gujarati ka
    Language("Marathi", R.xml.kbd_mar1, R.xml.kbd_mar2, -110, -111, "क"),   // U+0915 Devanagari ka
    Language("Kannada", R.xml.kbd_knd1, R.xml.kbd_knd2, -112, -113, "ಕ"),   // U+0C95 Kannada ka
    Language("Tamil", R.xml.kbd_tam1, R.xml.kbd_tam2, -114, -115, "க"),   // U+0B95 Tamil ka
    Language("Punjabi", R.xml.kbd_punj1, R.xml.kbd_punj2, -116, -117, "ਕ"),   // U+0A15 Gurmukhi ka
)

/**
 * Derived so the toggle keys cannot drift out of step with the layouts they
 * switch between.
 */
internal val LAYER_FOR_KEYCODE = SparseIntArray().apply {
    LANGUAGES.forEach { language ->
        put(language.toAltKeyCode, language.altLayout)
        put(language.toMainKeyCode, language.mainLayout)
    }
}
