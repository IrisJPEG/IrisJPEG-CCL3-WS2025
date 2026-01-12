package com.example.ccl3_ws2025_mindflow.data.notes

import kotlinx.coroutines.flow.Flow

class NoteRepository(private val dao: NoteDao) {

    fun observeNoteForDate(dateKey: String): Flow<NoteEntity?> =
        dao.observeByDate(dateKey)

    suspend fun getNoteForDate(dateKey: String): NoteEntity? =
        dao.getByDate(dateKey)

    suspend fun saveNote(dateKey: String, text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            dao.deleteByDate(dateKey)
        } else {
            dao.upsert(NoteEntity(dateKey = dateKey, text = trimmed))
        }
    }
}
