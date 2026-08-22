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

import com.firozmemon.customkeyboard.model.Key
import com.firozmemon.customkeyboard.model.KeyAction
import com.firozmemon.customkeyboard.model.KeyboardLayer
import com.firozmemon.customkeyboard.model.WIDE_KEY_WIDTH

/**
 * The key grids, one pair of layers per language.
 *
 * These were res/xml/kbd_*.xml until the UI moved to Compose. The old
 * <Keyboard> format was read by android.inputmethodservice.Keyboard, which
 * went away with KeyboardView; as Kotlin the rows cost no XML parsing, are
 * checked by the compiler, and read as the grid they describe.
 *
 * Every row adds up to one full width, so a key's fraction doubles as its
 * layout weight.
 */

/** One key per character, each emitting itself. */
private fun letters(labels: String): List<Key> = labels.map { Key(it.toString()) }

/** Switches to the language's other layer - letters <-> digits and marks. */
private fun toggle(label: String): List<Key> = listOf(Key(label, KeyAction.ToggleLayer))

private fun backspace(): List<Key> =
    listOf(Key("\u2190", KeyAction.Backspace, repeatable = true))

private fun enter(): List<Key> = listOf(Key("\u21B5", KeyAction.Newline))

private fun space(): List<Key> =
    listOf(Key(" ", widthFraction = WIDE_KEY_WIDTH, repeatable = true))

/** A letter given twice the usual room, for glyphs that need it. */
private fun wide(label: String): List<Key> = listOf(Key(label, widthFraction = WIDE_KEY_WIDTH))

internal val HINDI_MAIN = KeyboardLayer(
    letters("कखगघङचछजझञ"),
    letters("टठडढणतथदधन"),
    letters("पफबभमयरलवश"),
    toggle("⇧") + letters("ािीुूेैो") + backspace(),
    letters("ौंःॅ") + space() + letters("्।") + toggle("१२३") + enter(),
)

internal val HINDI_ALT = KeyboardLayer(
    letters("१२३४५६७८९०"),
    letters("अआइईउऊएऐओऔ"),
    letters("ऋषसहँृ़?.-"),
    toggle("⇧") + letters("ािीुूेैो") + backspace(),
    letters("ौंःॅ") + space() + letters("्।") + toggle("कख") + enter(),
)

internal val GUJARATI_MAIN = KeyboardLayer(
    letters("કખગઘઙચછજઝઞ"),
    letters("ટઠડઢણતથદધન"),
    letters("પફબભમયરલવશ"),
    toggle("⇧") + letters("ાિીુૂેૈો") + backspace(),
    letters("ૌંઃૅ") + space() + letters("ૉ્") + toggle("૧૨૩") + enter(),
)

internal val GUJARATI_ALT = KeyboardLayer(
    letters("૧૨૩૪૫૬૭૮૯૦"),
    letters("અઆઇઈઉઊએઐઓઔ"),
    letters("ઋષસહૐઁ઼?.-"),
    toggle("⇧") + letters("ાિીુૂેૈો") + backspace(),
    letters("ૌંઃૅ") + space() + letters("ૉ્") + toggle("કખ") + enter(),
)

internal val MARATHI_MAIN = KeyboardLayer(
    letters("कखगघङचछजझञ"),
    letters("टठडढणतथदधन"),
    letters("पफबभमयरलवश"),
    toggle("⇧") + letters("ािीुूेैो") + backspace(),
    letters("ौंःॅ") + space() + letters("्ळ") + toggle("१२३") + enter(),
)

internal val MARATHI_ALT = KeyboardLayer(
    letters("१२३४५६७८९०"),
    letters("अआइईउऊएऐओऔ"),
    letters("ऋषसहँृ़?.-"),
    toggle("⇧") + letters("ािीुूेैो") + backspace(),
    letters("ौंःॅ") + space() + letters("्ळ") + toggle("कख") + enter(),
)

internal val KANNADA_MAIN = KeyboardLayer(
    letters("ಕಖಗಘಙಚಛಜಝಞ"),
    letters("ಟಠಡಢಣತಥದಧನ"),
    letters("ಪಫಬಭಮಯರಲವಶ"),
    toggle("⇧") + letters("ಾಿೀುೂೆೇೈ") + backspace(),
    letters("ಂಃೊೋ") + space() + letters("ೌ್") + toggle("೧೨೩") + enter(),
)

internal val KANNADA_ALT = KeyboardLayer(
    letters("೧೨೩೪೫೬೭೮೯೦"),
    letters("ಅಆಇಈಉಊಎಏಐಒ"),
    letters("ಓಔಷಸಹಳಋೠ?."),
    toggle("⇧") + letters("ಾಿೀುೂೆೇೈ") + backspace(),
    letters("ಂಃೊೋ") + space() + letters("ೌ್") + toggle("ಕಖ") + enter(),
)

internal val TAMIL_MAIN = KeyboardLayer(
    letters("கதலஙநவசபழஞ"),
    letters("மளடயறணரனஹஜ"),
    letters("ஸஷ்+*_=?.-"),
    toggle("⇧") + letters("ாிீுூெேை") + backspace(),
    wide("ொ") + wide("ோ") + space() + wide("ௌ") + toggle("௧௨௩") + enter(),
)

internal val TAMIL_ALT = KeyboardLayer(
    letters("௧௨௩௪௫௬௭௮௯௰"),
    letters("அஆஇஈஉஊஎஏஐஒ"),
    letters("ஓஔ்+*_=?.-"),
    toggle("⇧") + letters("ாிீுூெேை") + backspace(),
    wide("ொ") + wide("ோ") + space() + wide("ௌ") + toggle("கத") + enter(),
)

internal val PUNJABI_MAIN = KeyboardLayer(
    letters("ਕਖਗਘਙਚਛਜਝਞ"),
    letters("ਟਠਡਢਣਤਥਦਧਨ"),
    letters("ਪਫਬਭਮਯਰਲਵਲ਼"),
    toggle("⇧") + letters("ਾਿੀੁੂੋੌੇ") + backspace(),
    letters("ੈਂ਼ੱ") + space() + letters("੍॥") + toggle("੧੨੩") + enter(),
)

internal val PUNJABI_ALT = KeyboardLayer(
    letters("੧੨੩੪੫੬੭੮੯੦"),
    letters("ਅਆਇਈਉਊਏਐਓਔ"),
    letters("ਸ਼ਸਹੜੰ+/?.-"),
    toggle("⇧") + letters("ਾਿੀੁੂੋੌੇ") + backspace(),
    letters("ੈਂ਼ੱ") + space() + letters("੍॥") + toggle("ਕਖ") + enter(),
)
