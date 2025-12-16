package com.example.codeforcesapplocker

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.text.TextUtils
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.codeforcesapplocker.services.AppLockService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val repository: UserSettingsRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState = _uiState.asStateFlow()

    // 1. Updates text as user types (Fixed: This was missing previously)
    fun onHandleChange(newHandle: String) {
        _uiState.update {
            it.copy(codeforcesHandle = newHandle, errorMessage = null)
        }
    }

    // 2. Checks if the user has granted necessary Android permissions
    fun checkPermissions() {
        val overlay = Settings.canDrawOverlays(context)
        val accessibility = isAccessibilityServiceEnabled(context, AppLockService::class.java)

        _uiState.update {
            it.copy(isOverlayGranted = overlay, isAccessibilityGranted = accessibility)
        }
    }

    // 3. Saves the handle to Database (Triggers navigation to Dashboard)
    fun completeSetup(handle: String) {
        // Trim whitespace (e.g. "tourist " -> "tourist") to prevent API errors
        val cleanHandle = handle.trim()

        if (cleanHandle.isBlank()) return

        viewModelScope.launch {
            repository.saveHandle(cleanHandle)
            // Note: No manual navigation needed here.
            // MainActivity observes the repository and will switch screens automatically.
        }
    }

    // --- Helper to check Accessibility Service status (Android API is tricky here) ---
    private fun isAccessibilityServiceEnabled(context: Context, service: Class<*>): Boolean {
        val expectedComponentName = ComponentName(context, service)

        val enabledServicesSetting = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServicesSetting)

        while (colonSplitter.hasNext()) {
            val componentNameString = colonSplitter.next()
            val enabledService = ComponentName.unflattenFromString(componentNameString)
            if (enabledService != null && enabledService == expectedComponentName) return true
        }
        return false
    }
}