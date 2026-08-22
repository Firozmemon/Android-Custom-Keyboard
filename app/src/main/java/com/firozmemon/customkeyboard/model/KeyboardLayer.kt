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
 * One grid of keys. A language has two: the letters it opens on, and the
 * digits and marks behind the shift key.
 */
@Immutable
internal class KeyboardLayer(vararg rows: List<Key>) {
    val rows: List<List<Key>> = rows.toList()
}
