package com.example.codeforcesapplocker

import android.util.Log
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EarnRepository @Inject constructor(
    private val api: CodeforcesApiService,
    private val dao: AppDao, // To check if problem was already claimed
    private val timeBankRepository: TimeBankRepository // To add time to the wallet
) {

    // Reward: 30 Minutes per problem (in milliseconds)
    private val REWARD_PER_PROBLEM = 30 * 60 * 1000L

    suspend fun verifyAndReward(handle: String): EarnResult {
        try {
            // 1. Fetch recent submissions
            val response = api.getUserSubmissions(handle, count = 50)

            if (response.status == "FAILED") {
                return EarnResult.Error("API Error: ${response.comment ?: "Unknown Error"}")
            }

            if (response.result == null) {
                return EarnResult.Error("No data received from Codeforces.")
            }

            // 2. Filter for Accepted solutions ("OK")
            val acceptedSubmissions = response.result.filter { it.verdict == "OK" }

            if (acceptedSubmissions.isEmpty()) {
                return EarnResult.NoNewSubmissions
            }

            var problemsRewarded = 0

            // 3. Process each submission
            for (sub in acceptedSubmissions) {
                // Check DB: Have we paid for this specific submission ID yet?
                val alreadyClaimed = dao.isSubmissionClaimed(sub.id)

                if (!alreadyClaimed) {
                    // A. Mark as claimed in DB so we don't pay again
                    dao.insertClaimedSubmission(ClaimedSubmission(submissionId = sub.id))

                    // B. Add time to the bank using our Repository helper
                    timeBankRepository.addTime(REWARD_PER_PROBLEM)

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
            return EarnResult.Error("Server Error ${e.code()}")
        } catch (e: Exception) {
            return EarnResult.Error(e.message ?: "Network Error")
        }
    }
}

// Sealed class must be outside the main class to be accessible elsewhere
sealed class EarnResult {
    data class Success(val problemsSolved: Int, val timeEarned: Long) : EarnResult()
    object NoNewSubmissions : EarnResult()
    data class Error(val message: String) : EarnResult()
}