package com.botoni.visura.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

sealed class Screen(val route: String) {
    data object SignUp : Screen("signUp")
    data object Main : Screen("main")
}

@Composable
fun Navigation(navController: NavHostController = rememberNavController()) {

}