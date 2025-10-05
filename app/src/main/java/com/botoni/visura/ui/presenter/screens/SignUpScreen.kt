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
import com.botoni.visura.ui.presenter.elements.input.StandardTextField
import com.botoni.visura.ui.presenter.elements.snackbar.SnackbarType
import com.botoni.visura.ui.presenter.elements.snackbar.StandardSnackbar
import com.botoni.visura.ui.presenter.theme.DemoTheme
import com.botoni.visura.ui.viewmodels.ErrorType
import com.botoni.visura.ui.viewmodels.FieldState
import com.botoni.visura.ui.viewmodels.SignUpEvent
import com.botoni.visura.ui.viewmodels.SignUpState
import com.botoni.visura.ui.viewmodels.SignUpViewModel
import com.botoni.visura.ui.viewmodels.UiState
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    onSignInClick: () -> Unit,
    viewModel: SignUpViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarType by remember { mutableStateOf(SnackbarType.DEFAULT) }

    HandleSignUpEvents(
        events = viewModel.events,
        snackBarHostState = snackbarHostState,
        onNavigate = onSignUpSuccess,
        onSnackbarTypeChange = { snackbarType = it }
    )

    SignUpScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        snackbarType = snackbarType,
        onEmailChange = { email ->
            viewModel.setEmail(email)
            viewModel.clearErrorsAndState()
        },
        onPasswordChange = { password ->
            viewModel.setPassword(password)
            viewModel.clearErrorsAndState()
        },
        onConfirmPasswordChange = { confirmPassword ->
            viewModel.setConfirmPassword(confirmPassword)
            viewModel.clearErrorsAndState()
        },
        onTogglePassword = viewModel::togglePasswordVisibility,
        onToggleConfirmPassword = viewModel::toggleConfirmPasswordVisibility,
        onSignUp = viewModel::signUpWithEmail,
        onGoogleSignUp = viewModel::signUpWithGoogle,
        onSignInClick = onSignInClick
    )
}

@Composable
private fun HandleSignUpEvents(
    events: SharedFlow<SignUpEvent>,
    snackBarHostState: SnackbarHostState,
    onNavigate: () -> Unit,
    onSnackbarTypeChange: (SnackbarType) -> Unit
) {
    LaunchedEffect(Unit) {
        events.collect { event ->
            when (event) {
                is SignUpEvent.ShowMessage -> {
                    onSnackbarTypeChange(
                        if (event.isSuccess) SnackbarType.SUCCESS else SnackbarType.ERROR
                    )

                    snackBarHostState.showSnackbar(
                        message = event.message,
                        duration = when {
                            event.isSuccess -> SnackbarDuration.Short
                            event.errorType == ErrorType.VALIDATION || event.errorType == ErrorType.UNKNOWN -> SnackbarDuration.Short
                            else -> SnackbarDuration.Long
                        }
                    )
                }
                SignUpEvent.NavigateToHome -> onNavigate()
            }
        }
    }
}

