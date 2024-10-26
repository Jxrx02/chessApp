package com.example.chess.service

import com.example.chess.api.APIManager
import com.example.chess.dto.PuzzleDto
import io.ktor.client.call.body
import io.ktor.client.request.get

class PuzzleService {
    suspend fun getDailyPuzzle(): PuzzleDto {
        val apiManager = APIManager("daily")
        return apiManager.jsonHttpClient.get("").body()
    }

    suspend fun getPuzzle(id: String): PuzzleDto {
        val apiManager = APIManager(id)
        return apiManager.jsonHttpClient.get("").body()
    }

}