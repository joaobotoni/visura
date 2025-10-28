package com.visura.ui.presenter.elements.button

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource

@Composable
fun StandardOutlinedButton(
    text: String,
    @DrawableRes icon: Int? = null,
    onClick: () -> Unit,
    enabled: Boolean,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    OutlinedButton(onClick = onClick, modifier = modifier, enabled = enabled)
    {
        if (icon != null) {
            Icon(painter = painterResource(id = icon), contentDescription = null)
        }
        Text(text = text)
    }
}
