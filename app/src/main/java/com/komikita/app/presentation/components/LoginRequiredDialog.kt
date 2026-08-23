package com.komikita.app.presentation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun LoginRequiredDialog(
    title: String = "Login Required",
    message: String = "Fitur ini hanya tersedia untuk pengguna terautentikasi. Silakan login untuk melanjutkan.",
    onDismiss: () -> Unit,
    onLoginClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = { Text(text = message) },
        confirmButton = {
            TextButton(onClick = {
                onDismiss()
                onLoginClick()
            }) {
                Text(text = "Login Sekarang")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Batal")
            }
        }
    )
}
