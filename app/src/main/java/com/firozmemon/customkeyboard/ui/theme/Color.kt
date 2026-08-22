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

package com.firozmemon.customkeyboard.ui.theme

import androidx.compose.ui.graphics.Color

/** The indigo and accent the app has always used, formerly in colors.xml. */
internal val Indigo = Color(0xFF3F51B5)
internal val DeepBlue = Color(0xFF000081)

/** Light enough to read against a dark surface, which the originals are not. */
internal val IndigoLight = Color(0xFF9FA8DA)
internal val BlueLight = Color(0xFFB0C4FF)

/**
 * The action bar's own pair, because no slot in the M3 schemes holds it. The
 * View theme put white on colorPrimary and could, since the window never went
 * dark; primary here has to stay legible against a dark surface too, which is
 * why the dark scheme lightens it. So the bar keeps the original indigo
 * itself, and reads the same in either theme.
 */
internal val TopBarContainer = Indigo
internal val TopBarContent = Color.White
