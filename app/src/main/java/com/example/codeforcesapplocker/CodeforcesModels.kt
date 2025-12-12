package com.example.codeforcesapplocker

import com.google.gson.annotations.SerializedName

data class CodeforcesResponse(
    val status: String, // "OK" or "FAILED"
    val comment: String?,
    val result: List<SubmissionDto>?
)

data class SubmissionDto(
    val id: Long,
    val contestId: Int?,
    val creationTimeSeconds: Long,
    val problem: ProblemDto,
    val verdict: String? // We are looking for "OK"
)

data class ProblemDto(
    val contestId: Int?,
    val index: String,
    val name: String,
    val rating: Int?,
    val tags: List<String>?
)