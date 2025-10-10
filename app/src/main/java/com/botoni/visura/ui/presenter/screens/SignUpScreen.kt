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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.botoni.visura.R
import com.botoni.visura.ui.presenter.elements.button.StandardButton
import com.botoni.visura.ui.presenter.elements.button.StandardOutlinedButton
import com.botoni.visura.ui.presenter.elements.button.StandardTextButton
import com.botoni.visura.ui.presenter.elements.field.StandardTextField
import com.botoni.visura.ui.presenter.theme.DemoTheme

@Composable
fun SignUpScreen() {
    SignUpScreenContent()
}

@Composable
private fun SignUpScreenContent() {
    DemoTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                snackbarHost = {
                    // Snackbar removido, pois depende de estados e eventos
                }
            ) { paddingValues ->
                SignUpForm(
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun SignUpForm(
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

        SignUpInputFields()

        Spacer(modifier = Modifier.height(16.dp))

        SignInLink()

        Spacer(modifier = Modifier.height(20.dp))

        SignUpButton()

        Spacer(modifier = Modifier.height(24.dp))

        DividerWithText()

        Spacer(modifier = Modifier.height(24.dp))

        GoogleButton()
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
private fun SignUpInputFields(
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        EmailField()

        PasswordField()

        ConfirmPasswordField()
    }
}

@Composable
private fun EmailField(
) {
    StandardTextField(
        value = "", // Valor fixo, pois estados foram removidos
        placeholder = "Email",
        label = "Digite seu email",
        onValueChange = {},
        enabled = true,
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
        isError = false,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun PasswordField(
) {
    StandardTextField(
        value = "", // Valor fixo, pois estados foram removidos
        placeholder = "Senha",
        label = "Digite sua senha",
        onValueChange = {},
        enabled = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(
                onClick = {},
                enabled = true
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = Icons.Default.Visibility,
                    contentDescription = "Mostrar senha",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        visualTransformation = VisualTransformation.None,
        isError = false,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ConfirmPasswordField(
) {
    StandardTextField(
        value = "", // Valor fixo, pois estados foram removidos
        placeholder = "Confirmar senha",
        label = "Confirme sua senha",
        onValueChange = {},
        enabled = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(
                onClick = {},
                enabled = true
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = Icons.Default.Visibility,
                    contentDescription = "Mostrar senha",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        visualTransformation = VisualTransformation.None,
        isError = false,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun SignInLink(
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        StandardTextButton(
            text = "Já possui uma conta?",
            onClick = {},
            enabled = true
        )
    }
}

@Composable
private fun SignUpButton(
) {
    StandardButton(
        text = "Criar conta",
        onClick = {},
        enabled = true,
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
) {
    StandardOutlinedButton(
        text = "Cadastrar com Google",
        onClick = {},
        enabled = true,
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
        SignUpForm()
    }
}