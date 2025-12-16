CodeEarn 🔒💻

Turn your distractions into motivation.
Lock your addictive apps and unlock them only by solving problems on Codeforces.

CodeEarn is an Android productivity app built with Jetpack Compose and Kotlin that helps competitive programmers maintain focus. It uses an Accessibility Service to detect when you open restricted apps (like Instagram or YouTube) and blocks them with an overlay until you earn more "screen time" by solving algorithmic problems.

✨ Features

🚫 App Blocker: Select specific apps to lock from a list of all installed packages.

⏳ Time Allowance: Users start with a time bank (e.g., 10 minutes). Time ticks down only while using restricted apps.

🛡️ The Enforcer: When time hits 0, a system-level overlay blocks the screen.

💰 Solve to Earn: Connects to the Codeforces API to verify your recent problem submissions.

✅ Reward System: Automatically detects new "Accepted" solutions and rewards you with 30 minutes of screen time per problem.

🛠️ Tech Stack

Language: Kotlin

UI: Jetpack Compose (Material3)

Architecture: MVVM (Model-View-ViewModel) + Clean Architecture principles

Dependency Injection: Dagger Hilt

Async: Coroutines & Flow

Local Data: * Room Database: For storing restricted apps and wallet balance.

DataStore: For storing user preferences (Codeforces handle).

Networking: Retrofit + Gson (Codeforces API)

Background Tasks: Android Accessibility Service (for app detection) & WindowManager (for blocking overlay).

🚀 Getting Started

Prerequisites

Android Studio Ladybug (or newer)

Android Device/Emulator running Android 8.0 (Oreo) or higher.

Installation

Clone the repo:

git clone [https://github.com/YourUsername/CodeEarn.git](https://github.com/YourUsername/CodeEarn.git)


Open the project in Android Studio.

Sync Gradle files.

Run on your device.

⚠️ Permissions Setup (Crucial)

Since this app uses advanced system features, you must manually grant permissions on the first run:

Display over other apps: Allows the lock screen overlay.

Accessibility Service: Allows the app to detect which package is currently in the foreground.

Note for Android 13+: If you side-load the APK, you may need to go to "App Info" -> three dots -> "Allow Restricted Settings" to enable the Accessibility service.

📸 Screenshots

Dashboard

App Selection

Locked Screen

(Add screenshot here)

(Add screenshot here)

(Add screenshot here)

🏗️ Architecture Overview

services/AppLockService.kt: The core engine. Runs in the background, detects window changes, manages the countdown timer, and triggers the overlay.

services/OverlayWindowManager.kt: Manages the Floating Window (overlay) lifecycle and attaches the Compose UI to the WindowManager.

data/repository/EarnRepository.kt: Handles the logic for fetching Codeforces submissions and calculating rewards.

feature/appselection: UI for the user to toggle which apps to restrict.

🤝 Contributing

Contributions are welcome! Please fork the repository and create a Pull Request for any features or bug fixes.
