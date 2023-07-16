package com.advice.schedule.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.advice.core.local.Event
import com.advice.core.local.Speaker
import com.advice.schedule.data.repositories.SpeakerRepository
import com.advice.ui.screens.SpeakerState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


class SpeakerViewModel : ViewModel(), KoinComponent {

    private val repository by inject<SpeakerRepository>()

    private val _speakerDetails = MutableStateFlow<SpeakerState>(SpeakerState.Loading)
    val speakerDetails: Flow<SpeakerState> get() = _speakerDetails

    fun fetchSpeakerDetails(id: String?) {
        if(id == null) {
            _speakerDetails.value = SpeakerState.Error
            return
        }

        viewModelScope.launch {
            val details = repository.getSpeakerDetails(id.toLong())
            _speakerDetails.value = details
        }
    }
}