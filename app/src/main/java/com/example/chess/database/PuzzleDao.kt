package com.example.chess.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PuzzleDao {
    @Query("SELECT * FROM puzzle_table ORDER BY puzzleId ASC")
    fun selectAllPuzzlesSortedById(): Flow<List<Puzzle>>

    @Insert
    suspend fun insert(puzzle: Puzzle)

    @Query("DELETE FROM puzzle_table")
    suspend fun deleteAll()

    @Delete
    suspend fun delete(puzzle: Puzzle)
}