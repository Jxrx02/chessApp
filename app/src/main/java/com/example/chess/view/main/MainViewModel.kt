package com.example.chess.view.main

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chess.database.Puzzle
import com.example.chess.database.PuzzleDatabase
import com.example.chess.database.PuzzleRepository
import com.example.chess.dto.PuzzleDto
import com.example.chess.service.PuzzleService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
class MainViewModel : ViewModel() {
    private lateinit var repository: PuzzleRepository

    var selectedPuzzle: Puzzle? by mutableStateOf(null)
    var puzzleText by mutableStateOf("")
    val puzzles: Flow<List<Puzzle>> get() = repository.allPuzzles
    var errorMessage: String by mutableStateOf("")
    var loading: Boolean by mutableStateOf(false)
    private val puzzleService = PuzzleService()
    var puzzle: PuzzleDto by mutableStateOf(PuzzleDto(0, "", 0, 0, "", ""))

    fun initialize(database: PuzzleDatabase) {
        repository = PuzzleRepository(database.puzzleDao)
    }

    fun insert() = viewModelScope.launch {
        errorMessage = ""
        loading = true
        Log.w("Puzzle", "Fetching puzzle")
        try {
            val puzzleRequest = puzzleService.getPuzzle(puzzleText)
            Log.i("Puzzle", "$puzzleText $puzzleRequest from API")
            loading = false
            puzzle = puzzleRequest

            // Convert PuzzleDto to Puzzle and insert
            val puzzleDatabase = Puzzle(
                puzzle.id,
                puzzle.puzzleId,
                puzzle.rating,
                puzzle.plays,
                puzzle.pgn,
                puzzle.solution
            )

            repository.insert(puzzleDatabase)
            puzzleText = ""
        } catch (e: Exception) {
            loading = false
            errorMessage = e.message.toString()
            Log.e("Puzzle", errorMessage)
        }
    }

    fun deleteAll() = viewModelScope.launch {
        repository.deleteAll()
        puzzleText = ""
    }

    fun delete(puzzle: Puzzle) = viewModelScope.launch {
        repository.delete(puzzle)
    }
}
}