package com.example.chess

// import android.content.Context
import android.os.Bundle
// import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.chess.ui.theme.ChessTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChessTheme {
                ChessBoard()

            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

// fun Context.toast(message: CharSequence) =
//    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ChessTheme {
        Greeting("Android")
    }
}