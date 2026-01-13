package com.example.ccl3_ws2025_mindflow.ui.breathing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BreathingViewModel : ViewModel() {
    private val _exercise = MutableStateFlow<BreathingExercise?>(null)
    val exercise: StateFlow<BreathingExercise?> = _exercise

    fun setExercise(ex: BreathingExercise) {
        _exercise.value = ex
    }
}
