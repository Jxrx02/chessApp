package com.example.chess.view.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chess.database.Puzzle
import com.example.chess.database.PuzzleDatabase
import com.example.chess.database.PuzzleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private lateinit var repository: PuzzleRepository

    var puzzleText by mutableStateOf("")
    lateinit var puzzles: Flow<List<Puzzle>>

    fun initialize(database: PuzzleDatabase) {
        repository = PuzzleRepository(database.puzzleDao)
        puzzles = repository.allPuzzles
    }

    //TODO: Hier API anbinden
    fun insert(puzzle: Puzzle) = viewModelScope.launch {
        repository.insert(puzzle)
        puzzleText = ""
    }

    fun deleteAll() = viewModelScope.launch {
        repository.deleteAll()
        puzzleText = ""
    }

    fun delete(puzzle: Puzzle) = viewModelScope.launch {
        repository.delete(puzzle)

    }
}