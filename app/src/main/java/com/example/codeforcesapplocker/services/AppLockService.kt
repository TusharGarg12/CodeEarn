package com.example.codeforcesapplocker.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.codeforcesapplocker.AppDao
import com.example.codeforcesapplocker.UserWallet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class AppLockService : AccessibilityService() {

    @Inject
    lateinit var overlayWindowManager: OverlayWindowManager

    @Inject
    lateinit var appDao: AppDao

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var restrictedPackages = setOf<String>()

    // The Timer Job that deducts time
    private var timerJob: Job? = null

    companion object {
        private const val TAG = "CodeEarn_Lock"
        // Default time: 2 Minutes (120,000 ms)
        private const val DEFAULT_TIME_MS = 120000L
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.e(TAG, ">>> SERVICE STARTED <<<")

        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
            flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
        }
        this.serviceInfo = info

        serviceScope.launch {
            appDao.getAllRestrictedApps().collectLatest { list ->
                restrictedPackages = list.map { it.packageName }.toSet()
                Log.e(TAG, "Restricted List Updated: $restrictedPackages")
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return

            // Log.e(TAG, "App Opened: $packageName") // Commented out to reduce noise

            if (restrictedPackages.contains(packageName)) {
                // IT IS A RESTRICTED APP!
                // Don't lock immediately. Start the countdown "Banker".
                startTimer()
            } else {
                // IT IS A SAFE APP
                if (packageName == this.packageName) {
                    // Ignore our own app (keep overlay if shown)
                    return
                }

                // If we leave the restricted app, STOP paying and HIDE overlay
                stopTimer()
                overlayWindowManager.hide()
            }
        }
    }

    private fun startTimer() {
        // If timer is already running (e.g. switched from Instagram to YouTube), don't restart it
        if (timerJob?.isActive == true) return

        Log.e(TAG, "Starting Timer...")

        timerJob = serviceScope.launch(Dispatchers.IO) {
            while (isActive) {
                // 1. Get Current Balance
                // We use first() to get the current snapshot from the Flow
                var wallet = appDao.getWallet().first()

                // If user has no wallet row yet, create one with default time
                if (wallet == null) {
                    wallet = UserWallet(id = 0, balanceInMillis = DEFAULT_TIME_MS)
                    appDao.updateWallet(wallet)
                }

                if (wallet.balanceInMillis > 0) {
                    // 2. HAS TIME: Deduct 1 second (1000ms)
                    val newBalance = wallet.balanceInMillis - 1000
                    appDao.updateWallet(wallet.copy(balanceInMillis = newBalance))

                    Log.e(TAG, "Time Remaining: ${newBalance / 1000}s")

                    // Wait 1 second before next tick
                    delay(1000)
                } else {
                    // 3. NO TIME: Lock the screen!
                    Log.e(TAG, "Time's Up! Locking screen.")
                    withContext(Dispatchers.Main) {
                        overlayWindowManager.show()
                    }
                    // Stop the timer loop since we are locked
                    break
                }
            }
        }
    }

    private fun stopTimer() {
        if (timerJob != null) {
            Log.e(TAG, "Stopping Timer (Left app)")
            timerJob?.cancel()
            timerJob = null
        }
    }

    override fun onInterrupt() {
        overlayWindowManager.hide()
        stopTimer()
    }

    override fun onDestroy() {
        super.onDestroy()
        overlayWindowManager.hide()
        stopTimer()
    }
}