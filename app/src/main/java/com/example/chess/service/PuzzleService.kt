package com.example.chess.service

import com.example.chess.api.APIManager
import com.example.chess.dto.PuzzleDto
import io.ktor.client.call.body
import io.ktor.client.request.get

class PuzzleService {
    private val apiManager = APIManager()
    suspend fun getDailyPuzzle(): PuzzleDto {
        return apiManager.jsonHttpClient.get("puzzle/daily").body()
    }

    suspend fun getPuzzle(id: String): PuzzleDto {
        return apiManager.jsonHttpClient.get("puzzle/${id}").body()
    }

}