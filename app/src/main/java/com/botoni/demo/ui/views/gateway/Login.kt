package com.botoni.demo.ui.views.gateway

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.botoni.demo.R
import com.botoni.demo.ui.components.button.StandardButton
import com.botoni.demo.ui.components.button.StandardOutlinedButton
import com.botoni.demo.ui.components.button.StandardTextButton
import com.botoni.demo.ui.components.textField.StandardTextField
import com.botoni.demo.ui.theme.DemoTheme
import com.botoni.demo.ui.viewmodels.AuthenticationViewModel

@Composable
fun Login(modifier: Modifier = Modifier, context: Context) {
    val authenticationViewModel = AuthenticationViewModel(context)

    DemoTheme {
        Surface(modifier = modifier.fillMaxSize()) {
            LoginForm(viewModel = authenticationViewModel)
        }
    }
}

@Composable
fun LoginForm(modifier: Modifier = Modifier, viewModel: AuthenticationViewModel) {
    val context = LocalContext.current
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val passwordVisibility by viewModel.passwordVisibility.collectAsState()

    val icon = if (passwordVisibility)
        painterResource(id = R.drawable.visible)
    else
        painterResource(id = R.drawable.not_visible)

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
            onValueChange = { viewModel.onEmailChange(it) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            visualTransformation = VisualTransformation.None
        )

        StandardTextField(
            value = password,
            placeholder = "Password",
            label = "Enter your password",
            onValueChange = { viewModel.onPasswordChange(it) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = {
                    viewModel.togglePasswordVisibility()
                }) {
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
                onClick = {
                    eventOnClick(context, "Did you click Forgot Password?")
                }
            )
        }

        StandardButton(
            text = "Continue",
            onClick = {
                viewModel.loginWithEmailPassword()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

        StandardOutlinedButton(
            text = "Login with Google",
            onClick = {
                viewModel.loginWithGoogle()
            },
            icon = R.drawable.google_icon,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        )
    }
}

fun eventOnClick(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}
