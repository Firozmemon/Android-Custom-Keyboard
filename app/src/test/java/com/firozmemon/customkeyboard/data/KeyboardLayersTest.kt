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

import com.firozmemon.customkeyboard.model.KeyAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The layouts came over from XML in bulk, and a key's width is now also its
 * layout weight. These are the properties that made the conversion safe, held
 * for whatever gets edited into the tables next.
 */
class KeyboardLayersTest {

    private val layers = LANGUAGES.flatMap { listOf(it.name to it.main, it.name to it.alt) }

    @Test
    fun everyLanguageHasTwoLayersOfFiveRows() {
        assertEquals(6, LANGUAGES.size)
        layers.forEach { (name, layer) ->
            assertEquals("$name has the wrong number of rows", 5, layer.rows.size)
        }
    }

    /**
     * A row's keys are laid out by weight, so this is what keeps a row filling
     * its width exactly rather than being scaled to fit.
     */
    @Test
    fun everyRowAddsUpToOneFullWidth() {
        layers.forEach { (name, layer) ->
            layer.rows.forEachIndexed { index, row ->
                val width = row.sumOf { it.widthFraction.toDouble() }
                assertEquals("$name row $index", 1.0, width, 0.0001)
            }
        }
    }

    /**
     * Both layers need a way out, or a language can be entered and not left.
     */
    @Test
    fun everyLayerCanBeSwitchedAwayFrom() {
        layers.forEach { (name, layer) ->
            val toggles = layer.rows.flatten().count { it.action == KeyAction.ToggleLayer }
            assertTrue("$name has no layer toggle", toggles > 0)
        }
    }

    @Test
    fun everyLayerCanTypeAndCorrect() {
        layers.forEach { (name, layer) ->
            val keys = layer.rows.flatten()
            assertTrue("$name cannot type", keys.any { it.action == KeyAction.Insert })
            assertTrue("$name has no backspace", keys.any { it.action == KeyAction.Backspace })
            assertTrue("$name has no enter", keys.any { it.action == KeyAction.Newline })
        }
    }

    /** Held keys repeat, and only the two that always did should. */
    @Test
    fun onlySpaceAndBackspaceRepeat() {
        layers.forEach { (name, layer) ->
            layer.rows.flatten().filter { it.repeatable }.forEach { key ->
                val isSpace = key.action == KeyAction.Insert && key.label.isBlank()
                assertTrue(
                    "$name repeats ${key.label} (${key.action})",
                    isSpace || key.action == KeyAction.Backspace,
                )
            }
        }
    }

    /** The probe is a single character on purpose - hasGlyph() needs one. */
    @Test
    fun everyLanguageProbesWithOneCharacter() {
        LANGUAGES.forEach { language ->
            assertEquals(language.name, 1, language.probeChar.length)
        }
    }
}
