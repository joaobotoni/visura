package com.botoni.visura.ui.presenter.elements.snackbar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

enum class SnackbarType(
    val containerColor: Color,
    val contentColor: Color,
    val iconColor: Color,
    val icon: ImageVector
) {
    SUCCESS(
        containerColor = Color(0xFF104C12),
        contentColor = Color.White,
        iconColor = Color(0xFF4CAF50),
        icon = Icons.Default.CheckCircle
    ),
    ERROR(
        containerColor = Color(0xFFDD493E),
        contentColor = Color.White,
        iconColor = Color.White,
        icon = Icons.Outlined.Warning
    ),
    DEFAULT(
        containerColor = Color(0xFF424242),
        contentColor = Color.White,
        iconColor = Color.White,
        icon = Icons.Outlined.Warning
    )
}

@Composable
fun StandardSnackbar(
    hostState: SnackbarHostState,
    type: SnackbarType = SnackbarType.DEFAULT
) {
    SnackbarHost(
        hostState = hostState,
        modifier = Modifier.padding(16.dp)
    ) { data ->
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = type.containerColor,
                contentColor = type.contentColor
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MessageContent(
                    message = data.visuals.message,
                    type = type,
                    modifier = Modifier.weight(1f)
                )
                DismissButton(
                    onDismiss = data::dismiss,
                    tint = type.contentColor
                )
            }
        }
    }
}

@Composable
private fun MessageContent(
    message: String,
    type: SnackbarType,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = type.icon,
            contentDescription = null,
            tint = type.iconColor
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun DismissButton(onDismiss: () -> Unit, tint: Color) {
    IconButton(
        onClick = onDismiss,
        modifier = Modifier.padding(start = 8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = null,
            tint = tint
        )
    }
}