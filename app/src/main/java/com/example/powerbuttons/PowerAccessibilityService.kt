package com.example.powerbuttons

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

/**
 * Does nothing except let the app ask Android to show the system power menu
 * (Power off / Restart). It ignores all screen events and reads no content.
 */
class PowerAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        instance = this
    }

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null
        return super.onUnbind(intent)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    companion object {
        @Volatile
        var instance: PowerAccessibilityService? = null

        val isRunning: Boolean get() = instance != null

        fun openPowerMenu(): Boolean =
            instance?.performGlobalAction(GLOBAL_ACTION_POWER_DIALOG) ?: false
    }
}
