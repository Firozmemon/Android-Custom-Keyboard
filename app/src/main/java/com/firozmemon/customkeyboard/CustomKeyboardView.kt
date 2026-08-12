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

import android.content.Context
import android.inputmethodservice.KeyboardView
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation

/**
 * Created by firoz
 */
class CustomKeyboardView(context: Context, attrs: AttributeSet) : KeyboardView(context, attrs) {

    fun showWithAnimation(animation: Animation) {
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                /* no-op */
            }

            override fun onAnimationRepeat(animation: Animation) {
                /* no-op */
            }

            override fun onAnimationEnd(animation: Animation) {
                visibility = View.VISIBLE
            }
        })

        setAnimation(animation)
    }
}
