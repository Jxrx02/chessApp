package com.example.chess

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.chess.database.Puzzle
import com.example.chess.view.main.MainViewModel
import kotlin.math.abs

// Schachbrett mit Bild-Ressourcen
val initialBoardWithImages = arrayOf(
    arrayOf(
        R.drawable.black_rook,
        R.drawable.black_knight,
        R.drawable.black_bishop,
        R.drawable.black_queen,
        R.drawable.black_king,
        R.drawable.black_bishop,
        R.drawable.black_knight,
        R.drawable.black_rook
    ),
    arrayOf(
        R.drawable.black_pawn,
        R.drawable.black_pawn,
        R.drawable.black_pawn,
        R.drawable.black_pawn,
        R.drawable.black_pawn,
        R.drawable.black_pawn,
        R.drawable.black_pawn,
        R.drawable.black_pawn
    ),
    arrayOf(0, 0, 0, 0, 0, 0, 0, 0),
    arrayOf(0, 0, 0, 0, 0, 0, 0, 0),
    arrayOf(0, 0, 0, 0, 0, 0, 0, 0),
    arrayOf(0, 0, 0, 0, 0, 0, 0, 0),
    arrayOf(
        R.drawable.white_pawn,
        R.drawable.white_pawn,
        R.drawable.white_pawn,
        R.drawable.white_pawn,
        R.drawable.white_pawn,
        R.drawable.white_pawn,
        R.drawable.white_pawn,
        R.drawable.white_pawn
    ),
    arrayOf(
        R.drawable.white_rook,
        R.drawable.white_knight,
        R.drawable.white_bishop,
        R.drawable.white_queen,
        R.drawable.white_king,
        R.drawable.white_bishop,
        R.drawable.white_knight,
        R.drawable.white_rook
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChessBoardView(viewModel: MainViewModel, navController: NavController) {

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Puzzle - ${viewModel.selectedPuzzle?.puzzleId}") },
            navigationIcon = {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.padding(end = 8.dp) // Optional padding for better spacing
                ) {
                    Text("<")
                }
            })
    }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Dein Puzzle-Inhalt
            val puzzle = viewModel.selectedPuzzle
            if (puzzle != null) {
                Text(text = "Puzzle: ${puzzle.toString()}")
            } else {
                Text(text = "Kein Puzzle ausgewählt")
            }

            if (puzzle != null) {
                ChessBoard(puzzle = puzzle)
            }

        }
    }
}


