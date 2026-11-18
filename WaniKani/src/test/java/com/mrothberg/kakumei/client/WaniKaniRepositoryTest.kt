package com.mrothberg.kakumei.client

import com.google.gson.JsonParseException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import okhttp3.Call
import okhttp3.EventListener
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.TimeUnit

class WaniKaniRepositoryTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: WaniKaniRepository
    private val callCanceled = CompletableDeferred<Unit>()

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val retrofit = WaniKaniServiceV2Builder("test-token").buildRetrofit()
        val client = (retrofit.callFactory() as OkHttpClient).newBuilder()
            .retryOnConnectionFailure(false)
            .eventListener(object : EventListener() {
                override fun canceled(call: Call) {
                    callCanceled.complete(Unit)
                }
            })
            .build()
        val service = retrofit.newBuilder()
            .baseUrl(server.url("/v2/"))
            .client(client)
            .build()
            .create(WaniKaniRemoteService::class.java)
        repository = DefaultWaniKaniRepository(service)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test(timeout = 10000)
    fun `fetch sends authenticated summary request and maps JSON`() = runBlocking {
        server.enqueue(MockResponse().setBody(summaryJson()))
        val queue = repository.getStudyQueue()
        assertEquals(3, queue.reviews_available)
        val request = server.takeRequest(2, TimeUnit.SECONDS)
        assertNotNull(request)
        assertEquals("GET", request!!.method)
        assertEquals("/v2/summary", request.path)
        assertEquals("Bearer test-token", request.getHeader("Authorization"))
        assertEquals("20170710", request.getHeader("Wanikani-Revision"))
    }

    @Test(timeout = 10000)
    fun `HTTP failures retain status and rate limit metadata`() = runBlocking {
        for (status in listOf(401, 429, 500)) {
            server.enqueue(MockResponse().setResponseCode(status)
                .setHeader("RateLimit-Reset", "1790463660")
                .setBody("{\"error\":\"Request failed\",\"code\":$status}"))
            try {
                repository.getStudyQueue()
                throw AssertionError("Expected HTTP $status")
            } catch (error: HttpException) {
                assertEquals(status, error.code())
                assertEquals("1790463660", error.response()!!.headers()["RateLimit-Reset"])
            }
        }
        assertEquals(3, server.requestCount)
    }

    @Test(timeout = 10000)
    fun `network failure propagates`() = runBlocking {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))
        try {
            repository.getStudyQueue()
            throw AssertionError("Expected network failure")
        } catch (_: IOException) {
            // A transport failure must not become an empty successful queue.
        }
    }

    @Test(timeout = 10000)
    fun `invalid JSON propagates a decoding failure`() = runBlocking {
        server.enqueue(MockResponse().setBody("{\"data\": []}"))
        try {
            repository.getStudyQueue()
            throw AssertionError("Expected decoding failure")
        } catch (_: JsonParseException) {
            // Gson's decoding error remains available to the caller.
        }
    }

    @Test(timeout = 10000)
    fun `missing payload fails validation`() = runBlocking {
        server.enqueue(MockResponse().setBody("{}"))
        try {
            repository.getStudyQueue()
            throw AssertionError("Expected invalid summary")
        } catch (error: InvalidSummaryException) {
            assertEquals("Summary data is missing", error.message)
        }
    }

    @Test(timeout = 10000)
    fun `canceling caller cancels the in flight HTTP call`() = runBlocking {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
        var returned = false
        val job = launch(start = CoroutineStart.UNDISPATCHED) {
            repository.getStudyQueue()
            returned = true
        }
        try {
            assertNotNull(server.takeRequest(2, TimeUnit.SECONDS))
            job.cancelAndJoin()
            withTimeout(2000) { callCanceled.await() }
            assertTrue(job.isCancelled)
            assertTrue(!returned)
        } finally {
            job.cancelAndJoin()
        }
    }
}
