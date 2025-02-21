package com.spendubaya.exam_spendubaya

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class MyAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            performGlobalAction(GLOBAL_ACTION_BACK) // Blokir tombol Back
            performGlobalAction(GLOBAL_ACTION_RECENTS) // Blokir Recent Apps
            performGlobalAction(GLOBAL_ACTION_HOME) // Blokir Home
        }
    }

    override fun onInterrupt() {}
}
