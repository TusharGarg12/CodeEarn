
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp) // Kotlin Symbol Processing for Room
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "com.example.codeforcesapplocker"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.codeforcesapplocker"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

// In C:/Users/gargt/AndroidStudioProjects/codeforcesapplocker/app/build.gradle.kts

dependencies {
    // This is the correct way to implement a BOM.
    // It should be implemented first.
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // --- Core & Lifecycle ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // --- Compose UI ---
    // CORRECTED: Use the aliases defined in your libs.versions.toml
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)

    // --- Persistence ---
    // DataStore for simple key-value storage
    implementation(libs.androidx.datastore.preferences)
    // Room for structured data
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // --- Dependency Injection (Hilt) ---
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    // You already have a direct Hilt navigation dependency, which is good.
    // The one in your TOML (androidx.hilt.navigation.compose) is what you should use.
    implementation(libs.androidx.hilt.navigation.compose)

    // --- Other Libraries ---
    implementation(libs.androidx.games.activity)

    // --- Testing ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation("com.google.accompanist:accompanist-drawablepainter:0.37.3")

    // CORRECTED: Use the new alias for the core Retrofit library
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.gson)
}
