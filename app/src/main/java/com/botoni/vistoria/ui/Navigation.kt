package com.botoni.vistoria.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.botoni.vistoria.ui.presenter.screens.main.MainScreen
import com.botoni.vistoria.ui.presenter.screens.signIn.SignInScreen
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun Navigation(
    navController: NavHostController = rememberNavController()
) {
    val user = Firebase.auth.currentUser
    user?.let {
        val email = it.email
    }

    NavHost(navController = navController, startDestination = "main/{email}") {
        composable("signIn") {
            SignInScreen()
        }
        composable("main/{email}") {
            MainScreen("João")
        }
    }
}