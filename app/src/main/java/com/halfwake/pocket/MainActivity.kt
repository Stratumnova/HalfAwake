package com.halfwake.pocket

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var permissionButton: Button
    private lateinit var logContainer: LinearLayout

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op either way */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.status_text)
        permissionButton = findViewById(R.id.permission_button)
        logContainer = findViewById(R.id.log_container)

        permissionButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }

        requestNotificationPermissionIfNeeded()
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val granted = UsageRepository.hasUsageAccess(this)
        permissionButton.visibility = if (granted) android.view.View.GONE else android.view.View.VISIBLE

        if (!granted) {
            statusText.text = "Halfwake needs Usage Access to read screen time and app " +
                "activity — tap below to grant it. It never reads what you type, only how long " +
                "and how often apps are used."
            logContainer.removeAllViews()
            return
        }

        TickWorker.ensureScheduled(this)

        val history = DiaryStore.load(this)
        if (history.isEmpty()) {
            statusText.text = "First tick hasn't run yet. Starting one now — check back in a moment."
            TickWorker.runOnce(this)
            return
        }

        val latest = history.last()
        val fmt = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
        statusText.text = "${latest.mood.uppercase()}\n${latest.line}\n\n(${latest.reason})"

        logContainer.removeAllViews()
        history.reversed().forEach { entry ->
            val row = TextView(this).apply {
                text = "${fmt.format(Date(entry.atMillis))} — ${entry.mood}\n${entry.line}"
                setPadding(0, 24, 0, 24)
                textSize = 15f
            }
            logContainer.addView(row)
        }
    }
}