@Composable
fun ChessBoard(puzzle: Puzzle) {
    var boardState by remember { mutableStateOf(initialBoardWithImages) }
    var selectedPiece by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var possibleMoves by remember { mutableStateOf<List<Pair<Int, Int>>>(emptyList()) }
    var currentPlayer by remember { mutableStateOf(true) } // true = Weiß, false = Schwarz
    var halfMoveClock by remember { mutableStateOf(0) }  // Halbzüge seit dem letzten Schlagen oder Bauernzug
    var fullMoveNumber by remember { mutableStateOf(1) } // Zähler für volle Züge
    val context = LocalContext.current // Kontext für den Toast

    // FEN-Notation initialisieren
    var fenNotation by remember {
        mutableStateOf(
            calculateFEN(
                boardState,
                currentPlayer,
                halfMoveClock,
                fullMoveNumber
            )
        )
    }

    // PGN in Moves umwandeln
    val pgnMoves = parsePgnToMoves(puzzle.pgn)



    Column {
        // FEN-Notation anzeigen
        Text(
            text = "FEN: $fenNotation",
            modifier = Modifier.padding(16.dp)
        )

        // Schachbrett anzeigen
        for (row in 0..7) {
            Row {
                for (col in 0..7) {
                    val isPossibleMove = possibleMoves.contains(Pair(row, col))

                    ChessTileWithPiece(
                        row = row,
                        col = col,
                        pieceResId = boardState[row][col],
                        isSelected = selectedPiece == Pair(row, col),
                        isPossibleMove = isPossibleMove,
                        onTileClick = { clickedRow, clickedCol ->
                            val piece = boardState[clickedRow][clickedCol]

                            if (selectedPiece == null && piece != 0 && isCorrectPlayer(
                                    piece,
                                    currentPlayer
                                )
                            ) {
                                // Figur auswählen
                                selectedPiece = Pair(clickedRow, clickedCol)
                                Toast.makeText(
                                    context,
                                    selectedPiece.toString(),
                                    Toast.LENGTH_SHORT
                                ).show()

                                possibleMoves = getPossibleMoves(boardState, clickedRow, clickedCol)
                            } else if (selectedPiece != null) {
                                val (selectedRow, selectedCol) = selectedPiece!!
                                val selectedPieceType = boardState[selectedRow][selectedCol]

                                if (isValidMove(
                                        boardState,
                                        selectedRow,
                                        selectedCol,
                                        clickedRow,
                                        clickedCol,
                                        selectedPieceType
                                    )
                                ) {
                                    // Zug ausführen
                                    boardState = boardState.movePiece(
                                        selectedRow,
                                        selectedCol,
                                        clickedRow,
                                        clickedCol
                                    )

                                    // Halbzugzähler und Vollzugzähler aktualisieren
                                    if (selectedPieceType == R.drawable.white_pawn || selectedPieceType == R.drawable.black_pawn || boardState[clickedRow][clickedCol] != 0) {
                                        halfMoveClock =
                                            0 // Setze Halbzugzähler zurück, wenn ein Bauer gezogen oder eine Figur geschlagen wurde
                                    } else {
                                        halfMoveClock++
                                    }

                                    if (!currentPlayer) {
                                        fullMoveNumber++ // Voller Zug abgeschlossen, wenn Schwarz gezogen hat
                                    }

                                    // Spieler wechseln
                                    currentPlayer = !currentPlayer

                                    // FEN-Notation aktualisieren
                                    fenNotation = calculateFEN(
                                        boardState,
                                        currentPlayer,
                                        halfMoveClock,
                                        fullMoveNumber
                                    )
                                }

                                // Auswahl zurücksetzen
                                selectedPiece = null
                                possibleMoves = emptyList()
                            }
                        }
                    )

                    // Züge nacheinander ausführen
                    LaunchedEffect(pgnMoves) {
                        pgnMoves.forEach { move ->
                            // Führe Zug aus
                            boardState = executeMovesFromPgn(move, boardState, currentPlayer)
                            // Spieler wechseln
                            currentPlayer = !currentPlayer
                        }
                    }


                }
            }
        }

        // Zeige den aktuellen Spieler an
        Text(
            text = if (currentPlayer) "Weiß am Zug" else "Schwarz am Zug",
            modifier = Modifier.padding(16.dp)
        )

    }
}


// Hilfsfunktion, um zu prüfen, ob der aktuelle Spieler die Figur bewegen darf
fun isCorrectPlayer(piece: Int, currentPlayer: Boolean): Boolean {
    return if (currentPlayer) {
        // Weißer Spieler, prüfe ob die Figur weiß ist
        piece in listOf(
            R.drawable.white_pawn,
            R.drawable.white_rook,
            R.drawable.white_knight,
            R.drawable.white_bishop,
            R.drawable.white_queen,
            R.drawable.white_king
        )
    } else {
        // Schwarzer Spieler, prüfe ob die Figur schwarz ist
        piece in listOf(
            R.drawable.black_pawn,
            R.drawable.black_rook,
            R.drawable.black_knight,
            R.drawable.black_bishop,
            R.drawable.black_queen,
            R.drawable.black_king
        )
    }
}


