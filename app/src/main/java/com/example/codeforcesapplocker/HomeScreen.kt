package com.example.codeforcesapplocker

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.util.Locale

@Composable
fun HomeScreen(
    handle: String,
    onLogout: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    var showAppSelection by remember { mutableStateOf(false) }
    val timeInMillis by viewModel.timeBalance.collectAsState()
    val earnState by viewModel.earnState.collectAsState()

    if (showAppSelection) {
        BackHandler { showAppSelection = false }
        Column(modifier = Modifier.fillMaxSize()) {
            Button(
                onClick = { showAppSelection = false },
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Text("Done / Back to Dashboard")
            }
            AppSelectionScreen()
        }
    } else {
        DashboardContent(
            handle = handle,
            timeInMillis = timeInMillis,
            earnState = earnState,
            onVerify = { viewModel.verifySubmissions() },
            onManageApps = { showAppSelection = true },
            onLogout = onLogout
        )
    }
}

@Composable
fun DashboardContent(
    handle: String,
    timeInMillis: Long,
    earnState: EarnUiState,
    onVerify: () -> Unit,
    onManageApps: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome, $handle",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text("Time Balance", style = MaterialTheme.typography.bodyLarge)
        Text(
            text = formatTime(timeInMillis),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- VERIFY BUTTON & STATUS ---
        Button(
            onClick = onVerify,
            enabled = earnState !is EarnUiState.Loading,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            if (earnState is EarnUiState.Loading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.size(8.dp))
                Text("Verify Solved Problems")
            }
        }

        // Feedback Message Area
        AnimatedVisibility(visible = earnState !is EarnUiState.Idle && earnState !is EarnUiState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .background(
                        color = if (earnState is EarnUiState.Success) Color(0xFF4CAF50) else MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(16.dp)
            ) {
                Text(
                    text = when (earnState) {
                        is EarnUiState.Success -> "Success! +${earnState.timeAdded / 60000} mins earned."
                        is EarnUiState.Error -> earnState.msg
                        else -> ""
                    },
                    color = if (earnState is EarnUiState.Success) Color.White else MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onManageApps,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Select Apps to Lock")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Logout")
        }
    }
}

fun formatTime(millis: Long): String {
    if (millis <= 0) return "00:00:00"
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
}