@Composable
private fun SignUpScreenContent(
    state: SignUpState,
    snackbarHostState: SnackbarHostState,
    snackbarType: SnackbarType,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onTogglePassword: () -> Unit,
    onToggleConfirmPassword: () -> Unit,
    onSignUp: () -> Unit,
    onGoogleSignUp: () -> Unit,
    onSignInClick: () -> Unit
) {
    DemoTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                snackbarHost = {
                    StandardSnackbar(
                        hostState = snackbarHostState,
                        type = snackbarType
                    )
                }
            ) { paddingValues ->
                SignUpForm(
                    state = state,
                    onEmailChange = onEmailChange,
                    onPasswordChange = onPasswordChange,
                    onConfirmPasswordChange = onConfirmPasswordChange,
                    onTogglePassword = onTogglePassword,
                    onToggleConfirmPassword = onToggleConfirmPassword,
                    onSignUp = onSignUp,
                    onGoogleSignUp = onGoogleSignUp,
                    onSignInClick = onSignInClick,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun SignUpForm(
    state: SignUpState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onTogglePassword: () -> Unit,
    onToggleConfirmPassword: () -> Unit,
    onSignUp: () -> Unit,
    onGoogleSignUp: () -> Unit,
    onSignInClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        SignUpHeader()

        Spacer(modifier = Modifier.height(32.dp))

        SignUpInputs(
            emailState = state.email,
            passwordState = state.password,
            confirmPasswordState = state.confirmPassword,
            showPassword = state.showPassword,
            showConfirmPassword = state.showConfirmPassword,
            isEmailLoading = state.isEmailLoading,
            isGoogleLoading = state.isGoogleLoading,
            state = state,
            onEmailChange = onEmailChange,
            onPasswordChange = onPasswordChange,
            onConfirmPasswordChange = onConfirmPasswordChange,
            onTogglePassword = onTogglePassword,
            onToggleConfirmPassword = onToggleConfirmPassword,
            onSignUp = onSignUp,
            onSignInClick = onSignInClick
        )

        Spacer(modifier = Modifier.height(24.dp))

        DividerWithText()

        Spacer(modifier = Modifier.height(24.dp))

        GoogleButton(
            isLoading = state.isGoogleLoading,
            isEnabled = !state.isEmailLoading && !state.isGoogleLoading &&
                    state.uiState !is UiState.Success,
            onClick = onGoogleSignUp
        )
    }
}

@Composable
private fun SignUpHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Crie sua conta!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Preencha os dados abaixo para começar",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}

@Composable
private fun SignUpInputs(
    emailState: FieldState,
    passwordState: FieldState,
    confirmPasswordState: FieldState,
    showPassword: Boolean,
    showConfirmPassword: Boolean,
    isEmailLoading: Boolean,
    isGoogleLoading: Boolean,
    state: SignUpState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onTogglePassword: () -> Unit,
    onToggleConfirmPassword: () -> Unit,
    onSignUp: () -> Unit,
    onSignInClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        EmailField(
            state = emailState,
            isEnabled = !isEmailLoading && !isGoogleLoading && state.uiState !is UiState.Success,
            onValueChange = onEmailChange
        )

        PasswordField(
            state = passwordState,
            showPassword = showPassword,
            isEnabled = !isEmailLoading && !isGoogleLoading && state.uiState !is UiState.Success,
            onValueChange = onPasswordChange,
            onTogglePassword = onTogglePassword
        )

        ConfirmPasswordField(
            state = confirmPasswordState,
            showPassword = showConfirmPassword,
            isEnabled = !isEmailLoading && !isGoogleLoading && state.uiState !is UiState.Success,
            onValueChange = onConfirmPasswordChange,
            onTogglePassword = onToggleConfirmPassword
        )

        SignInLink(
            onClick = onSignInClick,
            enabled = !isEmailLoading && !isGoogleLoading && state.uiState !is UiState.Success
        )

        SignUpButton(
            isLoading = isEmailLoading,
            isEnabled = !isEmailLoading && !isGoogleLoading &&
                    emailState.value.isNotEmpty() &&
                    passwordState.value.isNotEmpty() && confirmPasswordState.value.isNotEmpty() &&
                    state.uiState !is UiState.Success,
            onClick = onSignUp
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
private fun ConfirmPasswordField(
    state: FieldState,
    showPassword: Boolean,
    isEnabled: Boolean,
    onValueChange: (String) -> Unit,
    onTogglePassword: () -> Unit
) {
    StandardTextField(
        value = state.value,
        placeholder = "Confirmar senha",
        label = "Confirme sua senha",
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
private fun SignInLink(
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        StandardTextButton(
            text = "Já possui uma conta?",
            onClick = onClick,
            enabled = enabled
        )
    }
}

@Composable
private fun SignUpButton(
    isLoading: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    StandardButton(
        text = if (isLoading) "Criando conta..." else "Criar conta",
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
        text = if (isLoading) "Conectando..." else "Cadastrar com Google",
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
private fun SignUpScreenPreview() {
    DemoTheme {
        SignUpForm(
            state = SignUpState(),
            onEmailChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onTogglePassword = {},
            onToggleConfirmPassword = {},
            onSignUp = {},
            onGoogleSignUp = {},
            onSignInClick = {},
            modifier = Modifier
        )
    }
}