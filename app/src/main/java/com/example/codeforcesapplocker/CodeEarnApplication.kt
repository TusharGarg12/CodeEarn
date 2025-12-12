package com.example.codeforcesapplocker

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CodeEarnApplication : Application() {
    // Hilt uses this class to generate the dependency graph.
    // No other code is strictly required here for now.
}