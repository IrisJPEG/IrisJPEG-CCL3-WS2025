package com.example.ccl3_ws2025_mindflow.data.mood

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "moods")
data class MoodEntity(
    @PrimaryKey val dateKey: String, // yyyy-MM-dd
    val moodIndex: Int // 0..4
)

enum class MoodType(val index: Int, val emoji: String) {
    VERY_SAD(0, "😞"),
    SAD(1, "😕"),
    NEUTRAL(2, "😐"),
    HAPPY(3, "🙂"),
    VERY_HAPPY(4, "😄");

    companion object {
        fun fromIndex(index: Int): MoodType = entries.first { it.index == index }
    }
}
