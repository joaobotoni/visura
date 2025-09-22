package com.botoni.visura.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.botoni.visura.ui.presenter.screens.MainScreen
import com.botoni.visura.ui.presenter.screens.SignInScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun Navigation(
    navController: NavHostController = rememberNavController()
) {
    val auth = FirebaseAuth.getInstance()
    val startDestination = if (auth.currentUser != null) "main" else "signIn"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("signIn") {
            SignInScreen(
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo("signIn") { inclusive = true }
                    }
                },
                onSignUpClick = {}
            )
        }
        composable("main") {
            MainScreen()
        }
    }
}