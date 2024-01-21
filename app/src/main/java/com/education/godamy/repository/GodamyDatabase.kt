package com.education.godamy.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [User::class,Paths::class], version = 1, exportSchema = false)
abstract class GodamyDatabase :RoomDatabase() {
    abstract val dao: GodamyDao
    companion object {
        @Volatile
        private lateinit var INSTANCE: GodamyDatabase
        fun getInstance(context: Context): GodamyDatabase {
            synchronized(this) {
                if (!::INSTANCE.isInitialized) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                        GodamyDatabase::class.java,
                        "godamy_database").fallbackToDestructiveMigration().allowMainThreadQueries().build()
                }
                return INSTANCE
            }
        }}
}