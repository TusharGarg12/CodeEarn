package com.example.codeforcesapplocker.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.codeforcesapplocker.TimeBankRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    lateinit var timeRepository: TimeBankRepository

    // Coroutine Scope for the service lifecycle
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    // List of apps to block (e.g., com.instagram.android)
    private var restrictedPackages = setOf<String>()

    // Timer Logic Variables
    private var timerJob: Job? = null
    private var currentSessionBalance: Long = 0L // Tracks time in memory
    private var isTimerRunning = false

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("AppLockService", "Service Connected")

        // 1. Configure Accessibility Service
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
            flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
        }
        this.serviceInfo = info

        // 2. Observe the list of restricted apps from Database
        serviceScope.launch {
            timeRepository.restrictedApps.collect { list ->
                restrictedPackages = list
                Log.d("AppLockService", "Restricted Apps Updated: $list")
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return

            if (restrictedPackages.contains(packageName)) {
                // User opened a restricted app (e.g., Instagram)
                startTimer()
            } else {
                // User is in a safe app (or our own app)
                // Note: We check if packageName != null to avoid stopping on system UI glitches
                if (packageName != this.packageName) {
                    stopTimer()
                }
            }
        }
    }

    private fun startTimer() {
        // Prevent starting multiple timers if user switches between two restricted apps quickly
        if (isTimerRunning) return

        isTimerRunning = true
        Log.d("AppLockService", "Starting Timer...")

        timerJob = serviceScope.launch(Dispatchers.IO) {
            // 1. Get the latest time balance from DB
            // We use .first() to get the snapshot value
            currentSessionBalance = timeRepository.timeBalance.first()

            while (isActive && isTimerRunning) {
                if (currentSessionBalance > 1000) {
                    // HAS TIME: Deduct 1 second locally
                    currentSessionBalance -= 1000

                    // Debug log to show it's working (remove in production)
                    // Log.d("AppLockService", "Time left: ${currentSessionBalance/1000}s")

                    delay(1000)
                } else {
                    // NO TIME LEFT: Lock the screen!
                    Log.d("AppLockService", "Time is up! Locking.")

                    // Show Overlay on Main Thread
                    withContext(Dispatchers.Main) {
                        overlayWindowManager.show()
                    }

                    // Set balance to 0 and save to DB immediately
                    currentSessionBalance = 0
                    timeRepository.updateTime(0)

                    // Break the loop (timer stops, but overlay stays)
                    break
                }
            }
        }
    }

    private fun stopTimer() {
        if (!isTimerRunning) return

        Log.d("AppLockService", "Stopping Timer & Saving Balance")
        isTimerRunning = false
        timerJob?.cancel()
        timerJob = null

        // 1. Hide the Lock Overlay
        overlayWindowManager.hide()

        // 2. SAVE the remaining time to the Database
        serviceScope.launch(Dispatchers.IO) {
            timeRepository.updateTime(currentSessionBalance)
        }
    }

    override fun onInterrupt() {
        stopTimer()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
    }
}