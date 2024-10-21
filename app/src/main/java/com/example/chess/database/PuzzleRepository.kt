package com.example.chess.database

class PuzzleRepository(private val puzzleDao: PuzzleDao) {
    val allPuzzles = puzzleDao.selectAllPuzzlesSortedById()

    suspend fun insert(puzzle: Puzzle) {
        puzzleDao.insert(puzzle)
    }

    suspend fun deleteAll() {
        puzzleDao.deleteAll()
    }

    suspend fun delete(puzzle: Puzzle) {
        puzzleDao.delete(puzzle)
    }
}