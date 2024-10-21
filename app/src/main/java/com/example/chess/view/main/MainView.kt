package com.example.chess.view.main

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.chess.database.Puzzle

//TODO: Chessboard hier in die Klasse reinmachen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainView(viewModel: MainViewModel) {
    val puzzles by viewModel.puzzles.collectAsState(initial = emptyList())

    Scaffold(topBar = { TopAppBar(title = { Text("Puzzle") }) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding),
        ) {
            InputSection(puzzleText = viewModel.puzzleText, puzzles = puzzles, onInsert = {
                viewModel.insert(puzzle = Puzzle(0, "20", 440, 0, "e4", ""))
            },
                changeNoteText = {
                    viewModel.puzzleText = it
                },
                onDeleteAll = {
                    if (puzzles.isNotEmpty()) {
                        viewModel.deleteAll()
                    }
                })

            NoteList(puzzles = puzzles, onDeleteNote = {
                viewModel.delete(it)
            })
        }

    }
}

@Composable
fun InputSection(
    puzzleText: String,
    puzzles: List<Puzzle>,
    changeNoteText: (String) -> (Unit),
    onInsert: () -> (Unit),
    onDeleteAll: (List<Puzzle>) -> (Unit)
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = puzzleText,
            onValueChange = { changeNoteText(it) },
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = { onInsert() }, modifier = Modifier.padding(top = 10.dp)) {
            Text("Neue Notiz hinzufügen")
        }

    }
    AnimatedVisibility(
        puzzles.isNotEmpty()
    ) {
        Button(onClick = { onDeleteAll(puzzles) }, modifier = Modifier.padding(top = 10.dp)) {
            Text("Alle Notizen löschen")
        }

    }

}

@Composable
fun NoteList(puzzles: List<Puzzle>, onDeleteNote: (Puzzle) -> (Unit)) {
    LazyColumn {
        items(puzzles.size) { puzzle ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.onPrimary) // Hintergrundfarbe
                    .padding(16.dp) // Innenabstand
                    .padding(bottom = 5.dp) // Abstand zwischen den Zeilen
                    .clickable {
                        Log.i("Puzzle", "Auf Item ${puzzle + 1} wurde geklickt")
                        onDeleteNote(puzzles[puzzle])
                    }
            ) {
                Text(
                    text = puzzle.toString(),
                    modifier = Modifier.weight(1f) // Text nimmt den verfügbaren Platz ein
                )
            }
        }
    }
}
