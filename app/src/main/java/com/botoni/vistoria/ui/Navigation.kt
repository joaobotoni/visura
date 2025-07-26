package com.botoni.vistoria.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.botoni.vistoria.ui.presenter.screens.MainScreen
import com.botoni.vistoria.ui.presenter.screens.SignInScreen

@Composable
fun Navigation(
    navController: NavHostController = rememberNavController()
) {

    NavHost(navController = navController, startDestination = "signIn") {
        composable("signIn") {
            SignInScreen(navController = navController)
        }
        composable("main") {
            MainScreen()
        }
    }
}