// FILE: app/src/main/java/com/example/ccl3_ws2025_mindflow/data/mood/MoodDao.kt
package com.example.ccl3_ws2025_mindflow.data.mood

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodDao {

    @Query("SELECT * FROM moods WHERE dateKey = :dateKey LIMIT 1")
    suspend fun getByDate(dateKey: String): MoodEntity?

    @Query("SELECT * FROM moods WHERE dateKey = :dateKey LIMIT 1")
    fun observeByDate(dateKey: String): Flow<MoodEntity?>

    // NEW: observe all moods for the journey screen
    @Query("SELECT * FROM moods ORDER BY dateKey ASC")
    fun observeAll(): Flow<List<MoodEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: MoodEntity)
}
