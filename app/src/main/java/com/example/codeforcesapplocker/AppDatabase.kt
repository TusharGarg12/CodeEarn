package com.example.codeforcesapplocker

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

// --- ENTITIES ---

@Entity(tableName = "restricted_apps")
data class RestrictedApp(
    @PrimaryKey val packageName: String,
    val appName: String,
    val isLocked: Boolean = true
)

@Entity(tableName = "user_wallet")
data class UserWallet(
    @PrimaryKey val id: Int = 0,
    val balanceInMillis: Long = 0
)

@Entity(tableName = "claimed_submissions")
data class ClaimedSubmission(
    @PrimaryKey val submissionId: Long,
    val claimedAt: Long = System.currentTimeMillis()
)

// --- DAO ---

@Dao
interface AppDao {
    // Restricted Apps
    @Query("SELECT * FROM restricted_apps")
    fun getAllRestrictedApps(): Flow<List<RestrictedApp>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRestrictedApp(app: RestrictedApp)

    @Delete
    suspend fun deleteRestrictedApp(app: RestrictedApp) // <--- ADDED THIS

    // Wallet
    @Query("SELECT * FROM user_wallet WHERE id = 0")
    fun getWallet(): Flow<UserWallet?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateWallet(wallet: UserWallet)

    // Submissions
    @Query("SELECT EXISTS(SELECT 1 FROM claimed_submissions WHERE submissionId = :id)")
    suspend fun isSubmissionClaimed(id: Long): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClaimedSubmission(submission: ClaimedSubmission)
}

// --- DATABASE ---

@Database(entities = [RestrictedApp::class, UserWallet::class, ClaimedSubmission::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
}