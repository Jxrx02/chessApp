package com.example.chess

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chess.database.PuzzleDatabase
import com.example.chess.ui.theme.ChessTheme
import com.example.chess.view.main.MainView
import com.example.chess.view.main.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChessTheme {

                //ChessBoard()
                ChessApp()

            }

        }
    }
}

@Composable
fun ChessApp() {
    val viewModel = viewModel<MainViewModel>()
    viewModel.initialize(PuzzleDatabase.getInstance(LocalContext.current))

    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "mainView") {
        composable(route = "mainView") {
            MainView(viewModel = viewModel, navController = navController)
        }
        composable(route = "chessBoardView") {
            ChessBoardView(viewModel = viewModel, navController = navController)
        }
    }
}