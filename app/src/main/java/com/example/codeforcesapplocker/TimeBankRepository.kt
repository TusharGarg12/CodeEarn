package com.example.codeforcesapplocker

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimeBankRepository @Inject constructor(
    private val appDao: AppDao
) {

    // 1. Get Time Balance (Converts UserWallet object -> Long)
    // Defaults to 15 minutes (900000ms) if the database is empty (new user)
    val timeBalance: Flow<Long> = appDao.getWallet().map { wallet ->
        wallet?.balanceInMillis ?: (15 * 60 * 1000L)
    }

    // 2. Get Restricted Apps (Converts List<RestrictedApp> -> Set<String>)
    val restrictedApps: Flow<Set<String>> = appDao.getAllRestrictedApps().map { list ->
        if (list.isEmpty()) {
            // Default restricted apps if none are set in DB yet
            setOf("com.instagram.android", "com.google.android.youtube", "com.zhiliaoapp.musically")
        } else {
            list.filter { it.isLocked }.map { it.packageName }.toSet()
        }
    }

    // 3. Update the Time (Used by AppLockService to deduct seconds)
    suspend fun updateTime(newBalance: Long) {
        appDao.updateWallet(UserWallet(id = 0, balanceInMillis = newBalance))
    }

    // 4. Add Reward Time (Used when user solves a problem)
    suspend fun addTime(millisToAdd: Long) {
        // We need to fetch the current balance first
        val currentWallet = appDao.getWallet().firstOrNull()
        val currentBalance = currentWallet?.balanceInMillis ?: 0L

        val newBalance = currentBalance + millisToAdd
        appDao.updateWallet(UserWallet(id = 0, balanceInMillis = newBalance))
    }
}