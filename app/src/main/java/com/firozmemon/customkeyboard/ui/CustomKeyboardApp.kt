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

package com.firozmemon.customkeyboard.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.firozmemon.customkeyboard.ui.keyboard.KeyboardScreen
import com.firozmemon.customkeyboard.ui.theme.CustomKeyboardTheme

/** Wraps the screen so previews and tests get the app's colours. */
@Composable
internal fun CustomKeyboardApp() {
    // The scaffold paints the background the Surface here used to.
    CustomKeyboardTheme { KeyboardScreen() }
}

@Preview(name = "Screen", showBackground = true, heightDp = 720)
@Preview(name = "Screen, dark", showBackground = true, heightDp = 720, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun ScreenPreview() = CustomKeyboardApp()
