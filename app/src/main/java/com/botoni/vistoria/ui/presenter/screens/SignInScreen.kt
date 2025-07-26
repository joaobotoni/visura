package com.botoni.vistoria.ui.presenter.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
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
import com.botoni.vistoria.ui.viewmodels.SignInViewModel

@Composable
fun SignInScreen(modifier: Modifier = Modifier, onSignInSuccess: () -> Unit) {

    val context = LocalContext.current
    val signInViewModel: SignInViewModel = hiltViewModel()
    val uiState by signInViewModel.uiState.collectAsStateWithLifecycle()

    DemoTheme {
        Surface(modifier = modifier.fillMaxSize()) {
            Form(
                email = uiState.email,
                onEmailChange = signInViewModel::updateEmail,
                password = uiState.password,
                onPasswordChange = signInViewModel::updatePassword,
                passwordVisibility = uiState.passwordVisibility,
                onTogglePasswordVisibility = signInViewModel::togglePasswordVisibility,
                onSignInWithEmailAndPassword = signInViewModel::signIn,
                onGoogleSignInClicked = signInViewModel::signInWithGoogle
            )
        }
    }

    uiState.Success?.let { successMessage ->
        Toast.makeText(context, successMessage, Toast.LENGTH_SHORT).show()
        signInViewModel.clearSuccess()
    }

    uiState.Error?.let { errorMessage ->
        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        signInViewModel.clearError()
    }

    LaunchedEffect(uiState.isLogged) {
        if (uiState.isLogged) {
            onSignInSuccess()
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
    val icon = when (passwordVisibility) {
        true -> Icons.Default.Visibility
        false -> Icons.Default.VisibilityOff
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Welcome Back!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "Sign in to continue to your account",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StandardTextField(
                            value = email,
                            placeholder = "Email",
                            label = "Enter your email",
                            onValueChange = onEmailChange,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Email icon",
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            visualTransformation = VisualTransformation.None
                        )
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StandardTextField(
                            value = password,
                            placeholder = "Password",
                            label = "Enter your password",
                            onValueChange = onPasswordChange,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                IconButton(onClick = onTogglePasswordVisibility) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = if (passwordVisibility) "Hide password" else "Show password",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation()
                        )
                    }

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
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text = "OR",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

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
}


@Preview(showBackground = true)
@Composable
fun SignInScreenPreview() {
    DemoTheme {
        Form(
            email = "",
            onEmailChange = {},
            password = "",
            onPasswordChange = {},
            passwordVisibility = false,
            onTogglePasswordVisibility = {},
            onSignInWithEmailAndPassword = {},
            onGoogleSignInClicked = {}
        )
    }
}