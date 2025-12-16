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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
// Make sure to import your AppSelectionScreen
// import com.example.codeforcesapplocker.ui.appselection.AppSelectionScreen
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
                    val navController = rememberNavController()

                    // Watch the user handle to decide the START destination
                    val userHandle by repository.userHandle.collectAsState(initial = null)

                    if (userHandle == null) {
                        // 1. Loading State (While reading Database)
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        // 2. Decide where to start
                        val startDest = if (userHandle!!.isBlank()) "onboarding" else "dashboard"

                        NavHost(
                            navController = navController,
                            startDestination = startDest
                        ) {
                            // ROUTE 1: Onboarding
                            composable("onboarding") {
                                OnboardingScreen(
                                    onSetupComplete = {
                                        // Clear back stack and go to Dashboard
                                        navController.navigate("dashboard") {
                                            popUpTo("onboarding") { inclusive = true }
                                        }
                                    }
                                )
                            }

                            // ROUTE 2: Dashboard
                            composable("dashboard") {
                                DashboardScreen(
                                    // Pass navigation callback
                                    onNavigateToAppSelection = {
                                        navController.navigate("app_selection")
                                    }
                                )
                            }

                            // ROUTE 3: App Selection
                            composable("app_selection") {
                                // This is the screen you provided
                                AppSelectionScreen()
                            }
                        }
                    }
                }
            }
        }
    }
}