package com.example.data.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages ORDER BY id ASC")
    fun getAllEntities(): Flow<List<ChatEntity>>

    @Insert
    suspend fun insert(entity: ChatEntity): Long


    @Update
    suspend fun update(entity: ChatEntity)
}