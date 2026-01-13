package com.example.ccl3_ws2025_mindflow.ui.breathing

data class BreathingExercise(
    val id: String,
    val name: String,
    val instructions: String,
    val breathInDuration: Int,
    val breathOutDuration: Int,
    val holdDuration: Int = 0 // new property in seconds
)

// predefined exercises with hold durations
val breathingExercises = listOf(
    BreathingExercise(
        id = "box",
        name = "Box Breathing",
        instructions = "Inhale, hold, exhale, hold — each for 4 seconds",
        breathInDuration = 4,
        breathOutDuration = 4,
        holdDuration = 4
    ),
    BreathingExercise(
        id = "478",
        name = "4-7-8 Breathing",
        instructions = "Inhale 4s, hold 7s, exhale 8s",
        breathInDuration = 4,
        breathOutDuration = 8,
        holdDuration = 7
    ),
    BreathingExercise(
        id = "alternate",
        name = "Alternate Nostril Breathing",
        instructions = "Close one nostril, inhale, switch, exhale",
        breathInDuration = 4,
        breathOutDuration = 4,
        holdDuration = 0
    ),
    BreathingExercise(
        id = "relaxed",
        name = "Relaxed Breathing",
        instructions = "Breathe slowly and gently through your nose",
        breathInDuration = 5,
        breathOutDuration = 5,
        holdDuration = 0
    )
)
