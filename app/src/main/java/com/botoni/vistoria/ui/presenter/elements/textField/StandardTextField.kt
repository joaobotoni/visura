package com.botoni.vistoria.ui.presenter.elements.textField

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun StandardTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    if (visualTransformation != null) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { label?.let { Text(it) } },
            placeholder = { placeholder?.let { Text(it) } },
            modifier = modifier.fillMaxWidth(),
            keyboardOptions = keyboardOptions,
            trailingIcon = trailingIcon,
            visualTransformation = visualTransformation,
            textStyle = TextStyle(color = Color.Gray),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
    }
}