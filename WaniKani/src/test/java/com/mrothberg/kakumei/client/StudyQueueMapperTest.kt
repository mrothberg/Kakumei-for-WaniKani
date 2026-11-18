package com.mrothberg.kakumei.client

import com.google.gson.Gson
import com.mrothberg.kakumei.apimodels.SummaryRequest
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.Instant

class StudyQueueMapperTest {
    @Test
    fun `repository maps summary without database initialization`() = runTest {
        val service = object : WaniKaniRemoteService {
            override suspend fun getSummary() = summaryFixture()
        }
        val queue = DefaultWaniKaniRepository(service).getStudyQueue()

        assertEquals(0, queue.id)
        assertEquals(2, queue.lessons_available)
        assertEquals(3, queue.reviews_available)
        assertEquals(1, queue.reviews_available_next_hour)
        assertEquals(3, queue.reviews_available_next_day)
        assertEquals(Instant.parse("2026-09-26T23:00:00Z").epochSecond, queue.next_review_date)
        assertEquals(queue.next_review_date * 1000, queue.nextReviewDateInMillis)
    }

    @Test
    fun `empty arrays and no next review return zero counts`() {
        val response = summaryFixture().apply {
            data.lessons.clear()
            data.reviews.clear()
            data.next_reviews_at = null
        }
        val queue = mapStudyQueue(response)
        assertEquals(0, queue.lessons_available)
        assertEquals(0, queue.reviews_available)
        assertEquals(0, queue.reviews_available_next_hour)
        assertEquals(0, queue.reviews_available_next_day)
        assertEquals(0L, queue.next_review_date)
    }

    @Test
    fun `unordered sparse buckets retain their time windows across midnight`() {
        val response = summaryFixture().apply {
            data.reviews.removeAt(1)
            data.reviews.reverse()
        }
        val queue = mapStudyQueue(response)
        assertEquals(3, queue.reviews_available)
        assertEquals(0, queue.reviews_available_next_hour)
        assertEquals(3, queue.reviews_available_next_day)
    }

    @Test
    fun `forecast stops at 24 hours and accepts empty subject lists`() {
        val response = summaryFixture().apply {
            data.reviews[3].available_at = "2026-09-28T00:00:00Z"
            data.reviews[1].subject_ids.clear()
        }
        val queue = mapStudyQueue(response)
        assertEquals(0, queue.reviews_available_next_hour)
        assertEquals(2, queue.reviews_available_next_day)
    }

    @Test
    fun `missing required fields fail instead of looking like an empty queue`() {
        val mutations: List<(SummaryRequest) -> Unit> = listOf(
            { it.data = null },
            { it.data.lessons = null },
            { it.data.reviews = null },
            { it.data_updated_at = null },
            { it.data.reviews[0].available_at = null },
            { it.data.reviews[0].subject_ids = null },
            { it.data.lessons[0].subject_ids = null },
            { it.data.reviews.add(null) },
            { it.data.lessons.add(null) },
            { it.data.reviews[0].subject_ids.add(null) },
        )
        mutations.forEach { mutate ->
            val response = summaryFixture().also(mutate)
            assertThrows(InvalidSummaryException::class.java) { mapStudyQueue(response) }
        }
    }

    @Test
    fun `invalid timestamps fail clearly`() {
        val mutations: List<(SummaryRequest) -> Unit> = listOf(
            { it.data_updated_at = "not-a-date" },
            { it.data.reviews[0].available_at = "not-a-date" },
            { it.data.next_reviews_at = "not-a-date" },
        )
        mutations.forEach { mutate ->
            assertThrows(InvalidSummaryException::class.java) {
                mapStudyQueue(summaryFixture().also(mutate))
            }
        }
    }
}

internal fun summaryJson(): String =
    checkNotNull(StudyQueueMapperTest::class.java.getResource("/summary.json")).readText()

internal fun summaryFixture(): SummaryRequest = Gson().fromJson(summaryJson(), SummaryRequest::class.java)
