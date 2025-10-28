package com.visura.ui

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import com.visura.ui.presenter.screens.MainScreen
import com.visura.ui.presenter.screens.SignInScreen
import com.visura.ui.presenter.screens.SignUpScreen
import com.visura.ui.viewmodels.AuthenticationEvent
import com.visura.ui.viewmodels.AuthenticationState
import com.visura.ui.viewmodels.AuthenticationViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.serialization.Serializable


@Serializable
object Main

@Serializable
object SignIn

@Serializable
object SignUp
@RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
@Composable
fun Navigation(
    navController: NavHostController = rememberNavController(),
    viewModel: AuthenticationViewModel = hiltViewModel()
) {

    val authState by viewModel.state.collectAsStateWithLifecycle()

    val startDestination = remember {
        when (authState){
            is AuthenticationState.Authenticated -> Main
            AuthenticationState.Unauthenticated -> SignIn
        }
    }

    LaunchedEffect(Unit) {
        viewModel.event.collectLatest { event ->
            when (event) {
                is AuthenticationEvent.NavigateToMain -> {
                    delay(800)
                    navController.navigate(Main) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }

                is AuthenticationEvent.NavigateToSignIn -> {
                    delay(500)
                    navController.navigate(SignIn) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
        }
    }

    val navGraph = navController.createGraph(startDestination = startDestination) {

        composable<Main> {
            MainScreen()
        }

        composable<SignIn> {
            SignInScreen(
                navSignUp = {
                    navController.navigate(SignUp) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable<SignUp> {
            SignUpScreen(
                navSignIn = {
                    navController.popBackStack()
                }
            )
        }
    }

    NavHost(
        navController = navController,
        graph = navGraph,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(500)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(500)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(500)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(500)
            )
        }
    )
}