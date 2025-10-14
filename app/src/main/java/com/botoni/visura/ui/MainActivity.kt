package com.botoni.visura.ui

import android.annotation.SuppressLint
import android.os.Bundle
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHost
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import com.botoni.visura.ui.presenter.screens.MainScreen
import com.botoni.visura.ui.presenter.screens.SignInScreen
import com.botoni.visura.ui.presenter.screens.SignUpScreen
import dagger.hilt.android.AndroidEntryPoint
import com.botoni.visura.ui.presenter.theme.DemoTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.serialization.Serializable
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DemoTheme {
                Navigation()
            }
        }
    }
}