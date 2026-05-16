package com.smartlogix.app.ui.screen.componentes

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Botón genérico para escanear cualquier código de barras/QR
 * Abre el escáner en un diálogo de pantalla completa
 *
 * @param onCodeScanned Callback cuando se detecta un código
 * @param label Etiqueta del botón
 * @param modifier Modificador de composable
 * @param isFullWidth Si true, el botón ocupa todo el ancho disponible
 */
@Composable
fun ScanSimpleButton(
    onCodeScanned: (String) -> Unit,
    label: String = "Escanear Código",
    modifier: Modifier = Modifier,
    isFullWidth: Boolean = false
) {
    var showBarcodeScanner by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Botón
    Button(
        onClick = { showBarcodeScanner = true },
        modifier = if (isFullWidth) {
            modifier.fillMaxWidth()
        } else {
            modifier
        },
        contentPadding = PaddingValues(
            horizontal = 16.dp,
            vertical = 12.dp
        )
    ) {
        Icon(
            imageVector = Icons.Default.QrCodeScanner,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelLarge
        )
    }

    // Mostrar error si es necesario
    if (showError) {
        AlertDialog(
            onDismissRequest = { showError = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { showError = false }) {
                    Text("Aceptar")
                }
            }
        )
    }

    // Escáner de código de barras en pantalla completa
    if (showBarcodeScanner) {
        Dialog(
            onDismissRequest = { showBarcodeScanner = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false, // Ocupar toda la pantalla
                dismissOnBackPress = true,
                dismissOnClickOutside = false
            )
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                BarcodeScanner(
                    onBarcodeScanned = { scannedCode ->
                        if (scannedCode.isNotBlank()) {
                            onCodeScanned(scannedCode)
                            showBarcodeScanner = false
                        } else {
                            // Opcional: Mostrar error o simplemente ignorar lecturas vacías
                        }
                    },
                    onClose = {
                        showBarcodeScanner = false
                    }
                )
            }
        }
    }
}


