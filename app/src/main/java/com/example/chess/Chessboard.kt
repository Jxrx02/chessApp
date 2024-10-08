import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.chess.R

// Beispiel-Schachbrett mit Bild-Ressourcen
val initialBoardWithImages = arrayOf(
    arrayOf(R.drawable.black_rook, R.drawable.black_knight, R.drawable.black_bishop, R.drawable.black_queen, R.drawable.black_king, R.drawable.black_bishop, R.drawable.black_knight, R.drawable.black_rook),
    arrayOf(R.drawable.black_pawn, R.drawable.black_pawn, R.drawable.black_pawn, R.drawable.black_pawn, R.drawable.black_pawn, R.drawable.black_pawn, R.drawable.black_pawn, R.drawable.black_pawn),
    arrayOf(0, 0, 0, 0, 0, 0, 0, 0),
    arrayOf(0, 0, 0, 0, 0, 0, 0, 0),
    arrayOf(0, 0, 0, 0, 0, 0, 0, 0),
    arrayOf(0, 0, 0, 0, 0, 0, 0, 0),
    arrayOf(R.drawable.white_pawn, R.drawable.white_pawn, R.drawable.white_pawn, R.drawable.white_pawn, R.drawable.white_pawn, R.drawable.white_pawn, R.drawable.white_pawn, R.drawable.white_pawn),
    arrayOf(R.drawable.white_rook, R.drawable.white_knight, R.drawable.white_bishop, R.drawable.white_queen, R.drawable.white_king, R.drawable.white_bishop, R.drawable.white_knight, R.drawable.white_rook)
)

@Composable
fun ChessBoard() {
    var boardState by remember { mutableStateOf(initialBoardWithImages) }
    var selectedPiece by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var possibleMoves by remember { mutableStateOf<List<Pair<Int, Int>>>(emptyList()) }
    var fenNotation by remember { mutableStateOf(calculateFEN(boardState)) } // FEN wird basierend auf dem aktuellen Zustand berechnet

    Column {
        // FEN-Notation wird im Textfeld angezeigt
        Text(
            text = "FEN: $fenNotation",
            modifier = Modifier.padding(16.dp)
        )

        // Schachbrett mit Feldern
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
                            if (selectedPiece == null && boardState[clickedRow][clickedCol] != 0) {
                                // Wenn ein Feld mit einer Figur angeklickt wird, wird sie ausgewählt
                                selectedPiece = Pair(clickedRow, clickedCol)
                                possibleMoves = getPossibleMoves(boardState, clickedRow, clickedCol) // Berechne mögliche Züge
                            } else if (selectedPiece != null) {
                                val (selectedRow, selectedCol) = selectedPiece!!
                                val piece = boardState[selectedRow][selectedCol]

                                if (isValidMove(boardState, selectedRow, selectedCol, clickedRow, clickedCol, piece)) {
                                    // Aktualisiere das Schachbrett und FEN nach dem Zug
                                    boardState = boardState.movePiece(selectedRow, selectedCol, clickedRow, clickedCol)
                                    fenNotation = calculateFEN(boardState) // FEN-Notation nach dem Zug berechnen
                                }
                                selectedPiece = null // Auswahl zurücksetzen
                                possibleMoves = emptyList() // Mögliche Züge zurücksetzen
                            }
                        }
                    )
                }
            }
        }
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
fun Array<Array<Int>>.movePiece(fromRow: Int, fromCol: Int, toRow: Int, toCol: Int): Array<Array<Int>> {
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
        R.drawable.white_pawn, R.drawable.black_pawn -> isValidPawnMove(board, fromRow, fromCol, toRow, toCol, isWhite)
        R.drawable.white_rook, R.drawable.black_rook -> isValidRookMove(board, fromRow, fromCol, toRow, toCol)
        R.drawable.white_knight, R.drawable.black_knight -> isValidKnightMove(fromRow, fromCol, toRow, toCol)
        R.drawable.white_bishop, R.drawable.black_bishop -> isValidBishopMove(board, fromRow, fromCol, toRow, toCol)
        R.drawable.white_queen, R.drawable.black_queen -> isValidQueenMove(board, fromRow, fromCol, toRow, toCol)
        R.drawable.white_king, R.drawable.black_king -> isValidKingMove(fromRow, fromCol, toRow, toCol)
        else -> false
    }
}


fun isValidPawnMove(
    board: Array<Array<Int>>,
    fromRow: Int, fromCol: Int,
    toRow: Int, toCol: Int,
    isWhite: Boolean
): Boolean {
    val direction = if (isWhite) -1 else 1 // Weiße Bauern bewegen sich nach oben (-1), schwarze nach unten (+1)
    val startRow = if (isWhite) 6 else 1 // Weiße Bauern starten in der Reihe 6, schwarze in der Reihe 1

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
    if (Math.abs(fromCol - toCol) == 1 && toRow == fromRow + direction && board[toRow][toCol] != 0) {
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
    val rowDiff = Math.abs(fromRow - toRow)
    val colDiff = Math.abs(fromCol - toCol)
    return (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2)
}

// Beispiel: Überprüfe Läufer-Zug
fun isValidBishopMove(
    board: Array<Array<Int>>,
    fromRow: Int, fromCol: Int,
    toRow: Int, toCol: Int
): Boolean {
    if (Math.abs(fromRow - toRow) != Math.abs(fromCol - toCol)) return false

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
    return isValidRookMove(board, fromRow, fromCol, toRow, toCol) || isValidBishopMove(board, fromRow, fromCol, toRow, toCol)
}

// Beispiel: Überprüfe König-Zug
fun isValidKingMove(fromRow: Int, fromCol: Int, toRow: Int, toCol: Int): Boolean {
    val rowDiff = Math.abs(fromRow - toRow)
    val colDiff = Math.abs(fromCol - toCol)
    return rowDiff <= 1 && colDiff <= 1
}


fun calculateFEN(board: Array<Array<Int>>): String {
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

    fen += " w - - 0 1" // Standardmäßig "weiß am Zug", keine Rochaderechte, keine En passant-Möglichkeit, kein Zugzähler

    return fen
}
