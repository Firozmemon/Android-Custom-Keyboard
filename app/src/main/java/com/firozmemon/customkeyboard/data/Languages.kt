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

package com.firozmemon.customkeyboard.data

import com.firozmemon.customkeyboard.model.Language

/** One row per entry in the language menu after Default, in menu order. */
internal val LANGUAGES = listOf(
    Language("Hindi", HINDI_MAIN, HINDI_ALT, "क"),        // U+0915 Devanagari ka
    Language("Gujarati", GUJARATI_MAIN, GUJARATI_ALT, "ક"),  // U+0A95 Gujarati ka
    Language("Marathi", MARATHI_MAIN, MARATHI_ALT, "क"),    // U+0915 Devanagari ka
    Language("Kannada", KANNADA_MAIN, KANNADA_ALT, "ಕ"),    // U+0C95 Kannada ka
    Language("Tamil", TAMIL_MAIN, TAMIL_ALT, "க"),          // U+0B95 Tamil ka
    Language("Punjabi", PUNJABI_MAIN, PUNJABI_ALT, "ਕ"),    // U+0A15 Gurmukhi ka
)
