package com.botoni.vistoria.ui.presenter.screens.gateway

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.botoni.vistoria.R
import com.botoni.vistoria.ui.presenter.elements.button.StandardButton
import com.botoni.vistoria.ui.presenter.elements.button.StandardOutlinedButton
import com.botoni.vistoria.ui.presenter.elements.button.StandardTextButton
import com.botoni.vistoria.ui.presenter.elements.textField.StandardTextField
import com.botoni.vistoria.ui.presenter.theme.DemoTheme
import com.botoni.vistoria.ui.viewmodels.GatewayViewModel

@Composable
fun GatewayScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val gatewayViewModel: GatewayViewModel = hiltViewModel()

    val uiState by gatewayViewModel.uiState.collectAsStateWithLifecycle()

    DemoTheme {
        Surface(modifier = modifier.fillMaxSize()) {
            Form(
                email = uiState.email,
                onEmailChange = gatewayViewModel::updateEmail,
                password = uiState.password,
                onPasswordChange = gatewayViewModel::updatePassword,
                passwordVisibility = uiState.passwordVisibility,
                onTogglePasswordVisibility = gatewayViewModel::togglePasswordVisibility,
                onSignInWithEmailAndPassword = gatewayViewModel::signInWithEmailAndPassword,
                onGoogleSignInClicked = { gatewayViewModel.signInWithGoogle(context) }
            )
        }
    }
}

@Composable
fun Form(
    modifier: Modifier = Modifier,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisibility: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    onSignInWithEmailAndPassword: () -> Unit,
    onGoogleSignInClicked: () -> Unit
) {
    val icon: Painter = when (passwordVisibility) {
        true -> painterResource(id = R.drawable.visible)
        false -> painterResource(id = R.drawable.not_visible)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(text = "Welcome", fontSize = 24.sp)

        StandardTextField(
            value = email,
            placeholder = "Email",
            label = "Enter your email",
            onValueChange = onEmailChange,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            visualTransformation = VisualTransformation.None
        )

        StandardTextField(
            value = password,
            placeholder = "Password",
            label = "Enter your password",
            onValueChange = onPasswordChange,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = onTogglePasswordVisibility) {
                    Icon(
                        painter = icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            StandardTextButton(
                text = "Forgot password?",
                onClick = {}
            )
        }

        StandardButton(
            text = "Continue",
            onClick = onSignInWithEmailAndPassword,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

        StandardOutlinedButton(
            text = "Login with Google",
            onClick = onGoogleSignInClicked,
            icon = R.drawable.google_icon,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        )
    }
}