package com.botoni.visura.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.botoni.visura.ui.presenter.screens.MainScreen
import com.botoni.visura.ui.presenter.screens.SignInScreen
import com.botoni.visura.ui.presenter.screens.SignUpScreen
import kotlinx.serialization.Serializable


@Serializable
object Main

@Serializable
object SignIn

@Serializable
object SignUp

@Composable
fun Navigation(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = SignUp
    ) {
        composable<Main> { MainScreen() }
        composable<SignIn> { SignInScreen() }
        composable<SignUp> { SignUpScreen() }
    }
}



