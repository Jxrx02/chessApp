package com.example.chess.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "puzzle_table")
data class Puzzle(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val puzzleId: String,
    val rating: Int,
    val plays: Int,
    val pgn: String,
    val solution: String

)