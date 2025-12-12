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
import androidx.compose.runtime.LaunchedEffect
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
    viewModel: OnboardingViewModel = hiltViewModel(),
    onSetupComplete: () -> Unit
) {
    // Collecting flow from ViewModel
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // 1. Navigation Event: Watch for isSetupComplete becoming true
    LaunchedEffect(uiState.isSetupComplete) {
        if (uiState.isSetupComplete) {
            onSetupComplete()
        }
    }

    // 2. Permission Check: Runs on start and when returning from Settings
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

        // Step 1: Codeforces Handle
        // Changed to use ViewModel state instead of local state
        OutlinedTextField(
            value = uiState.codeforcesHandle,
            onValueChange = viewModel::onHandleChange,
            label = { Text("Codeforces Handle") },
            placeholder = { Text("e.g. tourist") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
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
                    Toast.makeText(context, "Find 'CodeEarn' -> Allow Restricted Settings first!", Toast.LENGTH_LONG).show()
                }
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                context.startActivity(intent)
            }
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Complete Button
        Button(
            // No need to pass arguments, VM has the state
            onClick = { viewModel.completeSetup() },
            enabled = uiState.isOverlayGranted &&
                    uiState.isAccessibilityGranted &&
                    uiState.codeforcesHandle.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start Earning Time")
        }
    }
}

/**
 * A helper composable to display permission status.
 */
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