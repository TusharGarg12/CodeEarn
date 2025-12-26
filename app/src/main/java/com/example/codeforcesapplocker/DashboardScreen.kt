package com.example.codeforcesapplocker

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToAppSelection: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Show verification messages (e.g. "Earned 30 mins!")
    LaunchedEffect(uiState.verificationMessage) {
        uiState.verificationMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("CodeEarn") },
                actions = {
                    // 1. Manage Locked Apps (The Lock Icon)
                    IconButton(onClick = onNavigateToAppSelection) {
                        Icon(Icons.Default.Lock, contentDescription = "Manage Locked Apps")
                    }

                    // 2. Refresh Problems (The Refresh Icon)
                    IconButton(onClick = { viewModel.refreshProblems() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }

                    // 3. Logout (The Exit Icon)
                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Time Bank Card
            TimeBankCard(balance = uiState.timeBankBalance)

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Your Daily Challenges",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Solve one to earn 30 mins",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Main Content: Loader, Error, or List
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.recommendedProblems.isEmpty()) {
                // Error State
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No problems found.\nCheck your handle or internet.",
                            textAlign = TextAlign.Center,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.refreshProblems() }) {
                            Text("Retry")
                        }
                    }
                }
            } else {
                // Problem List
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.recommendedProblems) { problem ->
                        ProblemCard(problem) {
                            // Open Codeforces in Browser
                            val url = "https://codeforces.com/contest/${problem.contestId}/problem/${problem.index}"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        }
                    }
                }
            }
        }
    }
}

// --- HELPER COMPOSABLES ---

@Composable
fun TimeBankCard(balance: Long) {
    // Format millis into "1h 30m" or "45m"
    val minutes = (balance / 1000) / 60
    val hours = minutes / 60
    val remainingMinutes = minutes % 60

    val timeText = if (hours > 0) {
        "${hours}h ${remainingMinutes}m"
    } else {
        "${minutes}m"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Available Time", style = MaterialTheme.typography.labelLarge)
            Text(
                text = timeText,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun ProblemCard(problem: Problem, onClick: () -> Unit) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${problem.index}. ${problem.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = problem.tags.take(3).joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                val rating = problem.rating ?: 0
                val badgeColor = when {
                    rating < 1200 -> Color(0xFF81C784) // Newbie (Green)
                    rating < 1400 -> Color(0xFF4CAF50) // Pupil (Dark Green)
                    rating < 1600 -> Color(0xFF03A9F4) // Specialist (Cyan)
                    rating < 1900 -> Color(0xFF0D47A1) // Expert (Blue)
                    else -> Color(0xFFAA00FF)          // CM+ (Purple)
                }

                Surface(
                    color = badgeColor,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = if (rating > 0) "$rating" else "Unrated",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}