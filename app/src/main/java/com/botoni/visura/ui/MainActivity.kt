package com.botoni.visura.ui

import android.os.Bundle
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import com.botoni.visura.ui.presenter.theme.DemoTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val auth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DemoTheme {
                Navigation()
            }
        }
    }
}