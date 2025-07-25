package com.botoni.vistoria.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.botoni.vistoria.ui.presenter.screens.signIn.SignInScreen

@Composable
fun Navigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = "signIn") {
        composable("signIn") {
            SignInScreen()
        }
    }
}