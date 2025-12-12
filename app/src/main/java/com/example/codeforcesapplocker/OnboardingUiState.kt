package com.example.codeforcesapplocker

data class OnboardingUiState(
    val codeforcesHandle: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isVerified: Boolean = false,
    val detectedRank: String? = null,
    val isOverlayGranted: Boolean = false,
    val isAccessibilityGranted: Boolean = false,

    // THIS is the field that was missing causing your error:
    val isSetupComplete: Boolean = false
)
