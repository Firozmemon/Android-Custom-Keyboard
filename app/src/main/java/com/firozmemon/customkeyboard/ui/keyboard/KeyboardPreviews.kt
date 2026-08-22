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

package com.firozmemon.customkeyboard.ui.keyboard

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.firozmemon.customkeyboard.data.HINDI_ALT
import com.firozmemon.customkeyboard.data.HINDI_MAIN
import com.firozmemon.customkeyboard.data.TAMIL_ALT
import com.firozmemon.customkeyboard.data.TAMIL_MAIN
import com.firozmemon.customkeyboard.ui.theme.CustomKeyboardTheme

/**
 * The grids were XML the IDE could not draw, so editing a layout meant
 * installing the app to see it. Now they render here.
 */
@Preview(name = "Hindi letters", showBackground = true)
@Composable
private fun HindiLettersPreview() = CustomKeyboardTheme {
    CustomKeyboard(layer = HINDI_MAIN, onKey = {})
}

@Preview(name = "Hindi digits", showBackground = true)
@Composable
private fun HindiDigitsPreview() = CustomKeyboardTheme {
    CustomKeyboard(layer = HINDI_ALT, onKey = {})
}

@Preview(name = "Tamil letters", showBackground = true)
@Composable
private fun TamilLettersPreview() = CustomKeyboardTheme {
    CustomKeyboard(layer = TAMIL_MAIN, onKey = {})
}

/** The one language whose keys need the extra room, worth an eye on. */
@Preview(name = "Tamil digits", showBackground = true)
@Composable
private fun TamilDigitsPreview() = CustomKeyboardTheme {
    CustomKeyboard(layer = TAMIL_ALT, onKey = {})
}
