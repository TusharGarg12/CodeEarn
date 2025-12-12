package com.example.codeforcesapplocker

import android.util.Log
import kotlinx.coroutines.flow.first
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EarnRepository @Inject constructor(
    private val api: CodeforcesApi,
    private val dao: AppDao
) {

    // Reward: 30 Minutes per problem (in milliseconds)
    private val REWARD_PER_PROBLEM = 30 * 60 * 1000L

    suspend fun verifyAndReward(handle: String): EarnResult {
        try {
            // 1. Fetch last 10 submissions
            val response = api.getUserSubmissions(handle)
            if (response.status != "OK" || response.result == null) {
                return EarnResult.Error("Codeforces API Error: ${response.comment}")
            }

            // 2. Filter for Accepted solutions ("OK")
            val acceptedSubmissions = response.result.filter { it.verdict == "OK" }

            if (acceptedSubmissions.isEmpty()) {
                return EarnResult.NoNewSubmissions
            }

            var problemsRewarded = 0

            // 3. Check which ones are NEW (not in our DB)
            for (sub in acceptedSubmissions) {
                val alreadyClaimed = dao.isSubmissionClaimed(sub.id)

                if (!alreadyClaimed) {
                    // 4. PAY THE USER!
                    dao.insertClaimedSubmission(ClaimedSubmission(submissionId = sub.id))

                    val currentWallet = dao.getWallet().first()
                    val newBalance = (currentWallet?.balanceInMillis ?: 0L) + REWARD_PER_PROBLEM

                    dao.updateWallet(
                        currentWallet?.copy(balanceInMillis = newBalance)
                            ?: UserWallet(balanceInMillis = newBalance)
                    )

                    problemsRewarded++
                    Log.d("EarnRepo", "Rewarded for submission ${sub.id}")
                }
            }

            return if (problemsRewarded > 0) {
                EarnResult.Success(problemsRewarded, problemsRewarded * REWARD_PER_PROBLEM)
            } else {
                EarnResult.NoNewSubmissions
            }

        } catch (e: HttpException) {
            // FIX: Extract the actual error message from the server if possible
            val errorBody = e.response()?.errorBody()?.string()
            Log.e("EarnRepo", "HTTP Error: $errorBody")
            return EarnResult.Error("Server Error ${e.code()}: $errorBody")
        } catch (e: Exception) {
            Log.e("EarnRepo", "Network Error", e)
            return EarnResult.Error(e.message ?: "Unknown Network Error")
        }
    }
}

sealed class EarnResult {
    data class Success(val problemsSolved: Int, val timeEarned: Long) : EarnResult()
    object NoNewSubmissions : EarnResult()
    data class Error(val message: String) : EarnResult()
}