package com.example.codeforcesapplocker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var repository: UserSettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 1. Initial State is NULL (Loading)
                    // 2. Once DataStore reads, it becomes "" (Onboarding) OR "handle" (Home)
                    val userHandle by repository.userHandle.collectAsState(initial = null)

                    when (userHandle) {
                        null -> {
                            // Still connecting to database... show spinner
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                        "" -> {
                            // Database connected, but handle is empty -> New User
                            OnboardingScreen(
                                onSetupComplete = {
                                    // No manual navigation needed.
                                    // When setup completes, repository updates 'userHandle',
                                    // causing this block to automatically recompose to the 'else' block.
                                }
                            )
                        }
                        else -> {
                            // Handle found -> Go to Home
                            HomeScreen(
                                handle = userHandle ?: "",
                                onLogout = {
                                    // Optional: Implementation for logging out (clearing DataStore) would go here
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}