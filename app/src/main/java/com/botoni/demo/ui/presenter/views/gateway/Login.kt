package com.botoni.demo.ui.presenter.views.gateway

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.botoni.demo.R
import com.botoni.demo.ui.presenter.components.button.StandardButton
import com.botoni.demo.ui.presenter.components.button.StandardOutlinedButton
import com.botoni.demo.ui.presenter.components.button.StandardTextButton
import com.botoni.demo.ui.presenter.components.textField.StandardTextField
import com.botoni.demo.ui.presenter.theme.DemoTheme
@Composable
fun Login(modifier: Modifier = Modifier) {
    DemoTheme {
        Surface(modifier = modifier.fillMaxSize()) {
            LoginForm()
        }
    }
}
@Composable
fun LoginForm(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisibility by remember { mutableStateOf(false) }

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
            onValueChange = { email = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            visualTransformation = VisualTransformation.None
        )

        StandardTextField(
            value = password,
            placeholder = "Password",
            label = "Enter your password",
            onValueChange = { password = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = {
                    passwordVisibility = !passwordVisibility
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
                eventOnClick(context, "Continue login clicked")
            },

            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

        StandardOutlinedButton(
            text = "Login with Google",
            onClick = {
                eventOnClick(context, "Google login clicked")
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
