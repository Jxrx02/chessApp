package com.example.chess.dto

import kotlinx.serialization.Serializable

@Serializable
data class PuzzleDto(
    val game: GameDto,
    val puzzle: PuzzleInfoDto,
)

@Serializable
data class GameDto(
    val id: String,
    val rated: Boolean,
    val pgn: String,
)

@Serializable
data class PuzzleInfoDto(
    val id: String,
    val rating: Int,
    val plays: Int,
    val solution: List<String>
)
