package com.example.codeforcesapplocker

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.text.TextUtils
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.codeforcesapplocker.services.AppLockService // <--- IMPORT THE REAL SERVICE
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// DELETE THE PLACEHOLDER CLASS THAT WAS HERE

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val repository: UserSettingsRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState = _uiState.asStateFlow()

    fun checkPermissions() {
        val overlay = Settings.canDrawOverlays(context)
        // Now checks the REAL service class
        val accessibility = isAccessibilityServiceEnabled(context, AppLockService::class.java)

        _uiState.update {
            it.copy(isOverlayGranted = overlay, isAccessibilityGranted = accessibility)
        }
    }

    // Connects UI text input to ViewModel state
    fun onHandleChange(newHandle: String) {
        _uiState.update { currentState ->
            currentState.copy(codeforcesHandle = newHandle, errorMessage = null)
        }
    }

    fun completeSetup() {
        val handle = _uiState.value.codeforcesHandle

        viewModelScope.launch {
            try {
                repository.saveHandle(handle)
                // Signal the UI that setup is complete
                _uiState.update { it.copy(isSetupComplete = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to save data: ${e.message}") }
            }
        }
    }

    // Existing verify logic...
    fun verifyUser() {
        val currentHandle = _uiState.value.codeforcesHandle
        if (currentHandle.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Handle cannot be empty") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // Mock network delay
                delay(2000)
                if (currentHandle.lowercase() == "error") throw Exception("User not found")
                _uiState.update { it.copy(isLoading = false, isVerified = true, detectedRank = "Specialist") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Unknown error") }
            }
        }
    }

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

