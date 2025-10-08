package com.botoni.visura.ui.presenter.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import com.botoni.visura.R
import com.botoni.visura.ui.presenter.elements.button.StandardButton
import com.botoni.visura.ui.presenter.elements.button.StandardOutlinedButton
import com.botoni.visura.ui.presenter.elements.button.StandardTextButton
import com.botoni.visura.ui.presenter.elements.field.StandardTextField
import com.botoni.visura.ui.presenter.elements.snackbar.SnackbarType
import com.botoni.visura.ui.presenter.elements.snackbar.StandardSnackbar
import com.botoni.visura.ui.presenter.theme.DemoTheme
import com.botoni.visura.ui.viewmodels.FieldState
import com.botoni.visura.ui.viewmodels.SignInEvent
import com.botoni.visura.ui.viewmodels.SignInState
import com.botoni.visura.ui.viewmodels.SignInViewModel
import com.botoni.visura.ui.viewmodels.UiState
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun SignInScreen(
    onLoginSuccess: () -> Unit,
    onSignUpClick: () -> Unit,
    viewModel: SignInViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackBarHostState = remember { SnackbarHostState() }
    var snackBarType by remember { mutableStateOf(SnackbarType.DEFAULT) }

    HandleSignInEvents(
        events = viewModel.events,
        snackBarHostState = snackBarHostState,
        onNavigate = onLoginSuccess,
        onSnackBarTypeChange = { snackBarType = it }
    )

    SignInScreenContent(
        state = state,
        snackBarHostState = snackBarHostState,
        snackBarType = snackBarType,
        onEmailChange = viewModel::setEmail,
        onPasswordChange = viewModel::setPassword,
        onTogglePassword = viewModel::togglePasswordVisibility,
        onLogin = viewModel::loginWithEmail,
        onGoogleLogin = viewModel::loginWithGoogle,
        onSignUpClick = onSignUpClick
    )
}

@Composable
private fun HandleSignInEvents(
    events: SharedFlow<SignInEvent>,
    snackBarHostState: SnackbarHostState,
    onNavigate: () -> Unit,
    onSnackBarTypeChange: (SnackbarType) -> Unit
) {
    LaunchedEffect(Unit) {
        events.collect { event ->
            when (event) {
                is SignInEvent.ShowMessage -> {
                    val type = if (event.isSuccess) SnackbarType.SUCCESS else SnackbarType.ERROR
                    onSnackBarTypeChange(type)
                    snackBarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Long
                    )
                    if (event.isSuccess) {
                        onNavigate()
                    }
                }
            }
        }
    }
}


@Composable
private fun SignInScreenContent(
    state: SignInState,
    snackBarHostState: SnackbarHostState,
    snackBarType: SnackbarType,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePassword: () -> Unit,
    onLogin: () -> Unit,
    onGoogleLogin: () -> Unit,
    onSignUpClick: () -> Unit
) {
    DemoTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                snackbarHost = {
                    StandardSnackbar(
                        hostState = snackBarHostState,
                        type = snackBarType
                    )
                }
            ) { paddingValues ->
                SignInForm(
                    state = state,
                    onEmailChange = onEmailChange,
                    onPasswordChange = onPasswordChange,
                    onTogglePassword = onTogglePassword,
                    onLogin = onLogin,
                    onGoogleLogin = onGoogleLogin,
                    onSignUpClick = onSignUpClick,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun SignInForm(
    state: SignInState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePassword: () -> Unit,
    onLogin: () -> Unit,
    onGoogleLogin: () -> Unit,
    onSignUpClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLoading = state.isEmailLoading || state.isGoogleLoading
    val isSuccess = state.uiState is UiState.Success
    val isEnabled = !isLoading && !isSuccess

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        SignInHeader()

        Spacer(modifier = Modifier.height(32.dp))

        SignInInputFields(
            emailState = state.email,
            passwordState = state.password,
            showPassword = state.showPassword,
            isEnabled = isEnabled,
            onEmailChange = onEmailChange,
            onPasswordChange = onPasswordChange,
            onTogglePassword = onTogglePassword
        )

        Spacer(modifier = Modifier.height(16.dp))

        SignUpLink(
            onClick = onSignUpClick,
            enabled = isEnabled
        )

        Spacer(modifier = Modifier.height(20.dp))

        LoginButton(
            isLoading = state.isEmailLoading,
            isEnabled = isEnabled && state.email.value.isNotEmpty() && state.password.value.isNotEmpty(),
            onClick = onLogin
        )

        Spacer(modifier = Modifier.height(24.dp))

        DividerWithText()

        Spacer(modifier = Modifier.height(24.dp))

        GoogleButton(
            isLoading = state.isGoogleLoading,
            isEnabled = isEnabled,
            onClick = onGoogleLogin
        )
    }
}

@Composable
private fun SignInHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Bem-vindo de volta!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Faça login para continuar acessando sua conta",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}

@Composable
private fun SignInInputFields(
    emailState: FieldState,
    passwordState: FieldState,
    showPassword: Boolean,
    isEnabled: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePassword: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        EmailField(
            state = emailState,
            isEnabled = isEnabled,
            onValueChange = onEmailChange
        )

        PasswordField(
            state = passwordState,
            showPassword = showPassword,
            isEnabled = isEnabled,
            onValueChange = onPasswordChange,
            onTogglePassword = onTogglePassword
        )
    }
}

@Composable
private fun EmailField(
    state: FieldState,
    isEnabled: Boolean,
    onValueChange: (String) -> Unit
) {
    StandardTextField(
        value = state.value,
        placeholder = "Email",
        label = "Digite seu email",
        onValueChange = onValueChange,
        enabled = isEnabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = "Ícone de email",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        visualTransformation = VisualTransformation.None,
        isError = !state.isValid,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun PasswordField(
    state: FieldState,
    showPassword: Boolean,
    isEnabled: Boolean,
    onValueChange: (String) -> Unit,
    onTogglePassword: () -> Unit
) {
    StandardTextField(
        value = state.value,
        placeholder = "Senha",
        label = "Digite sua senha",
        onValueChange = onValueChange,
        enabled = isEnabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(
                onClick = onTogglePassword,
                enabled = isEnabled
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = if (showPassword) "Ocultar senha" else "Mostrar senha",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
        isError = !state.isValid,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun SignUpLink(
    onClick: () -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        StandardTextButton(
            text = "Ainda não possui uma conta?",
            onClick = onClick,
            enabled = enabled
        )
    }
}

@Composable
private fun LoginButton(
    isLoading: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    StandardButton(
        text = if (isLoading) "Entrando..." else "Continuar",
        onClick = onClick,
        enabled = isEnabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    )
}

@Composable
private fun DividerWithText() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )

        Text(
            text = "OU",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun GoogleButton(
    isLoading: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    StandardOutlinedButton(
        text = if (isLoading) "Conectando..." else "Entrar com Google",
        onClick = onClick,
        enabled = isEnabled,
        icon = R.drawable.google_icon,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SignInScreenPreview() {
    DemoTheme {
        SignInForm(
            state = SignInState(),
            onEmailChange = {},
            onPasswordChange = {},
            onTogglePassword = {},
            onLogin = {},
            onGoogleLogin = {},
            onSignUpClick = {}
        )
    }
}