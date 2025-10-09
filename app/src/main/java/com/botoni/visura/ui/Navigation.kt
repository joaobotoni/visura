//package com.botoni.visura.ui.navigation
//
//import androidx.compose.animation.AnimatedContentTransitionScope
//import androidx.compose.animation.core.FiniteAnimationSpec
//import androidx.compose.animation.core.spring
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.DisposableEffect
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.unit.IntOffset
//import androidx.navigation.NavHostController
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import androidx.navigation.compose.rememberNavController
//import com.botoni.visura.ui.presenter.screens.MainScreen
//import com.botoni.visura.ui.presenter.screens.SignInScreen
//import com.google.firebase.auth.FirebaseAuth
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//
//sealed class Screen(val route: String) {
//    data object SignIn : Screen("signIn")
//    data object SignUp : Screen("signUp")
//    data object Main : Screen("main")
//}
//
//@Composable
//fun Navigation(navController: NavHostController = rememberNavController()) {
//    val auth = remember { FirebaseAuth.getInstance() }
//    var isAuthenticated by remember { mutableStateOf(false) }
//    var isLoading by remember { mutableStateOf(true) }
//
//    LaunchedEffect(Unit) {
//        withContext(Dispatchers.IO) {
//            val user = auth.currentUser
//            withContext(Dispatchers.Main) {
//                isAuthenticated = user != null
//                isLoading = false
//            }
//        }
//    }
//
//    DisposableEffect(Unit) {
//        val listener = FirebaseAuth.AuthStateListener {
//            val authenticated = it.currentUser != null
//            if (authenticated != isAuthenticated) {
//                isAuthenticated = authenticated
//                if (authenticated) {
//                    navController.navigate(Screen.Main.route) {
//                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
//                        launchSingleTop = true
//                    }
//                }
//            }
//        }
//        auth.addAuthStateListener(listener)
//        onDispose { auth.removeAuthStateListener(listener) }
//    }
//
//    val startScreen = if (isLoading || !isAuthenticated) Screen.SignIn.route else Screen.Main.route
//    val slideAnimation: FiniteAnimationSpec<IntOffset> = spring(stiffness = 300f)
//
//    NavHost(navController = navController, startDestination = startScreen) {
//        composable(
//            route = Screen.SignIn.route,
//            enterTransition = {
//                slideIntoContainer(
//                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
//                    animationSpec = slideAnimation
//                )
//            },
//            exitTransition = {
//                slideOutOfContainer(
//                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
//                    animationSpec = slideAnimation
//                )
//            }
//        ) {
//            if (!isAuthenticated) {
//                SignInScreen(
//                    onLoginSuccess = {
//                        navController.navigate(Screen.Main.route) {
//                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
//                            launchSingleTop = true
//                        }
//                    },
//                    onSignUpClick = { navController.navigate(Screen.SignUp.route) }
//                )
//            } else {
//                LaunchedEffect(Unit) {
//                    navController.navigate(Screen.Main.route) {
//                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
//                        launchSingleTop = true
//                    }
//                }
//            }
//        }
//
//        composable(
//            route = Screen.SignUp.route,
//            enterTransition = {
//                slideIntoContainer(
//                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
//                    animationSpec = slideAnimation
//                )
//            },
//            exitTransition = {
//                slideOutOfContainer(
//                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
//                    animationSpec = slideAnimation
//                )
//            }
//        ) {
////            if (!isAuthenticated) {
////                SignUpScreen(
////                    onSignUpSuccess = {
////                        navController.navigate(Screen.Main.route) {
////                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
////                            launchSingleTop = true
////                        }
////                    },
////                    onSignInClick = { navController.popBackStack() }
////                )
////            } else {
////                LaunchedEffect(Unit) {
////                    navController.navigate(Screen.Main.route) {
////                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
////                        launchSingleTop = true
////                    }
////                }
////            }
//        }
//
//        composable(route = Screen.Main.route) {
//            MainScreen()
//        }
//    }
//}