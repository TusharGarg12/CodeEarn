package com.example.codeforcesapplocker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = true,
    val recommendedProblems: List<Problem> = emptyList(),
    val timeBankBalance: Long = 0L,
    val userHandle: String = "",
    val verificationMessage: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val recommendationRepository: RecommendationRepository,
    private val userSettingsRepository: UserSettingsRepository,
    private val timeBankRepository: TimeBankRepository,
    private val earnRepository: EarnRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadDashboard()
        observeTimeBank()
    }

    // 1. Listen to the database for time changes (Real-time UI update)
    private fun observeTimeBank() {
        viewModelScope.launch {
            timeBankRepository.timeBalance.collect { millis ->
                _uiState.update { it.copy(timeBankBalance = millis) }
            }
        }
    }

    // 2. Initial Data Load
    private fun loadDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val handle = userSettingsRepository.userHandle.firstOrNull() ?: return@launch

            // If we are logged out (handle is empty), stop here.
            if (handle.isBlank()) {
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }

            // Fetch the "Bot" recommendations
            val problems = recommendationRepository.getRecommendedProblems(handle)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    userHandle = handle,
                    recommendedProblems = problems
                )
            }
        }
    }

    // 3. User clicked Refresh button
    fun refreshProblems() {
        checkSubmissions()
    }

    // 4. Verify if user solved a problem & Reload list
    private fun checkSubmissions() {
        viewModelScope.launch {
            val handle = _uiState.value.userHandle
            if (handle.isBlank()) return@launch

            _uiState.update { it.copy(isLoading = true) }

            // A. Check for rewards
            val result = earnRepository.verifyAndReward(handle)

            // B. Get fresh problems (e.g. remove the one just solved)
            val problems = recommendationRepository.getRecommendedProblems(handle)

            // C. Update UI with result message
            _uiState.update {
                it.copy(
                    isLoading = false,
                    recommendedProblems = problems,
                    verificationMessage = when(result) {
                        is EarnResult.Success -> "Earned ${result.timeEarned / 60000} mins!"
                        is EarnResult.NoNewSubmissions -> "No new accepted solutions found."
                        is EarnResult.Error -> result.message
                    }
                )
            }
        }
    }

    // 5. Logout Logic (Triggers navigation to Onboarding)
    fun logout() {
        viewModelScope.launch {
            userSettingsRepository.saveHandle("")
        }
    }
}