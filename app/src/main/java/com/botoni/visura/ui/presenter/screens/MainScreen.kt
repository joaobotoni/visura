package com.botoni.visura.ui.presenter.screens

import Register
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Menu(modifier = modifier)
    }
}

@Serializable
@SerialName("Home")
object Home

@Serializable
@SerialName("Register")
object Register

enum class Destination(
    val route: Any,
    val label: String,
    val icon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME(
        route = Home,
        label = "Home",
        icon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    REGISTER(
        route = Register,
        label = "Register",
        icon = Icons.Filled.AddCircle,
        unselectedIcon = Icons.Outlined.AddCircleOutline
    )
}

@RequiresPermission(allOf = [android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION])
@Composable
fun Menu(modifier: Modifier = Modifier) {

    val navController = rememberNavController()
    val startDestination = Destination.HOME
    var selected by remember { mutableIntStateOf(startDestination.ordinal) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val current = navBackStackEntry?.destination?.route

    LaunchedEffect(current) {
        Destination.entries.forEachIndexed { index, destination ->
            if (current == destination.label) {
                selected = index
            }
        }
    }

    val graph = navController.createGraph(startDestination = startDestination.route) {
        composable<Home> { Home() }
        composable<Register>  { Register() }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            // TODO()
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.fillMaxWidth(),
                windowInsets = NavigationBarDefaults.windowInsets
            ) {
                Destination.entries.forEachIndexed { index, destination ->
                    NavigationBarItem(
                        selected = selected == index,
                        onClick = {
                            if (selected != index) {
                                navController.navigate(destination.route)
                                selected = index
                            }
                        },
                        icon = {
                            Icon(
                                modifier = Modifier.size(28.dp),
                                imageVector = if (selected == index) destination.icon else destination.unselectedIcon,
                                contentDescription = null
                            )
                        },
                        label = {
                            Text(if (destination.route == Home) "Home" else "Nova Vistoria");
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController,
            graph,
            Modifier
                .padding(paddingValues)
                .fillMaxSize()
        )
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
fun MainScreenPreview() {
    MainScreen()
}