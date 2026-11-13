package com.example.powerbuttons

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import kotlin.concurrent.thread

class MainActivity : Activity() {

    private lateinit var status: TextView
    private lateinit var enableButton: Button
    private var hasRoot = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()
        // Check for root in the background; it is only used if available.
        thread {
            hasRoot = checkRoot()
            runOnUiThread { refreshStatus() }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshStatus()
    }

    // ---------- UI ----------

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(24), dp(48), dp(24), dp(24))
            setBackgroundColor(Color.WHITE)
        }

        root.addView(TextView(this).apply {
            text = "Power Buttons"
            textSize = 28f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
        })

        status = TextView(this).apply {
            textSize = 14f
            setTextColor(Color.DKGRAY)
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, dp(32))
        }
        root.addView(status)

        root.addView(bigButton("Power off", Color.parseColor("#C0392B")) { onPowerOff() })
        root.addView(bigButton("Restart", Color.parseColor("#2471A3")) { onRestart() })

        enableButton = Button(this).apply {
            text = "Enable power menu access"
            setOnClickListener { openAccessibilitySettings() }
        }
        root.addView(enableButton, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dp(24) })

        setContentView(root)
    }

    private fun bigButton(label: String, color: Int, onClick: () -> Unit): Button =
        Button(this).apply {
            text = label
            textSize = 20f
            setTextColor(Color.WHITE)
            setBackgroundColor(color)
            setOnClickListener { onClick() }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(80)
            ).apply { bottomMargin = dp(16) }
        }

    private fun refreshStatus() {
        val serviceOn = PowerAccessibilityService.isRunning
        status.text = when {
            hasRoot -> "Root detected: buttons act directly."
            serviceOn -> "Ready: buttons open your phone's power menu."
            else -> "Turn on power menu access to use the buttons."
        }
        enableButton.visibility =
            if (hasRoot || serviceOn) android.view.View.GONE else android.view.View.VISIBLE
    }

    // ---------- Actions ----------

    private fun onPowerOff() {
        if (hasRoot) {
            confirm("Power off the phone now?") { runRoot("reboot -p") }
        } else {
            openPowerMenuOrExplain()
        }
    }

    private fun onRestart() {
        if (hasRoot) {
            confirm("Restart the phone now?") { runRoot("reboot") }
        } else {
            openPowerMenuOrExplain()
        }
    }

    /** Without root, we show Android's own power menu; the user taps Power off or Restart there. */
    private fun openPowerMenuOrExplain() {
        if (!PowerAccessibilityService.isRunning) {
            Toast.makeText(
                this, "Turn on Power Buttons in Accessibility settings first.", Toast.LENGTH_LONG
            ).show()
            openAccessibilitySettings()
            return
        }
        if (!PowerAccessibilityService.openPowerMenu()) {
            Toast.makeText(this, "Could not open the power menu.", Toast.LENGTH_LONG).show()
        }
    }

    private fun openAccessibilitySettings() {
        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }

    private fun confirm(message: String, onYes: () -> Unit) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("Yes") { _, _ -> onYes() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ---------- Root helpers ----------

    private fun checkRoot(): Boolean = try {
        val p = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
        val out = p.inputStream.bufferedReader().readText()
        p.waitFor() == 0 && out.contains("uid=0")
    } catch (e: Exception) {
        false
    }

    private fun runRoot(command: String) {
        thread {
            try {
                Runtime.getRuntime().exec(arrayOf("su", "-c", command))
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Root command failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
