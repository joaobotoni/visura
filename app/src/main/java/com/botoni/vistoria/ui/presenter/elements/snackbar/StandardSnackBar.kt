package com.botoni.vistoria.ui.presenter.elements.snackbar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp

sealed class SnackbarType(
    val containerColor: Color,
    val contentColor: Color,
    val iconColor: Color,
    val icon: ImageVector
) {
    object Success : SnackbarType(
        containerColor = Color(0xFF104C12),
        contentColor = Color(0xFFFFFFFF),
        iconColor = Color(0xFF4CAF50),
        icon = Icons.Default.CheckCircle
    )

    object Error : SnackbarType(
        containerColor = Color(0xFFDD493E),
        contentColor = Color(0xFFFFFFFF),
        iconColor = Color(0xFFFFFFFF),
        icon = Icons.Outlined.Warning
    )

}

@Composable
fun StandardSnackbar(
    snackBarHostState: SnackbarHostState,
    snackBarState: Boolean
) {
    val snackbarType = if (snackBarState) {
        SnackbarType.Success
    } else {
        SnackbarType.Error
    }

    SnackbarHost(
        hostState = snackBarHostState,
        modifier = Modifier.padding(16.dp),
        snackbar = {
            snackBarHostState.currentSnackbarData?.let { snackbarData ->
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = snackbarType.containerColor,
                        contentColor = snackbarType.contentColor
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
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(
                                imageVector = snackbarType.icon,
                                contentDescription = null,
                                tint = snackbarType.iconColor
                            )
                            Text(
                                text = snackbarData.visuals.message,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        IconButton(
                            onClick = { snackbarData.dismiss() },
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Fechar",
                                tint = snackbarType.contentColor
                            )
                        }
                    }
                }
            }
        }
    )
}
