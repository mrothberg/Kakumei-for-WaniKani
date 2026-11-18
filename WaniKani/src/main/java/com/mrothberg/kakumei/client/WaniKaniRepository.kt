package com.mrothberg.kakumei.client

import com.mrothberg.kakumei.wkamodels.StudyQueue

interface WaniKaniRepository {
    suspend fun getStudyQueue(): StudyQueue
}

class DefaultWaniKaniRepository(
    private val service: WaniKaniRemoteService,
) : WaniKaniRepository {
    override suspend fun getStudyQueue(): StudyQueue = mapStudyQueue(service.getSummary())
}
