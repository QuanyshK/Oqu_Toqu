package com.example.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ChatEntity::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
}