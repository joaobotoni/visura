package com.visura.ui.screens.createaccount

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.visura.ui.theme.BorderGray
import com.visura.ui.theme.NavyBlue
import com.visura.ui.theme.TextGray

@Composable
fun CreateAccountInfoScreen(
    onBackClick: () -> Unit = {},
    onContinueClick: () -> Unit = {}
) {
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var confirmarSenha by remember { mutableStateOf("") }
    var senhaVisivel by remember { mutableStateOf(false) }

    val emailValido = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

    val podeContinuar = nome.isNotBlank() && emailValido &&
            senha.length >= 6 && senha == confirmarSenha

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .safeDrawingPadding()
            .padding(24.dp)
    ) {
        IconButton(onClick = onBackClick, modifier = Modifier.padding(bottom = 4.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
        }

        Text("Criar conta", fontSize = 22.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Crie seu login com e-mail e senha. Na próxima etapa você vincula seus documentos.",
            fontSize = 14.sp,
            color = TextGray
        )

        Spacer(modifier = Modifier.height(24.dp))

        LabeledField("Nome completo *", nome, { nome = it }, placeholder = "Seu nome completo")
        Spacer(modifier = Modifier.height(12.dp))
        LabeledField(
            "E-mail *", email, { email = it },
            placeholder = "seuemail@exemplo.com",
            keyboardType = KeyboardType.Email
        )
        Spacer(modifier = Modifier.height(12.dp))
        LabeledField(
            "Senha *", senha, { senha = it },
            isPassword = true,
            senhaVisivel = senhaVisivel,
            onToggleVisibility = { senhaVisivel = !senhaVisivel }
        )
        Spacer(modifier = Modifier.height(12.dp))
        LabeledField(
            "Confirmar senha *", confirmarSenha, { confirmarSenha = it },
            isPassword = true,
            senhaVisivel = senhaVisivel,
            onToggleVisibility = { senhaVisivel = !senhaVisivel }
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onContinueClick,
            enabled = podeContinuar,
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("Continuar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White)
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun LabeledField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    senhaVisivel: Boolean = false,
    onToggleVisibility: (() -> Unit)? = null
) {
    Text(label, fontSize = 13.sp, color = TextGray)
    Spacer(modifier = Modifier.height(4.dp))
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { if (placeholder.isNotEmpty()) Text(placeholder) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isPassword) KeyboardType.Password else keyboardType
        ),
        visualTransformation = if (isPassword && !senhaVisivel)
            PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = if (isPassword && onToggleVisibility != null) {
            {
                IconButton(onClick = onToggleVisibility) {
                    Icon(
                        imageVector = if (senhaVisivel) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = null
                    )
                }
            }
        } else null,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = BorderGray,
            focusedBorderColor = NavyBlue
        ),
        modifier = Modifier.fillMaxWidth()
    )
}