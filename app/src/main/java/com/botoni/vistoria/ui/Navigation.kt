package com.botoni.vistoria.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.botoni.vistoria.ui.presenter.screens.MainScreen
import com.botoni.vistoria.ui.presenter.screens.SignInScreen
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

    NavHost(navController = navController, startDestination = "signIn") {
        composable("signIn") {
            SignInScreen()
        }
        composable("main/{email}") {
            MainScreen("João")
        }
    }
}