package com.example.chess.dto

import kotlinx.serialization.Serializable

@Serializable
data class PuzzleDto(
    val id: Int,
    val puzzleId: String,
    val rating: Int,
    val plays: Int,
    val pgn: String,
    val solution: String
)
