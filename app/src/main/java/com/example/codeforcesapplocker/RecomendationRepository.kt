package com.example.codeforcesapplocker


import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecommendationRepository @Inject constructor(
    private val api: CodeforcesApiService
) {
    private val TAG = "CodeEarn_Repo"
    private val MIN_MODERN_CONTEST_ID = 1000

    private val TRACKED_TAGS = listOf(
        "dynamic programming", "graphs", "greedy", "constructive algorithms",
        "math", "strings", "trees", "data structures", "geometry",
        "number theory", "two pointers", "binary search", "implementation"
    )

    suspend fun getRecommendedProblems(handle: String): List<Problem> {
        return try {
            Log.d(TAG, "Fetching recommendations for: $handle")

            // 1. Fetch Data
            val userInfo = api.getUserInfo(handle).result?.firstOrNull()
            val userSubmissions = api.getUserSubmissions(handle, count = 500).result ?: emptyList()
            val problemSetResponse = api.getProblemSet()

            if (problemSetResponse.status != "OK" || problemSetResponse.result == null) {
                return emptyList()
            }

            val allProblems = problemSetResponse.result.problems
            val stats = problemSetResponse.result.problemStatistics
            val popularityMap = stats.associate {
                "${it.contestId}${it.index}" to it.solvedCount
            }

            // 2. User Analysis
            val currentRating = userInfo?.rating ?: 800
            val solvedIds = userSubmissions
                .filter { it.verdict == "OK" }
                .map { "${it.problem.contestId}${it.problem.index}" }
                .toSet()

            // 3. Tag Analysis
            val tagCounts = userSubmissions
                .filter { it.verdict == "OK" }
                .flatMap { it.problem.tags }
                .filter { it in TRACKED_TAGS }
                .groupingBy { it }
                .eachCount()

            val weakTags = TRACKED_TAGS.sortedBy { tagCounts[it] ?: 0 }
            val strongTags = TRACKED_TAGS.sortedByDescending { tagCounts[it] ?: 0 }

            // 4. Base Filtering
            val candidates = allProblems.filter { p ->
                val pId = "${p.contestId}${p.index}"
                !solvedIds.contains(pId) && p.contestId >= MIN_MODERN_CONTEST_ID
            }

            val recommendations = mutableListOf<Problem>()

            // --- SLOT 1: WEAKNESS FIXER (1 Problem) ---
            // Range: [Current, Current + 300]
            val weakTag = weakTags.first()
            val weakProblem = candidates.filter {
                it.tags.contains(weakTag) &&
                        (it.rating ?: 0) in currentRating..(currentRating + 300)
            }.maxByOrNull { popularityMap["${it.contestId}${it.index}"] ?: 0 }

            if (weakProblem != null) recommendations.add(weakProblem)

            // --- SLOT 2: POWER PLAY (1 Problem) ---
            // Range: [Current - 100, Current + 100]
            val strongTag = strongTags.firstOrNull() ?: "implementation"
            val strongProblem = candidates.filter {
                !recommendations.contains(it) &&
                        it.tags.contains(strongTag) &&
                        (it.rating ?: 0) in (currentRating - 100)..(currentRating + 100)
            }.maxByOrNull { popularityMap["${it.contestId}${it.index}"] ?: 0 }

            if (strongProblem != null) recommendations.add(strongProblem)

            // --- SLOT 4: THE BOSS LEVEL (1 Problem) ---
            // Range: [Current + 200, Current + 400] -> Strictly harder
            val hardProblem = candidates.filter {
                !recommendations.contains(it) &&
                        (it.rating ?: 0) in (currentRating + 200)..(currentRating + 400)
            }.maxByOrNull { popularityMap["${it.contestId}${it.index}"] ?: 0 } // Get the most popular hard one

            if (hardProblem != null) recommendations.add(hardProblem)

            // --- SLOT 3: GENERAL PRACTICE (2 Problems) ---
            // Range: [Current - 100, Current + 400]
            val generalProblems = candidates.filter {
                !recommendations.contains(it) &&
                        (it.rating ?: 0) in (currentRating - 100)..(currentRating + 400)
            }.sortedByDescending { popularityMap["${it.contestId}${it.index}"] ?: 0 }
                .take(2) // Taking 2 instead of 3

            recommendations.addAll(generalProblems)

            // Fallback: Fill up to 5 if any slot failed to find a match
            val totalNeeded = 5
            if (recommendations.size < totalNeeded) {
                val filler = candidates
                    .filter { !recommendations.contains(it) && (it.rating ?: 0) in (currentRating - 100)..(currentRating + 400) }
                    .sortedByDescending { popularityMap["${it.contestId}${it.index}"] ?: 0 }
                    .take(totalNeeded - recommendations.size)
                recommendations.addAll(filler)
            }

            recommendations

        } catch (e: Exception) {
            Log.e(TAG, "Error generating recommendations", e)
            emptyList()
        }
    }
}