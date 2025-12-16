package com.example.codeforcesapplocker

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect

@Composable
fun OnboardingScreen(
    // FIX: Do NOT put 'handle: String' here.
    // The ViewModel holds the data, and MainActivity calls this without arguments.
    viewModel: OnboardingViewModel = hiltViewModel(),
    onSetupComplete: () -> Unit
) {
    // 1. Observe the ViewModel State (Single Source of Truth)
    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current

    // Re-check permissions every time the app resumes (e.g. coming back from Settings)
    LifecycleResumeEffect(Unit) {
        viewModel.checkPermissions()
        onPauseOrDispose { }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome to CodeEarn",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Step 1: Codeforces Handle Input
        OutlinedTextField(
            value = uiState.codeforcesHandle, // Read from ViewModel
            onValueChange = { viewModel.onHandleChange(it) }, // Send to ViewModel
            label = { Text("Codeforces Handle") },
            placeholder = { Text("e.g. tourist") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.errorMessage != null,
            supportingText = {
                if (uiState.errorMessage != null) {
                    Text(uiState.errorMessage ?: "")
                }
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Step 2: Permission Dashboard
        PermissionRow(
            title = "Draw Over Apps",
            isGranted = uiState.isOverlayGranted,
            onClick = {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                intent.data = Uri.parse("package:${context.packageName}")
                context.startActivity(intent)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        PermissionRow(
            title = "Accessibility Service",
            isGranted = uiState.isAccessibilityGranted,
            onClick = {
                if (!uiState.isAccessibilityGranted) {
                    Toast.makeText(context, "Find 'CodeEarn' and enable it.", Toast.LENGTH_LONG).show()
                }
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                context.startActivity(intent)
            }
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Complete Button
        Button(
            // Pass the handle from the state to the complete function
            onClick = { viewModel.completeSetup(uiState.codeforcesHandle) },
            enabled = uiState.isOverlayGranted &&
                    uiState.isAccessibilityGranted &&
                    uiState.codeforcesHandle.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start Earning Time")
        }
    }
}

// --- Helper Composable for Permission Rows ---
@Composable
fun PermissionRow(
    title: String,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isGranted, onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (isGranted) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Granted",
                tint = Color(0xFF4CAF50), // Green
                modifier = Modifier.size(24.dp)
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Enable",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelLarge
                )
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Denied",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}