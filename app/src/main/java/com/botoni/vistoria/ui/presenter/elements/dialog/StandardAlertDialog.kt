package com.botoni.vistoria.ui.presenter.elements.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable


@Composable
fun StandardAlertDialog(
    onDismissRequest: () -> Unit,
    dialogTitle: String,
    dialogText: String,
){
    AlertDialog(
        onDismissRequest = { onDismissRequest()},
        title = { Text(dialogTitle) },
        text = { Text(dialogText) },
        confirmButton = {
            TextButton(
                onClick = { onDismissRequest() }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onDismissRequest() }
            ) {
                Text("Cancel")
            }
        }
    )
}