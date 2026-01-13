package com.example.ccl3_ws2025_mindflow.ui.moodjourney

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ccl3_ws2025_mindflow.data.mood.MoodDay
import com.example.ccl3_ws2025_mindflow.data.mood.MoodRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class MoodJourneyUiState(
    val days: List<MoodDay> = emptyList()
)

class MoodJourneyViewModel(
    moodRepo: MoodRepository
) : ViewModel() {

    val uiState: StateFlow<MoodJourneyUiState> =
        moodRepo.observeAllMoods()
            .map { list ->
                MoodJourneyUiState(days = list)
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                MoodJourneyUiState()
            )
}
