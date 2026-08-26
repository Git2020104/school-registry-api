package com.maranatha.skools.models

import kotlinx.serialization.Serializable

enum class TermStatus {
    ACTIVE,
    FROZEN,
}

@Serializable
data class TermState(
    val id: Int,
    val name: String,
    val year: Int,
    val status: TermStatus,
)

@Serializable
data class ScoreEntryInput(
    val studentId: Int,
    val subjectId: Int,
    val mark: Double,
)

@Serializable
data class BulkScoreSubmissionRequest(
    val termId: Int,
    val classId: Int,
    val streamId: Int,
    val subjectId: Int,
    val entries: List<ScoreEntryInput>,
)

// Custom Exceptions
class TermFrozenException(
    val termId: Int,
) : IllegalStateException("Term ID $termId is FROZEN. Mark entry or modification is strictly locked.")

class InvalidMarkException(val details: String) : IllegalArgumentException("Mark validation failed: $details")
