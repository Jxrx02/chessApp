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
        R.drawable.black_rook_l,
        R.drawable.black_knight_l,
        R.drawable.black_bishop_l,
        R.drawable.black_queen,
        R.drawable.black_king,
        R.drawable.black_bishop_r,
        R.drawable.black_knight_r,
        R.drawable.black_rook_r
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
        R.drawable.white_rook_l,
        R.drawable.white_knight_l,
        R.drawable.white_bishop_l,
        R.drawable.white_queen,
        R.drawable.white_king,
        R.drawable.white_bishop_r,
        R.drawable.white_knight_r,
        R.drawable.white_rook_r
    )
)
var onload = true

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChessBoardView(viewModel: MainViewModel, navController: NavController) {
    onload = true
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
    var board by remember { mutableStateOf(initialBoardWithImages) }
    var selectedPiece by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var possibleMoves by remember { mutableStateOf<List<Pair<Int, Int>>>(emptyList()) }
    var currentPlayer by remember { mutableStateOf(true) } // true = Weiß, false = Schwarz
    var halfMoveClock by remember { mutableStateOf(0) }  // Halbzüge seit dem letzten Schlagen oder Bauernzug
    var fullMoveNumber by remember { mutableStateOf(1) } // Zähler für volle Züge
    val context = LocalContext.current // Kontext für den Toast

    if (onload) {
        val pgnMoves = parsePgnToMoves(puzzle.pgn)
        pgnMoves.forEach { move ->
            board = executeMoveFromPgn(move, board, currentPlayer)
            currentPlayer = !currentPlayer
        }
        onload = false
    }
    // FEN-Notation initialisieren
    var fenNotation by remember {
        mutableStateOf(
            calculateFEN(
                board,
                currentPlayer,
                halfMoveClock,
                fullMoveNumber
            )
        )
    }

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
                        pieceResId = board[row][col],
                        isSelected = selectedPiece == Pair(row, col),
                        isPossibleMove = isPossibleMove,
                        onTileClick = { clickedRow, clickedCol ->
                            val piece = board[clickedRow][clickedCol]

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

                                possibleMoves = getPossibleMoves(board, clickedRow, clickedCol)
                            } else if (selectedPiece != null) {
                                val (selectedRow, selectedCol) = selectedPiece!!
                                val selectedPieceType = board[selectedRow][selectedCol]

                                if (isValidMove(
                                        board,
                                        selectedRow,
                                        selectedCol,
                                        clickedRow,
                                        clickedCol,
                                        selectedPieceType
                                    )
                                ) {
                                    // Zug ausführen
                                    board = board.movePiece(
                                        selectedRow,
                                        selectedCol,
                                        clickedRow,
                                        clickedCol
                                    )

                                    // Halbzugzähler und Vollzugzähler aktualisieren
                                    if (selectedPieceType == R.drawable.white_pawn || selectedPieceType == R.drawable.black_pawn || board[clickedRow][clickedCol] != 0) {
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
                                        board,
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
            R.drawable.white_rook_l,
            R.drawable.white_rook_r,
            R.drawable.white_knight_l,
            R.drawable.white_knight_r,
            R.drawable.white_bishop_l,
            R.drawable.white_bishop_r,
            R.drawable.white_queen,
            R.drawable.white_king
        )
    } else {
        // Schwarzer Spieler, prüfe ob die Figur schwarz ist
        piece in listOf(
            R.drawable.black_pawn,
            R.drawable.black_rook_l,
            R.drawable.black_rook_r,
            R.drawable.black_knight_l,
            R.drawable.black_knight_r,
            R.drawable.black_bishop_l,
            R.drawable.black_bishop_r,
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
        if (col == 0) {
            Text(
                text = (8 - row).toString(),
                color = if (isLightTile) Color.Black else Color.White,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp)
            )
        }

        if (row == 7) {
            Text(
                text = ('a' + col).toString(),
                color = if (isLightTile) Color.Black else Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(1.dp)

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
        R.drawable.white_pawn,
        R.drawable.white_rook_l, R.drawable.white_rook_r,
        R.drawable.white_knight_l, R.drawable.white_knight_r,
        R.drawable.white_bishop_l, R.drawable.white_bishop_r,
        R.drawable.white_queen, R.drawable.white_king -> true

        R.drawable.black_pawn,
        R.drawable.black_rook_l, R.drawable.black_rook_r,
        R.drawable.black_knight_l, R.drawable.black_knight_r,
        R.drawable.black_bishop_l, R.drawable.black_bishop_r,
        R.drawable.black_queen, R.drawable.black_king -> false

        else -> return false // Ungültige Figur
    }

    // Verhindere das Bewegen auf ein Feld mit einer eigenen Figur
    val targetPiece = board[toRow][toCol]
    if (targetPiece != 0) { // Wenn das Zielfeld nicht leer ist
        val targetIsWhite = when (targetPiece) {
            R.drawable.white_pawn,
            R.drawable.white_rook_l, R.drawable.white_rook_r,
            R.drawable.white_knight_l, R.drawable.white_knight_r,
            R.drawable.white_bishop_l, R.drawable.white_bishop_r,
            R.drawable.white_queen, R.drawable.white_king -> true

            R.drawable.black_pawn,
            R.drawable.black_rook_l, R.drawable.black_rook_r,
            R.drawable.black_knight_l, R.drawable.black_knight_r,
            R.drawable.black_bishop_l, R.drawable.black_bishop_r,
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

        R.drawable.white_rook_l, R.drawable.white_rook_r,
        R.drawable.black_rook_l, R.drawable.black_rook_r -> isValidRookMove(
            board,
            fromRow,
            fromCol,
            toRow,
            toCol
        )

        R.drawable.white_knight_l, R.drawable.white_knight_r,
        R.drawable.black_knight_l, R.drawable.black_knight_r -> isValidKnightMove(
            fromRow,
            fromCol,
            toRow,
            toCol
        )

        R.drawable.white_bishop_l, R.drawable.white_bishop_r,
        R.drawable.black_bishop_l, R.drawable.black_bishop_r -> isValidBishopMove(
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
            board,
            fromRow,
            fromCol,
            toRow,
            toCol,
            false,
            false,
            isWhite
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

fun isValidKnightMove(fromRow: Int, fromCol: Int, toRow: Int, toCol: Int): Boolean {
    val rowDiff = abs(fromRow - toRow)
    val colDiff = abs(fromCol - toCol)
    if (colDiff == 0 || rowDiff == 0) return false
    return (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2)
}

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

fun isValidKingMove(
    board: Array<Array<Int>>,
    fromRow: Int, fromCol: Int,
    toRow: Int, toCol: Int,
    hasKingMoved: Boolean,
    hasRookMoved: Boolean,
    isWhite: Boolean
): Boolean {
    val rowDiff = abs(fromRow - toRow)
    val colDiff = abs(fromCol - toCol)

    // Überprüfung der Rochade
    if (!hasKingMoved && rowDiff == 0 && colDiff == 2) {
        // Kurze Rochade (O-O)
        if (toCol > fromCol) {
            val rookCol = 7
            if (!hasRookMoved && isValidRookMove(board, fromRow, fromCol, fromRow, rookCol)) {
                // Prüfen, ob der Weg frei ist und die Felder nicht bedroht sind
                return isPathClearForCastling(board, fromRow, fromCol, toCol, isWhite)
            }
        }
        // Lange Rochade (O-O-O)
        if (toCol < fromCol) {
            val rookCol = 0
            if (!hasRookMoved && isValidRookMove(board, fromRow, fromCol, fromRow, rookCol)) {
                // Prüfen, ob der Weg frei ist und die Felder nicht bedroht sind
                return isPathClearForCastling(board, fromRow, fromCol, toCol, isWhite)
            }
        }
    }
    // Normale Königszüge (ein Feld in jede Richtung)
    if (rowDiff <= 1 && colDiff <= 1) return true

    return false
}

// Hilfsfunktion zur Überprüfung der bedrohten Felder während der Rochade
fun isPathClearForCastling(
    board: Array<Array<Int>>,
    row: Int, fromCol: Int, toCol: Int,
    isWhite: Boolean
): Boolean {
    val minCol = minOf(fromCol, toCol)
    val maxCol = maxOf(fromCol, toCol)

    // Überprüfen, ob der Weg blockiert ist und die Felder nicht bedroht sind
    for (col in minCol..maxCol) {
        if (board[row][col] != 0) return false
        // Hier kannst du noch eine Funktion aufrufen, die überprüft, ob das Feld bedroht ist
        // z.B. if (isSquareAttacked(row, col, isWhite)) return false
    }
    return true
}


fun calculateFEN(
    board: Array<Array<Int>>,
    currentPlayer: Boolean,
    halfMoveClock: Int,
    fullMoveNumber: Int
): String {
    val pieceMap = mapOf(
        R.drawable.white_pawn to "P",
        R.drawable.white_rook_l to "R",
        R.drawable.white_rook_r to "R",
        R.drawable.white_knight_l to "N",
        R.drawable.white_knight_r to "N",
        R.drawable.white_bishop_l to "B",
        R.drawable.white_bishop_r to "B",
        R.drawable.white_queen to "Q",
        R.drawable.white_king to "K",

        R.drawable.black_pawn to "p",
        R.drawable.black_rook_l to "r",
        R.drawable.black_rook_r to "r",
        R.drawable.black_knight_l to "n",
        R.drawable.black_knight_r to "n",
        R.drawable.black_bishop_l to "b",
        R.drawable.black_bishop_r to "b",
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

fun executeMoveFromPgn(
    move: String,
    board: Array<Array<Int>>,
    isWhite: Boolean
): Array<Array<Int>> {

    if (move.equals("O-O") || move.equals("O-O-O")) {
        return doCastle(board, move, isWhite)
    }

    val moveCoords = convertPgnMoveToCoords(move, isWhite, board)

    if (moveCoords != null) {
        val (from, to) = moveCoords

        val piece = board[from.first][from.second]
        println(
            move + "|${isWhite}: piece: ${piece}  (${from.first}, ${from.second}) to (${to.first}, ${to.second})"
        )

        if (isValidMove(board, from.first, from.second, to.first, to.second, piece)) {
            // Führe den Zug aus
            return board.movePiece(from.first, from.second, to.first, to.second)

        }
    }
    return board
}

fun convertPgnMoveToCoords(
    move: String,
    isWhite: Boolean,
    board: Array<Array<Int>>
): Pair<Pair<Int, Int>, Pair<Int, Int>>? {
    val fileMap =
        mapOf('a' to 0, 'b' to 1, 'c' to 2, 'd' to 3, 'e' to 4, 'f' to 5, 'g' to 6, 'h' to 7)

    var move_ = move
    if (move_.contains("+")) {
        move_ = move_.replace("+", "")
        //TODO: Logik für Schach setzten hinzufügen
    }


    // Handle pawn moves (no piece letter, e.g., 'e4')
    val regex = Regex("[a-h]x[a-h][0-8]$")
    if (move_.length == 2 || move_.length == 4 && regex.matches(move_)) {
        return convertPgnPawnMoveToCoords(move_, isWhite, board)
    }

    if (move_.contains("x")) {
        move_ = move_.replace("x", "")
        //TODO: Figur wurde geschlagen, Aktualisiere geschlagene Figuren (nicht auf dem Spielfeld)
    }

    // Handle piece moves like 'Nf3', 'Qd4'
    if (move_.length >= 3) {
        val pieceType = move_.substring(0, move_.length - 2)
        val endFile = fileMap[move_[move_.length - 2]] ?: return null
        val endRank = 8 - move_[move_.length - 1].digitToInt()

        return findPieceMove(pieceType, endRank, endFile, isWhite, board, fileMap)
    }
    return null
}

fun doCastle(board: Array<Array<Int>>, move: String, isWhite: Boolean): Array<Array<Int>> {
    var board_ = board.clone()
    if (move == "O-O") {
        // Kurze Rochade
        if (isWhite) {
            // Turm
            board_ = board.movePiece(7, 7, 7, 5)
                .movePiece(7, 4, 7, 6)  // König
        } else {
            board_ = board.movePiece(0, 7, 0, 5)   // Turm
                .movePiece(0, 4, 0, 6)            // König

        }
    } else if (move == "O-O-O") {
        // Lange Rochade
        if (isWhite) {
            //Turm
            board_ = board.movePiece(7, 7, 7, 3)
                .movePiece(7, 4, 7, 2)
        } else {
            //Turm
            board_ = board.movePiece(0, 7, 0, 3)
                .movePiece(0, 4, 0, 2)
        }
    }
    return board_
}

fun findPieceMove(
    pieceType: String,
    endRank: Int,
    endFile: Int,
    isWhite: Boolean,
    board: Array<Array<Int>>,
    fileMap: Map<Char, Int>
): Pair<Pair<Int, Int>, Pair<Int, Int>>? {
    var specificFile: Int? = null
    var specificRank: Int? = null

    // Prüfen, ob eine spezifische Spalte oder Reihe im Zug angegeben ist
    if (pieceType.length == 2 && pieceType[1] in 'a'..'h') {
        // Wenn das zweite Zeichen ein Buchstabe ist, ist es die Spalte
        specificFile = fileMap[pieceType[1]]
    } else if (pieceType.length == 2 && pieceType[1].isDigit()) {
        // Wenn das zweite Zeichen eine Zahl ist, ist es die Reihe
        specificRank = 8 - pieceType[1].digitToInt()
    }


    // Suche nach der spezifischen Figur, die den Zug machen kann
    for (i in 0..7) {
        for (j in 0..7) {
            val currentPiece = board[i][j]
            if (isPieceOfType(pieceType[0].toString(), currentPiece, isWhite)) {
                // Überprüfe ob die Figur in der spezifizierten Spalte oder Reihe ist
                if ((specificFile == null || j == specificFile) && (specificRank == null || i == specificRank)) {
                    // Prüfen, ob diese Figur zum Ziel bewegen kann
                    if (isValidMove(board, i, j, endRank, endFile, currentPiece)) {
                        return Pair(Pair(i, j), Pair(endRank, endFile))
                    }
                }
            }
        }
    }
    return null
}


fun isPieceOfType(pieceType: String, currentPiece: Int, isWhite: Boolean): Boolean {
    // Define piece types
    return when (pieceType) {
        "N" -> if (isWhite) {
            currentPiece == R.drawable.white_knight_l || currentPiece == R.drawable.white_knight_r
        } else {
            currentPiece == R.drawable.black_knight_l || currentPiece == R.drawable.black_knight_r
        }


        "B" -> if (isWhite) {
            currentPiece == R.drawable.white_bishop_l || currentPiece == R.drawable.white_bishop_r
        } else {
            currentPiece == R.drawable.black_bishop_l || currentPiece == R.drawable.black_bishop_r
        }


        "R" -> if (isWhite) {
            currentPiece == R.drawable.white_rook_l || currentPiece == R.drawable.white_rook_r
        } else {
            currentPiece == R.drawable.black_rook_l || currentPiece == R.drawable.black_rook_r
        }

        "Q" -> currentPiece == if (isWhite) R.drawable.white_queen else R.drawable.black_queen
        "K" -> currentPiece == if (isWhite) R.drawable.white_king else R.drawable.black_king
        else -> false // Unsupported piece
    }
}


fun convertPgnPawnMoveToCoords(
    move: String,
    isWhite: Boolean,
    board: Array<Array<Int>>
): Pair<Pair<Int, Int>, Pair<Int, Int>>? {
    val fileMap =
        mapOf('a' to 0, 'b' to 1, 'c' to 2, 'd' to 3, 'e' to 4, 'f' to 5, 'g' to 6, 'h' to 7)

    // Bauernzüge haben nur zwei Zeichen, wie 'e4' oder 'c5'
    if (move.length == 2) {
        try {
            val pawnId = if (isWhite) R.drawable.white_pawn else R.drawable.black_pawn

            // Das Zielfeld
            val file = move[0]
            val endFile = fileMap[file] ?: return null // Der Buchstabe des Zielfelds (Spalte)
            val endRank = 8 - move[1].digitToInt()         // Die Zahl des Zielfelds (Zeile)
            var startRank: Int = if (isWhite) 6 else 1

            for (row in board.indices) {
                if (board[row][endFile] == pawnId) {
                    startRank = row // Finde die Startreihe für den Bauern
                    break
                }
            }
            // Der Startpunkt hängt von der Farbe des Spielers ab

            return Pair(Pair(startRank, endFile), Pair(endRank, endFile))
        } catch (e: Exception) {
            return null
        }
    } else { // move.length == 4
        try {
            val file_from = move[0] // Der Startspaltenbuchstabe
            val file_to = move[2]   // Der Zielspaltenbuchstabe
            val startFile = fileMap[file_from] ?: return null // Der Startspaltenindex
            val endFile = fileMap[file_to] ?: return null // Der Zielspaltenindex
            val endRank = 8 - move[3].digitToInt() // Die Zielzeile (1-8 wird zu 7-0)

            // Finde den Bauern anhand seiner ID (weißer oder schwarzer Bauer)
            val pawnId = if (isWhite) R.drawable.white_pawn else R.drawable.black_pawn

            // Iteriere über das Board, um die Position des Bauern zu finden
            var startRank: Int? = null
            for (row in board.indices) {
                if (board[row][startFile] == pawnId) {
                    startRank = row // Finde die Startreihe für den Bauern
                    break
                }
            }

            // Wenn kein Bauer auf der Startposition gefunden wurde, gib null zurück
            if (startRank == null) return null

            return Pair(Pair(startRank, startFile), Pair(endRank, endFile))
        } catch (e: Exception) {
            return null
        }
    }
}