@Composable
fun ChessTileWithPiece(
    row: Int,
    col: Int,
    pieceResId: Int,
    isSelected: Boolean,
    isPossibleMove: Boolean,
    onTileClick: (Int, Int) -> Unit
) {
    val isLightTile = (row + col) % 2 == 0
    val backgroundColor = when {
        isPossibleMove -> Color.Red // Möglicher Zug
        isSelected -> Color.Yellow // Ausgewählte Figur
        isLightTile -> Color.LightGray
        else -> Color.DarkGray
    }

    Box(
        modifier = Modifier
            .size(48.dp)
            .background(color = backgroundColor)
            .clickable { onTileClick(row, col) },
        contentAlignment = Alignment.Center
    ) {
        if (pieceResId != 0) {
            Image(
                painter = painterResource(id = pieceResId),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}


// Funktion, die die möglichen Züge für eine Figur berechnet
fun getPossibleMoves(board: Array<Array<Int>>, row: Int, col: Int): List<Pair<Int, Int>> {
    val piece = board[row][col]
    val possibleMoves = mutableListOf<Pair<Int, Int>>()

    for (toRow in 0..7) {
        for (toCol in 0..7) {
            if (isValidMove(board, row, col, toRow, toCol, piece)) {
                possibleMoves.add(Pair(toRow, toCol))
            }
        }
    }
    return possibleMoves
}

// Funktion, die das Verschieben der Figur umsetzt
fun Array<Array<Int>>.movePiece(
    fromRow: Int,
    fromCol: Int,
    toRow: Int,
    toCol: Int
): Array<Array<Int>> {
    val newBoard = this.map { it.clone() }.toTypedArray()
    newBoard[toRow][toCol] = newBoard[fromRow][fromCol] // Figur verschieben
    newBoard[fromRow][fromCol] = 0 // Altes Feld leeren
    return newBoard
}


fun isValidMove(
    board: Array<Array<Int>>, // Aktuelles Schachbrett
    fromRow: Int, fromCol: Int, toRow: Int, toCol: Int,
    piece: Int // Die Figur, die bewegt wird
): Boolean {
    val isWhite = when (piece) {
        R.drawable.white_pawn, R.drawable.white_rook,
        R.drawable.white_knight, R.drawable.white_bishop,
        R.drawable.white_queen, R.drawable.white_king -> true

        R.drawable.black_pawn, R.drawable.black_rook,
        R.drawable.black_knight, R.drawable.black_bishop,
        R.drawable.black_queen, R.drawable.black_king -> false

        else -> return false // Ungültige Figur
    }

    // Verhindere das Bewegen auf ein Feld mit einer eigenen Figur
    val targetPiece = board[toRow][toCol]
    if (targetPiece != 0) { // Wenn das Zielfeld nicht leer ist
        val targetIsWhite = when (targetPiece) {
            R.drawable.white_pawn, R.drawable.white_rook,
            R.drawable.white_knight, R.drawable.white_bishop,
            R.drawable.white_queen, R.drawable.white_king -> true

            R.drawable.black_pawn, R.drawable.black_rook,
            R.drawable.black_knight, R.drawable.black_bishop,
            R.drawable.black_queen, R.drawable.black_king -> false

            else -> return false
        }
        if (isWhite == targetIsWhite) return false // Wenn das Zielfeld eine eigene Figur hat, ist der Zug ungültig
    }

    return when (piece) {
        R.drawable.white_pawn, R.drawable.black_pawn -> isValidPawnMove(
            board,
            fromRow,
            fromCol,
            toRow,
            toCol,
            isWhite
        )

        R.drawable.white_rook, R.drawable.black_rook -> isValidRookMove(
            board,
            fromRow,
            fromCol,
            toRow,
            toCol
        )

        R.drawable.white_knight, R.drawable.black_knight -> isValidKnightMove(
            fromRow,
            fromCol,
            toRow,
            toCol
        )

        R.drawable.white_bishop, R.drawable.black_bishop -> isValidBishopMove(
            board,
            fromRow,
            fromCol,
            toRow,
            toCol
        )

        R.drawable.white_queen, R.drawable.black_queen -> isValidQueenMove(
            board,
            fromRow,
            fromCol,
            toRow,
            toCol
        )

        R.drawable.white_king, R.drawable.black_king -> isValidKingMove(
            fromRow,
            fromCol,
            toRow,
            toCol
        )

        else -> false
    }
}


fun isValidPawnMove(
    board: Array<Array<Int>>,
    fromRow: Int, fromCol: Int,
    toRow: Int, toCol: Int,
    isWhite: Boolean
): Boolean {
    val direction =
        if (isWhite) -1 else 1 // Weiße Bauern bewegen sich nach oben (-1), schwarze nach unten (+1)
    val startRow =
        if (isWhite) 6 else 1 // Weiße Bauern starten in der Reihe 6, schwarze in der Reihe 1

    // Normale Bewegung nach vorne
    if (fromCol == toCol && board[toRow][toCol] == 0) {
        if (toRow == fromRow + direction) {
            return true
        }
        // Doppelschritt bei erstem Zug
        if (fromRow == startRow && toRow == fromRow + 2 * direction && board[fromRow + direction][toCol] == 0) {
            return true
        }
    }

    // Schlagen (diagonale Bewegung)
    if (abs(fromCol - toCol) == 1 && toRow == fromRow + direction && board[toRow][toCol] != 0) {
        return true
    }

    return false
}

// Beispiel: Überprüfe Turm-Zug
fun isValidRookMove(
    board: Array<Array<Int>>,
    fromRow: Int, fromCol: Int,
    toRow: Int, toCol: Int
): Boolean {
    if (fromRow != toRow && fromCol != toCol) return false
    // Überprüfen, ob der Weg blockiert ist
    if (fromRow == toRow) {
        val minCol = minOf(fromCol, toCol)
        val maxCol = maxOf(fromCol, toCol)
        for (col in minCol + 1 until maxCol) {
            if (board[fromRow][col] != 0) return false
        }
    } else {
        val minRow = minOf(fromRow, toRow)
        val maxRow = maxOf(fromRow, toRow)
        for (row in minRow + 1 until maxRow) {
            if (board[row][fromCol] != 0) return false
        }
    }
    return true
}

// Beispiel: Überprüfe Springer-Zug
fun isValidKnightMove(fromRow: Int, fromCol: Int, toRow: Int, toCol: Int): Boolean {
    val rowDiff = abs(fromRow - toRow)
    val colDiff = abs(fromCol - toCol)
    return (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2)
}

// Beispiel: Überprüfe Läufer-Zug
fun isValidBishopMove(
    board: Array<Array<Int>>,
    fromRow: Int, fromCol: Int,
    toRow: Int, toCol: Int
): Boolean {
    if (abs(fromRow - toRow) != abs(fromCol - toCol)) return false

    val rowDirection = if (toRow > fromRow) 1 else -1
    val colDirection = if (toCol > fromCol) 1 else -1

    var row = fromRow + rowDirection
    var col = fromCol + colDirection
    while (row != toRow && col != toCol) {
        if (row !in 0..7 || col !in 0..7) return false
        if (board[row][col] != 0) return false
        row += rowDirection
        col += colDirection
    }
    return true
}

// Beispiel: Überprüfe Dame-Zug (Kombination aus Turm und Läufer)
fun isValidQueenMove(
    board: Array<Array<Int>>,
    fromRow: Int, fromCol: Int,
    toRow: Int, toCol: Int
): Boolean {
    return isValidRookMove(board, fromRow, fromCol, toRow, toCol) || isValidBishopMove(
        board,
        fromRow,
        fromCol,
        toRow,
        toCol
    )
}

// Beispiel: Überprüfe König-Zug
fun isValidKingMove(fromRow: Int, fromCol: Int, toRow: Int, toCol: Int): Boolean {
    val rowDiff = abs(fromRow - toRow)
    val colDiff = abs(fromCol - toCol)
    return rowDiff <= 1 && colDiff <= 1
}


fun calculateFEN(
    board: Array<Array<Int>>,
    currentPlayer: Boolean,
    halfMoveClock: Int,
    fullMoveNumber: Int
): String {
    val pieceMap = mapOf(
        R.drawable.white_pawn to "P",
        R.drawable.white_rook to "R",
        R.drawable.white_knight to "N",
        R.drawable.white_bishop to "B",
        R.drawable.white_queen to "Q",
        R.drawable.white_king to "K",
        R.drawable.black_pawn to "p",
        R.drawable.black_rook to "r",
        R.drawable.black_knight to "n",
        R.drawable.black_bishop to "b",
        R.drawable.black_queen to "q",
        R.drawable.black_king to "k"
    )

    var fen = ""

    // Iteriere über jede Reihe des Schachbretts
    for (row in board) {
        var emptyCount = 0

        for (piece in row) {
            if (piece == 0) {
                emptyCount++
            } else {
                if (emptyCount > 0) {
                    fen += emptyCount
                    emptyCount = 0
                }
                fen += pieceMap[piece] ?: ""
            }
        }

        if (emptyCount > 0) {
            fen += emptyCount
        }

        fen += "/"
    }

    fen = fen.dropLast(1) // Entferne das letzte "/"

    // Spieleranzeige (w = Weiß, b = Schwarz)
    fen += if (currentPlayer) " w " else " b "

    // Rochaderechte und En passant sind momentan auf Standard gesetzt
    fen += "- - "

    // Halbzugzähler und Zugzähler
    fen += "$halfMoveClock $fullMoveNumber"

    return fen
}


fun parsePgnToMoves(pgn: String): List<String> {
    return pgn.split(" ").filter { it.isNotEmpty() && !it.contains(".") }
}

fun executeMovesFromPgn(
    move: String,
    board: Array<Array<Int>>,
    currentPlayer: Boolean
): Array<Array<Int>> {

    val moveCoords = convertPgnPawnMoveToCoords(move, currentPlayer)

    if (moveCoords != null) {
        val (from, to) = moveCoords

        val piece = board[from.first][from.second]
        println(
            move + " " + "piece: " + piece + " " + from.first + "|" + from.second + " " + to.first + "|" + to.second
        )

        if (isValidMove(board, from.first, from.second, to.first, to.second, piece)) {
            // Führe den Zug aus
            return board.movePiece(from.first, from.second, to.first, to.second)

        }
    }
    return board
}

fun convertPgnPawnMoveToCoords(
    move: String,
    isWhite: Boolean
): Pair<Pair<Int, Int>, Pair<Int, Int>>? {
    val fileMap =
        mapOf('a' to 0, 'b' to 1, 'c' to 2, 'd' to 3, 'e' to 4, 'f' to 5, 'g' to 6, 'h' to 7)


    // Bauernzüge haben nur zwei Zeichen, wie 'e4' oder 'c5'
    if (move.length == 2) {
        try {
            // Das Zielfeld
            val endFile = fileMap[move[0]] ?: return null // Der Buchstabe des Zielfelds (Spalte)
            val endRank = 8 - move[1].digitToInt()         // Die Zahl des Zielfelds (Zeile)

            // Der Startpunkt hängt von der Farbe des Spielers ab
            val startRank =
                if (isWhite) 6 else 1          // Weiß startet von Reihe 6, Schwarz von Reihe 1

            // Rückgabe des Zugpaars als ((startRow, startCol), (endRow, endCol))
            return Pair(Pair(startRank, endRank), Pair(endFile, endRank))
        } catch (e: Exception) {
            return null
        }
    }


    if (move.length >= 4) {
        try {
            val startFile = fileMap[move[0]] ?: return null
            val startRank = 8 - move[1].digitToInt()
            val endFile = fileMap[move[2]] ?: return null
            val endRank = 8 - move[3].digitToInt()

            return Pair(Pair(startRank, startFile), Pair(endRank, endFile))
        } catch (e: Exception) {
            return null
        }
    }

    // Zusätzliche Fälle wie Schlagen (Nxe5, Bxf6) können implementiert werden, hier wird das 'x' ignoriert
    if (move.contains("x") && move.length >= 5) {
        try {
            val startFile = fileMap[move[0]] ?: return null
            val startRank = 8 - move[1].digitToInt()
            val endFile = fileMap[move[3]] ?: return null
            val endRank = 8 - move[4].digitToInt()

            return Pair(Pair(startRank, startFile), Pair(endRank, endFile))
        } catch (e: Exception) {
            return null
        }
    }

    // Rückgabe `null` für nicht unterstützte Fälle
    return null
}

