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

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.firozmemon.customkeyboard.R
import com.firozmemon.customkeyboard.ui.CustomKeyboardApp
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The screen end to end: picking a language brings its keys up, the keys type
 * into the message, and the shift key swaps the grid underneath.
 */
@RunWith(AndroidJUnit4::class)
class KeyboardScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private fun start() = compose.setContent { CustomKeyboardApp() }

    private fun choose(language: String) {
        compose.onNodeWithTag(LANGUAGE_PICKER_TAG).performClick()
        compose.onNodeWithText(language).performClick()
        compose.waitForIdle()
    }

    /** The key with this label, found the way a screen reader would. */
    private fun press(key: String) {
        compose.onNodeWithContentDescription(key).performClick()
        compose.waitForIdle()
    }

    /**
     * A layer has two of these - the shift key and the digits key - and either
     * does, so take whichever comes first.
     */
    private fun pressFirst(description: Int) {
        val label = InstrumentationRegistry.getInstrumentation()
            .targetContext.getString(description)
        compose.onAllNodesWithContentDescription(label)[0].performClick()
        compose.waitForIdle()
    }

    private fun message(): String =
        compose.onNodeWithTag(MESSAGE_FIELD_TAG)
            .fetchSemanticsNode()
            .config[SemanticsProperties.EditableText]
            .text

    @Test
    fun theBarNamesTheApp() {
        start()

        val name = InstrumentationRegistry.getInstrumentation()
            .targetContext.getString(R.string.app_name)

        compose.onNodeWithTag(TOP_BAR_TAG).assertIsDisplayed()
        compose.onNodeWithText(name).assertIsDisplayed()
    }

    @Test
    fun theKeyboardStaysAwayUntilALanguageIsPicked() {
        start()

        compose.onNodeWithContentDescription("क").assertDoesNotExist()
    }

    @Test
    fun choosingALanguageBringsUpItsKeys() {
        start()

        choose("Hindi")

        compose.onNodeWithContentDescription("क").assertIsDisplayed()
    }

    @Test
    fun goingBackToDefaultPutsTheKeyboardAway() {
        start()
        choose("Hindi")

        choose("Default")

        compose.onNodeWithContentDescription("क").assertDoesNotExist()
    }

    @Test
    fun pressingKeysTypesIntoTheMessage() {
        start()
        choose("Hindi")

        press("क")
        press("ख")

        assertEquals("कख", message())
    }

    @Test
    fun backspaceTakesTheLastCharacterBack() {
        start()
        choose("Hindi")
        press("क")
        press("ख")

        pressFirst(R.string.key_backspace)

        assertEquals("क", message())
    }

    @Test
    fun shiftSwapsTheLettersForDigits() {
        start()
        choose("Hindi")
        compose.onNodeWithContentDescription("क").assertIsDisplayed()

        pressFirst(R.string.key_switch_layer)

        // the other layer opens on the Devanagari digits
        compose.onNodeWithContentDescription("१").assertIsDisplayed()
        compose.onNodeWithContentDescription("क").assertDoesNotExist()
    }

    @Test
    fun typingSurvivesASwitchOfLayer() {
        start()
        choose("Hindi")
        press("क")

        pressFirst(R.string.key_switch_layer)
        press("१")

        assertEquals("क१", message())
    }
}
