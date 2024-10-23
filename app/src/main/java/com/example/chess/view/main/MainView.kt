package com.example.chess.view.main

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.chess.R
import com.example.chess.database.Puzzle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainView(viewModel: MainViewModel, navController: NavController) {
    val puzzles by viewModel.puzzles.collectAsState(initial = emptyList())

    Scaffold(topBar = { TopAppBar(title = { Text("Puzzle") }) }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            InputSection(puzzleText = viewModel.puzzleText, puzzles = puzzles, onInsert = {
                viewModel.insert(
                    puzzle = Puzzle(
                        0,
                        viewModel.puzzleText,
                        440,
                        0,
                        "Nf3 e6 d4 d6 Nbd2",
                        ""
                    )
                )
            },
                changeNoteText = {
                    viewModel.puzzleText = it
                },
                onDeleteAll = {
                    if (puzzles.isNotEmpty()) {
                        viewModel.deleteAll()
                    }
                })

            NoteList(puzzles = puzzles,
                onClickPuzzle = {
                    Log.i("Puzzle", "Auf Item ${it} wurde geklickt")
                    viewModel.selectedPuzzle = it // Puzzle im ViewModel speichern
                    navController.navigate("ChessBoardView")
                },
                onDeletePuzzle = {
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
            Text("Nach Puzzle suchen")
        }

    }
    AnimatedVisibility(
        puzzles.isNotEmpty()
    ) {
        Button(onClick = { onDeleteAll(puzzles) }, modifier = Modifier.padding(top = 10.dp)) {
            Text("Alle gespeicherten Puzzles löschen")
        }

    }

}

@Composable
fun NoteList(
    puzzles: List<Puzzle>,
    onClickPuzzle: (Puzzle) -> (Unit),
    onDeletePuzzle: (Puzzle) -> (Unit),
) {
    LazyColumn {
        items(puzzles.size) { index ->
            val puzzle = puzzles[index]

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp)) // Abgerundete Ecken
                    .background(MaterialTheme.colorScheme.primary) // Hintergrundfarbe
                    .fillMaxWidth()
                    .padding(16.dp) // Innenabstand
                    .padding(bottom = 5.dp) // Abstand zwischen den Zeilen
                    .clickable {
                        onClickPuzzle(puzzle)
                    }
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp) // Abstand innerhalb der Spalte
                ) {
                    Text(
                        text = "Puzzle ${index + 1}",
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Text(
                        text = puzzle.toString(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                OutlinedButton(
                    modifier = Modifier.padding(top = 2.dp),
                    onClick = { onDeletePuzzle(puzzle) }
                ) {
                    Text(
                        stringResource(R.string.delete),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

