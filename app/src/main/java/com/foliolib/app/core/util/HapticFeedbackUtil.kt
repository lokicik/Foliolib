package com.foliolib.app.core.util

import android.view.HapticFeedbackConstants
import android.view.View

object HapticFeedbackUtil {
    fun View.performHaptic(
        type: Int = HapticFeedbackConstants.VIRTUAL_KEY,
        ignoreGlobalSetting: Boolean = true
    ) {
        val flags = if (ignoreGlobalSetting) {
            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
        } else {
            0
        }
        performHapticFeedback(type, flags)
    }
}
