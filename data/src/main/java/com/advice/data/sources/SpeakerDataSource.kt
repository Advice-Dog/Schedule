package com.advice.data.sources

import com.advice.core.local.Event
import com.advice.core.local.Speaker
import kotlinx.coroutines.flow.Flow

interface SpeakerDataSource {

    fun get(): Flow<Speaker>

    fun getEvents(): Flow<List<Event>>
}
