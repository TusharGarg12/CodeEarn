CodeEarn - Earn Your Screen Time 🧠🔓

CodeEarn is a productivity-focused Android App Locker that forces you to improve your coding skills before you can doom-scroll. It blocks distracting apps (like Instagram, YouTube) and requires you to solve Codeforces problems to "earn" unlock time.

The Logic: 1 Accepted Solution = 30 Minutes of App Time.

🚀 Features

1. The Time Bank ⏳

Instead of a hard block, CodeEarn uses a currency model.

Earn: Solve problems to add time to your wallet.

Spend: Time is deducted second-by-second only while you are using restricted apps.

Lock: Once the timer hits 00:00, the "Black Overlay" appears, blocking access until you solve another problem.

2. Smart Recommendation Bot 🤖

The app doesn't just give you random problems. It analyzes your Codeforces profile and generates a personalized daily problem set using a 4-Slot Strategy:

Slot 1 (The Fixer): Targets your weakest topic (Rating: Current to +300).

Slot 2 (Power Play): A problem from your strongest topic to maintain speed (Rating: ±100).

Slot 3 (General): 2 problems from various topics to simulate contest randomness.

Slot 4 (The Boss): A difficult problem strictly above your level (+200 to +400) to push your limits.

3. Focus Mode 🛡️

Uses Android's Accessibility Service to detect when you open specific package names.

Uses System Overlay to block the screen physically.

Works offline (Time Bank is stored locally).

🛠️ Tech Stack

Language: Kotlin

UI: Jetpack Compose (Material 3)

Architecture: MVVM (Model-View-ViewModel) + Clean Architecture

Dependency Injection: Dagger Hilt

Network: Retrofit + Gson (Codeforces API)

Local Database: Room Database (SQL)

Preferences: Jetpack DataStore

Background Tasks: Kotlin Coroutines & Flows

System Integration: AccessibilityService API, WindowManager

📸 Screenshots

Onboarding

![WhatsApp Image 2025-12-17 at 3 09 18 AM](https://github.com/user-attachments/assets/f519e5f9-5263-4f88-ae00-f2ebc70d3e4b)


Dashboard

![WhatsApp Image 2025-12-17 at 3 09 18 AM (2)](https://github.com/user-attachments/assets/a616795f-6915-481c-95c9-c0a9fcaedfb7)


Locked State

![WhatsApp Image 2025-12-17 at 3 09 18 AM (1)](https://github.com/user-attachments/assets/f406eba4-07fe-41e9-8691-313f6d175f8f)

<img width="2048" height="2048" alt="Gemini_Generated_Image_p6t83lp6t83lp6t8" src="https://github.com/user-attachments/assets/1d412bdb-af03-4bcd-852b-237386266dbd" />



🔧 Installation & Setup

Clone the repo

git clone [https://github.com/TusharGarg12/CodeEarn.git](https://github.com/TusharGarg12/CodeEarn.git)


Open in Android Studio (Ladybug or newer recommended).

Sync Gradle.

Run on Device (Emulator might not support Overlay permissions correctly, physical device recommended).

Permissions Required

Upon first launch, you must grant:

"Display Over Other Apps": To show the lock screen.

"Accessibility Service": To detect when you open Instagram/YouTube.

📂 Project Structure

com.example.codeforcesapplocker
├── data
│   ├── network          # Retrofit API & Models
│   ├── AppDatabase.kt   # Room Database
│   ├── RecommendationRepository.kt # The "Bot" Logic
│   └── TimeBankRepository.kt       # Wallet & Lock Logic
├── di                   # Hilt Modules (Network, Database)
├── services             # Background Services
│   ├── AppLockService.kt        # Accessibility Service (The Police)
│   └── OverlayWindowManager.kt  # The Lock Screen View
├── ui
│   ├── dashboard        # Main Screen (Problem List & Timer)
│   ├── onboarding       # Handle Setup
│   └── appselection     # Choose apps to lock
└── MainActivity.kt      # Navigation Host


🔒 Privacy & Permissions

Why does this app need Accessibility Services?
To function as an App Locker, CodeEarn needs to know which app is currently on your screen (e.g., to distinguish between "Calculator" and "Instagram").

Data Privacy:

All data is processed locally on your device.

Your Codeforces handle is used only to fetch public submission data.

No personal usage data is sent to any external server.

🤝 Contributing

This is a personal project, but ideas are welcome!

Fork the Project

Create your Feature Branch (git checkout -b feature/AmazingFeature)

Commit your Changes (git commit -m 'Add some AmazingFeature')

Push to the Branch (git push origin feature/AmazingFeature)

Open a Pull Request

📄 License

Distributed under the MIT License. See LICENSE for more information.

*Built with ❤️ and ☕ by [TusharGarg12]
