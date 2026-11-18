package com.mrothberg.kakumei.client

import com.mrothberg.kakumei.apimodels.SummaryRequest
import retrofit2.http.GET
import retrofit2.http.Headers

interface WaniKaniRemoteService {
    @Headers("Wanikani-Revision: 20170710")
    @GET("summary")
    suspend fun getSummary(): SummaryRequest

    companion object {
        fun create(apiKey: String): WaniKaniRemoteService =
            WaniKaniServiceV2Builder(apiKey).buildRetrofit()
                .create(WaniKaniRemoteService::class.java)
    }
}
