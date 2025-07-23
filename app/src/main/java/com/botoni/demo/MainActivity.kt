package com.botoni.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.botoni.demo.ui.viewmodels.AuthenticationViewModel
import com.botoni.demo.ui.views.gateway.Login

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Login(context = this)
        }
    }
}
