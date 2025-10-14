package com.botoni.visura.ui.presenter.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.botoni.visura.ui.presenter.elements.button.StandardOutlinedButton
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val auth: FirebaseAuth = Firebase.auth
    val user = auth.currentUser

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
        ) {
            StandardOutlinedButton(
                text = "Sair",
                onClick = {},
                enabled = true,
                modifier = Modifier.align(Alignment.TopStart)
            )
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = "Ola: ${user?.email}"
            )
        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
fun MainScreenPreview() {
    MainScreen()
}