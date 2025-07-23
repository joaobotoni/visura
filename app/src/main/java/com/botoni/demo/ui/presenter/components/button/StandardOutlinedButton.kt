package com.botoni.demo.ui.presenter.components.button

import androidx.annotation.DrawableRes
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun StandardOutlinedButton(
    text: String,
    @DrawableRes icon: Int? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(onClick = onClick,  modifier = modifier)
    {
        if (icon != null) {
            Icon(painter = painterResource(id = icon), contentDescription = null)
        }
        Text(text = text)
    }
}
