package com.example.chess.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Puzzle::class], version = 2, exportSchema = false)
abstract class PuzzleDatabase : RoomDatabase() {

    abstract val puzzleDao: PuzzleDao

    companion object {
        @Volatile
        private var INSTANCE: PuzzleDatabase? = null
        fun getInstance(context: Context): PuzzleDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        PuzzleDatabase::class.java,
                        "puzzle_database"
                    ).fallbackToDestructiveMigration().build()
                }
                return instance
            }
        }
    }
}