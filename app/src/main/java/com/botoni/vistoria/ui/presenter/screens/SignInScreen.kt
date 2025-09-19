package com.botoni.vistoria.ui.presenter.screens

import android.annotation.SuppressLint
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.botoni.vistoria.ui.presenter.elements.input.StandardTextField
import com.botoni.vistoria.ui.presenter.elements.snackbar.StandardSnackbar
import com.botoni.vistoria.ui.presenter.theme.DemoTheme
import com.botoni.vistoria.ui.viewmodels.SignInEvent
import com.botoni.vistoria.ui.viewmodels.SignInState
import com.botoni.vistoria.ui.viewmodels.SignInViewModel

@Composable
fun SignInScreen(
    onLoginSuccess: () -> Unit,
    viewModel: SignInViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarState = remember { SnackbarHostState() }
    var isSuccessMessage by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SignInEvent.ShowMessage -> {
                    isSuccessMessage = event.isSuccess
                    snackbarState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) {
            onLoginSuccess()
        }
    }

    LoginScreenContent(
        state = state,
        snackbarState = snackbarState,
        isSuccessMessage = isSuccessMessage,
        onEmailChange = viewModel::setEmail,
        onPasswordChange = viewModel::setPassword,
        onTogglePassword = viewModel::togglePasswordVisibility,
        onLogin = viewModel::loginWithEmail,
        onGoogleLogin = viewModel::loginWithGoogle
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
private fun LoginScreenContent(
    state: SignInState,
    snackbarState: SnackbarHostState,
    isSuccessMessage: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePassword: () -> Unit,
    onLogin: () -> Unit,
    onGoogleLogin: () -> Unit
) {
    DemoTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                snackbarHost = {
                    StandardSnackbar(
                        snackBarHostState = snackbarState,
                        snackBarState = isSuccessMessage
                    )
                }
            ) {
                LoginForm(
                    state = state,
                    onEmailChange = onEmailChange,
                    onPasswordChange = onPasswordChange,
                    onTogglePassword = onTogglePassword,
                    onLogin = onLogin,
                    onGoogleLogin = onGoogleLogin
                )
            }
        }
    }
}

@Composable
private fun LoginForm(
    state: SignInState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePassword: () -> Unit,
    onLogin: () -> Unit,
    onGoogleLogin: () -> Unit
) {
    Box(
        modifier = Modifier
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
            WelcomeHeader()
            Spacer(modifier = Modifier.height(32.dp))

            LoginInputs(
                email = state.email,
                password = state.password,
                showPassword = state.showPassword,
                isLoading = state.isLoading,
                onEmailChange = onEmailChange,
                onPasswordChange = onPasswordChange,
                onTogglePassword = onTogglePassword,
                onLogin = onLogin
            )

            Spacer(modifier = Modifier.height(24.dp))
            LoginDivider()
            Spacer(modifier = Modifier.height(24.dp))

            GoogleLoginButton(
                isLoading = state.isLoading,
                onGoogleLogin = onGoogleLogin
            )
        }
    }
}

@Composable
private fun WelcomeHeader() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Bem-vindo de volta!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Faça login para continuar acessando sua conta",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LoginInputs(
    email: String,
    password: String,
    showPassword: Boolean,
    isLoading: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePassword: () -> Unit,
    onLogin: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EmailInput(
            email = email,
            onEmailChange = onEmailChange
        )

        PasswordInput(
            password = password,
            showPassword = showPassword,
            onPasswordChange = onPasswordChange,
            onTogglePassword = onTogglePassword
        )

        SignUpLink()

        LoginButton(
            isLoading = isLoading,
            onLogin = onLogin
        )
    }
}

@Composable
private fun EmailInput(
    email: String,
    onEmailChange: (String) -> Unit
) {
    StandardTextField(
        value = email,
        placeholder = "Email",
        label = "Digite seu email",
        onValueChange = onEmailChange,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        },
        visualTransformation = VisualTransformation.None,
    )
}

@Composable
private fun PasswordInput(
    password: String,
    showPassword: Boolean,
    onPasswordChange: (String) -> Unit,
    onTogglePassword: () -> Unit
) {
    val icon = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff
    val description = if (showPassword) "Ocultar senha" else "Mostrar senha"

    StandardTextField(
        value = password,
        placeholder = "Senha",
        label = "Digite sua senha",
        onValueChange = onPasswordChange,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(onClick = onTogglePassword) {
                Icon(
                    imageVector = icon,
                    contentDescription = description,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        visualTransformation = if (showPassword) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        }
    )
}

@Composable
private fun SignUpLink() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        StandardTextButton(
            text = "Ainda não possui uma conta?",
            onClick = { /* TODO: Navegar para cadastro */ }
        )
    }
}

@Composable
private fun LoginButton(
    isLoading: Boolean,
    onLogin: () -> Unit
) {
    StandardButton(
        text = "Continuar",
        onClick = onLogin,
        enabled = !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    )
}

@Composable
private fun LoginDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f))

        Text(
            text = "OU",
            modifier = Modifier.padding(horizontal = 16.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )

        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun GoogleLoginButton(
    isLoading: Boolean,
    onGoogleLogin: () -> Unit
) {
    StandardOutlinedButton(
        text = "Entrar com Google",
        onClick = onGoogleLogin,
        enabled = !isLoading,
        icon = R.drawable.google_icon,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    )
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    DemoTheme {
        LoginForm(
            state = SignInState(),
            onEmailChange = {},
            onPasswordChange = {},
            onTogglePassword = {},
            onLogin = {},
            onGoogleLogin = {}
        )
    }
}