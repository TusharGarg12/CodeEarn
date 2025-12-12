package com.example.codeforcesapplocker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val appDao: AppDao,
    private val earnRepository: EarnRepository,
    private val userSettingsRepository: UserSettingsRepository
) : ViewModel() {

    // 1. Time Balance
    val timeBalance = appDao.getWallet()
        .map { it?.balanceInMillis ?: 0L }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0L
        )

    // 2. Earning UI State
    private val _earnState = MutableStateFlow<EarnUiState>(EarnUiState.Idle)
    val earnState = _earnState.asStateFlow()

    init {
        initializeWalletIfEmpty()
    }

    // Called when user clicks "Verify Solved Problems"
    fun verifySubmissions() {
        viewModelScope.launch {
            _earnState.value = EarnUiState.Loading

            // FIX: Added .trim() to remove whitespace that causes HTTP 400 errors
            val handle = userSettingsRepository.userHandle.first().trim()

            if (handle.isBlank()) {
                _earnState.value = EarnUiState.Error("User handle not found. Please Logout and set it again.")
                return@launch
            }

            // Call the repository to check Codeforces
            val result = earnRepository.verifyAndReward(handle)

            _earnState.value = when (result) {
                is EarnResult.Success -> EarnUiState.Success(result.problemsSolved, result.timeEarned)
                is EarnResult.NoNewSubmissions -> EarnUiState.Error("No new accepted submissions found (last 10).")
                is EarnResult.Error -> EarnUiState.Error(result.message)
            }

            // Auto-dismiss the message after 4 seconds
            if (_earnState.value !is EarnUiState.Loading) {
                delay(4000)
                _earnState.value = EarnUiState.Idle
            }
        }
    }

    private fun initializeWalletIfEmpty() {
        viewModelScope.launch {
            val currentWallet = appDao.getWallet().first()
            if (currentWallet == null) {
                val initialTime = 120000L // 2 minutes default
                appDao.updateWallet(UserWallet(id = 0, balanceInMillis = initialTime))
            }
        }
    }
}

// Simple state for the UI
sealed class EarnUiState {
    object Idle : EarnUiState()
    object Loading : EarnUiState()
    data class Success(val count: Int, val timeAdded: Long) : EarnUiState()
    data class Error(val msg: String) : EarnUiState()
}