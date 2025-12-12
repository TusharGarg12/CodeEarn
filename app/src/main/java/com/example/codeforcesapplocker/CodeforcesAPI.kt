package com.example.codeforcesapplocker

import retrofit2.http.GET
import retrofit2.http.Query

interface CodeforcesApi {
    // https://codeforces.com/api/user.status?handle=Fefer_Ivan&from=1&count=10
    @GET("user.status")
    suspend fun getUserSubmissions(
        @Query("handle") handle: String,
        @Query("from") from: Int = 1,
        @Query("count") count: Int = 10 // We only check the last 10 submissions
    ): CodeforcesResponse
}