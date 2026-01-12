package com.example.ccl3_ws2025_mindflow.data.notes

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes WHERE dateKey = :dateKey LIMIT 1")
    suspend fun getByDate(dateKey: String): NoteEntity?

    @Query("SELECT * FROM notes WHERE dateKey = :dateKey LIMIT 1")
    fun observeByDate(dateKey: String): Flow<NoteEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: NoteEntity)

    @Query("DELETE FROM notes WHERE dateKey = :dateKey")
    suspend fun deleteByDate(dateKey: String)
}
