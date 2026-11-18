package com.mrothberg.kakumei.client

import com.mrothberg.kakumei.apimodels.SummaryRequest
import com.mrothberg.kakumei.wkamodels.StudyQueue
import java.io.IOException
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.time.format.DateTimeParseException

class InvalidSummaryException(message: String, cause: Throwable? = null) :
    IOException(message, cause)

internal fun mapStudyQueue(response: SummaryRequest): StudyQueue {
    val data = response.data ?: throw InvalidSummaryException("Summary data is missing")
    val lessons = data.lessons ?: throw InvalidSummaryException("Summary lessons are missing")
    val reviews = data.reviews ?: throw InvalidSummaryException("Summary reviews are missing")
    // Interpret the forecast at the report's hour, independent of the device clock.
    val hour = parseTimestamp(response.data_updated_at, "data_updated_at")
        .truncatedTo(ChronoUnit.HOURS)
    var available = 0
    var nextHour = 0
    var nextDay = 0
    for (review in reviews) {
        if (review == null) throw InvalidSummaryException("Summary contains a null review bucket")
        val at = parseTimestamp(review.available_at, "reviews.available_at")
        val count = subjectCount(review.subject_ids)
        when {
            at <= hour -> available += count
            at <= hour.plus(1, ChronoUnit.HOURS) -> nextHour += count
            at <= hour.plus(24, ChronoUnit.HOURS) -> nextDay += count
        }
    }
    val lessonCount = lessons.sumOf { lesson ->
        if (lesson == null) throw InvalidSummaryException("Summary contains a null lesson bucket")
        subjectCount(lesson.subject_ids)
    }
    val nextReview = data.next_reviews_at?.let { parseTimestamp(it, "next_reviews_at").epochSecond } ?: 0L
    // Preserve the legacy model's disjoint forecast counts and epoch-second date.
    return StudyQueue(0, lessonCount, available, nextHour, nextDay, nextReview)
}

private fun subjectCount(ids: List<Int?>?): Int {
    if (ids == null || ids.any { it == null }) {
        throw InvalidSummaryException("Summary subject_ids are missing or contain null")
    }
    return ids.size
}

private fun parseTimestamp(value: String?, field: String): Instant {
    if (value == null) throw InvalidSummaryException("Summary $field is missing")
    return try {
        Instant.parse(value)
    } catch (error: DateTimeParseException) {
        throw InvalidSummaryException("Summary $field is not a valid timestamp", error)
    }
}
