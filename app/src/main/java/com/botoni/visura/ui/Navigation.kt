package com.botoni.visura.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import com.botoni.visura.ui.presenter.screens.MainScreen
import com.botoni.visura.ui.presenter.screens.SignInScreen
import com.botoni.visura.ui.presenter.screens.SignUpScreen
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.serialization.Serializable

@Serializable
object Main

@Serializable
object SignIn

@Serializable
object SignUp

@Composable
fun Navigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    var user by remember { mutableStateOf(Firebase.auth.currentUser) }
    LaunchedEffect(Unit) {
        Firebase.auth.addAuthStateListener { auth ->
            user = auth.currentUser
        }
    }

    val startDestination = if (user != null) Main else SignIn

    val navGraph = navController.createGraph(startDestination = startDestination) {
        composable<Main> {
            if (Firebase.auth.currentUser != null) {
                MainScreen(exit = {})
            } else {
                navController.navigate(SignIn) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }

        composable<SignIn> {
            SignInScreen(
                navSignUp = {
                    navController.navigate(SignUp) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                },
                navMain = {
                    navController.navigate(Main) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable<SignUp> {
            SignUpScreen(
                navSignIn = {
                    navController.navigate(SignIn) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                },
                navMain = {
                    navController.navigate(Main) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

    }

    NavHost(navController, graph = navGraph)
}