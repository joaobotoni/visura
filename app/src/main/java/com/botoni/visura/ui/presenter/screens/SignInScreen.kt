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
import androidx.compose.ui.res.stringResource
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
import com.botoni.visura.domain.model.Email
import com.botoni.visura.domain.model.Password
import com.botoni.visura.ui.presenter.elements.button.StandardButton
import com.botoni.visura.ui.presenter.elements.button.StandardOutlinedButton
import com.botoni.visura.ui.presenter.elements.button.StandardTextButton
import com.botoni.visura.ui.presenter.elements.field.StandardTextField
import com.botoni.visura.ui.presenter.elements.snackbar.SnackbarType
import com.botoni.visura.ui.presenter.elements.snackbar.StandardSnackbar
import com.botoni.visura.ui.presenter.theme.DemoTheme
import com.botoni.visura.ui.viewmodels.SignInEvent
import com.botoni.visura.ui.viewmodels.SignInState
import com.botoni.visura.ui.viewmodels.SignInViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SignInScreen(
    viewModel: SignInViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarType by remember { mutableStateOf(SnackbarType.DEFAULT) }

    LaunchedEffect(Unit) {
        viewModel.event.collectLatest { event ->
            snackbarType = when (event) {
                is SignInEvent.Success -> SnackbarType.SUCCESS
                is SignInEvent.Error -> SnackbarType.ERROR
            }
            val message = when (event) {
                is SignInEvent.Success -> event.message
                is SignInEvent.Error -> event.message
            }
            snackbarHostState.showSnackbar(message)
        }
    }

    SignInScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        snackbarType = snackbarType,
        onEmailChange = { viewModel.setEmail(Email(it)) },
        onPasswordChange = { viewModel.setPassword(Password(it)) },
        onTogglePasswordVisibility = viewModel::togglePassword,
        onSignInWithEmail = viewModel::signInWithEmail,
        onSignInWithGoogle = viewModel::signInWithGoogle,
        onSignUpClick = { /* TODO: Navigate to sign up screen if needed */ }
    )
}

@Composable
private fun SignInScreenContent(
    state: SignInState,
    snackbarHostState: SnackbarHostState,
    snackbarType: SnackbarType,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onSignInWithEmail: () -> Unit,
    onSignInWithGoogle: () -> Unit,
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
                        hostState = snackbarHostState,
                        type = snackbarType
                    )
                }
            ) { paddingValues ->
                SignInForm(
                    modifier = Modifier.padding(paddingValues),
                    state = state,
                    onEmailChange = onEmailChange,
                    onPasswordChange = onPasswordChange,
                    onTogglePasswordVisibility = onTogglePasswordVisibility,
                    onSignInWithEmail = onSignInWithEmail,
                    onSignInWithGoogle = onSignInWithGoogle,
                    onSignUpClick = onSignUpClick
                )
            }
        }
    }
}

@Composable
private fun SignInForm(
    modifier: Modifier = Modifier,
    state: SignInState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onSignInWithEmail: () -> Unit,
    onSignInWithGoogle: () -> Unit,
    onSignUpClick: () -> Unit
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
        SignInHeader()

        Spacer(modifier = Modifier.height(32.dp))

        SignInInputFields(
            state = state,
            onEmailChange = onEmailChange,
            onPasswordChange = onPasswordChange,
            onTogglePasswordVisibility = onTogglePasswordVisibility
        )

        Spacer(modifier = Modifier.height(16.dp))

        SignUpLink(
            onSignUpClick = onSignUpClick
        )

        Spacer(modifier = Modifier.height(20.dp))

        LoginButton(
            enabled = !state.emailLoading,
            onClick = onSignInWithEmail
        )

        Spacer(modifier = Modifier.height(24.dp))

        DividerWithText()

        Spacer(modifier = Modifier.height(24.dp))

        GoogleButton(
            enabled = !state.googleLoading,
            onClick = onSignInWithGoogle
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
            text = stringResource(R.string.sign_in_header),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Text(
            text = stringResource(R.string.sign_in_header_con),
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}

@Composable
private fun SignInInputFields(
    state: SignInState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        EmailField(
            value = state.email.value,
            onValueChange = onEmailChange
        )

        PasswordField(
            value = state.password.value,
            onValueChange = onPasswordChange,
            showPassword = state.showPassword,
            onToggleVisibility = onTogglePasswordVisibility
        )
    }
}

@Composable
private fun EmailField(
    value: String,
    onValueChange: (String) -> Unit
) {
    StandardTextField(
        value = value,
        placeholder = stringResource(R.string.email_field_placeholder_login),
        label = stringResource(R.string.email_field_label_login),
        onValueChange = onValueChange,
        enabled = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        visualTransformation = VisualTransformation.None,
        isError = false,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    showPassword: Boolean,
    onToggleVisibility: () -> Unit
) {
    StandardTextField(
        value = value,
        placeholder = stringResource(R.string.password_field_placeholder_login),
        label = stringResource(R.string.password_field_label_login),
        onValueChange = onValueChange,
        enabled = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(
                onClick = onToggleVisibility,
                enabled = true
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
        isError = false,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun SignUpLink(
    onSignUpClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        StandardTextButton(
            text = stringResource(R.string.sign_up_link),
            onClick = onSignUpClick,
            enabled = true
        )
    }
}

@Composable
private fun LoginButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    StandardButton(
        text = stringResource(R.string.login_button),
        onClick = onClick,
        enabled = enabled,
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
            text = stringResource(R.string.divider_with_text),
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
    enabled: Boolean,
    onClick: () -> Unit
) {
    StandardOutlinedButton(
        text = stringResource(R.string.google_login_button),
        onClick = onClick,
        enabled = enabled,
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
        SignInScreenContent(
            state = SignInState(),
            snackbarHostState = SnackbarHostState(),
            snackbarType = SnackbarType.DEFAULT,
            onEmailChange = {},
            onPasswordChange = {},
            onTogglePasswordVisibility = {},
            onSignInWithEmail = {},
            onSignInWithGoogle = {},
            onSignUpClick = {}
        )
    }
}