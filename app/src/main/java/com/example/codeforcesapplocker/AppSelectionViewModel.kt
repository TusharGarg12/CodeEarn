package com.example.codeforcesapplocker

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class AppUiModel(
    val packageName: String,
    val appName: String,
    val icon: android.graphics.drawable.Drawable?, // Note: Keeping drawables in VM is usually bad practice, but okay for MVP
    val isLocked: Boolean
)

@HiltViewModel
class AppSelectionViewModel @Inject constructor(
    private val appDao: AppDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // 1. Raw list of installed apps from system
    private val _installedApps = MutableStateFlow<List<AppUiModel>>(emptyList())

    // 2. Apps currently saved in DB as locked
    private val _lockedAppsFlow = appDao.getAllRestrictedApps()

    // 3. Combine them: Show all installed apps, mark checkbox true if they exist in DB
    val appList = combine(_installedApps, _lockedAppsFlow) { installed, locked ->
        val lockedPackageNames = locked.map { it.packageName }.toSet()

        installed.map { app ->
            app.copy(isLocked = lockedPackageNames.contains(app.packageName))
        }.sortedBy { it.appName } // Sort alphabetically
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadInstalledApps()
    }

    private fun loadInstalledApps() {
        viewModelScope.launch {
            _installedApps.value = withContext(Dispatchers.IO) {
                val pm = context.packageManager
                // Get all apps that have a launch intent (ignoring background services/system modules)
                val packages = pm.getInstalledPackages(PackageManager.GET_META_DATA)

                packages.mapNotNull { packageInfo ->
                    // Filter: Must be launchable and not our own app
                    if (pm.getLaunchIntentForPackage(packageInfo.packageName) != null &&
                        packageInfo.packageName != context.packageName) {

                        val appName = packageInfo.applicationInfo?.loadLabel(pm).toString()
                        val icon = packageInfo.applicationInfo?.loadIcon(pm)

                        AppUiModel(
                            packageName = packageInfo.packageName,
                            appName = appName,
                            icon = icon,
                            isLocked = false // Default to false, 'combine' will fix this
                        )
                    } else {
                        null
                    }
                }
            }
        }
    }

    fun toggleAppLock(app: AppUiModel) {
        viewModelScope.launch {
            if (app.isLocked) {
                // Unlock: Remove from DB
                val entity = RestrictedApp(app.packageName, app.appName)
                appDao.deleteRestrictedApp(entity)
            } else {
                // Lock: Add to DB
                val entity = RestrictedApp(app.packageName, app.appName, isLocked = true)
                appDao.insertRestrictedApp(entity)
            }
        }
    }
}