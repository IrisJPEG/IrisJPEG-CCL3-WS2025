package com.example.ccl3_ws2025_mindflow.data.mood

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


data class MoodDay(
    val dateKey: String,
    val mood: MoodType
)

class MoodRepository(private val dao: MoodDao) {

    fun observeMoodForDate(dateKey: String): Flow<MoodType?> =
        dao.observeByDate(dateKey).map { entity ->
            entity?.let { MoodType.fromIndex(it.moodIndex) }
        }

    suspend fun getMoodForDate(dateKey: String): MoodType? =
        dao.getByDate(dateKey)?.let { MoodType.fromIndex(it.moodIndex) }

    suspend fun saveMood(dateKey: String, mood: MoodType) {
        dao.upsert(MoodEntity(dateKey = dateKey, moodIndex = mood.index))
    }

    // For Mood Journey screen
    fun observeAllMoods(): Flow<List<MoodDay>> =
        dao.observeAll().map { list ->
            list.map { MoodDay(dateKey = it.dateKey, mood = MoodType.fromIndex(it.moodIndex)) }
        }


}
