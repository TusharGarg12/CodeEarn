package com.example.codeforcesapplocker

import retrofit2.http.GET
import retrofit2.http.Query

// -----------------------------------------------------------------------------
// 1. Data Models (Everything needed for Bot + App Lock)
// -----------------------------------------------------------------------------

// --- Type A: User Info (To get your rating) ---
data class UserInfoResponse(
    val status: String,
    val comment: String?,
    val result: List<UserResult>?
)

data class UserResult(
    val handle: String,
    val rating: Int?, // Needed for the Bot to know your level
    val rank: String?
)

// --- Type B: Submissions (To check if you solved a problem) ---
data class UserStatusResponse(
    val status: String,
    val comment: String?,
    val result: List<Submission>?
)

data class Submission(
    val id: Long,
    val contestId: Int,
    val problem: Problem,
    val verdict: String?,
    val author: Author
)

data class Author(
    val members: List<Member>
)

data class Member(
    val handle: String
)

// --- Type C: Problem Set (To find new problems for you) ---
data class ProblemSetResponse(
    val status: String,
    val comment: String?,
    val result: ProblemSetResult?
)

data class ProblemSetResult(
    val problems: List<Problem>,
    val problemStatistics: List<ProblemStat>
)

data class ProblemStat(
    val contestId: Int,
    val index: String,
    val solvedCount: Int
)

data class Problem(
    val contestId: Int,
    val index: String,
    val name: String,
    val rating: Int?,
    val tags: List<String>
)

// -----------------------------------------------------------------------------
// 2. The API Interface
// -----------------------------------------------------------------------------

interface CodeforcesApiService {

    @GET("user.info")
    suspend fun getUserInfo(
        @Query("handles") handles: String
    ): UserInfoResponse

    @GET("user.status")
    suspend fun getUserSubmissions(
        @Query("handle") handle: String,
        @Query("from") from: Int = 1,
        @Query("count") count: Int = 500
    ): UserStatusResponse

    @GET("problemset.problems")
    suspend fun getProblemSet(
        @Query("tags") tags: String? = null
    ): ProblemSetResponse
}