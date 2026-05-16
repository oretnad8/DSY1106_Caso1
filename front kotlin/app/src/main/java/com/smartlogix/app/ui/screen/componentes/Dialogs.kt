package com.smartlogix.app.ui.screen.componentes

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Diálogo de confirmación genérico
 */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmText: String = "Confirmar",
    dismissText: String = "Cancelar",
    showDialog: Boolean
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = null
                )
            },
            title = {
                Text(text = title)
            },
            text = {
                Text(text = message)
            },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text(confirmText)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(dismissText)
                }
            }
        )
    }
}

/**
 * Diálogo de eliminación (con advertencia)
 */
@Composable
fun DeleteDialog(
    title: String = "¿Eliminar?",
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    showDialog: Boolean
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFD32F2F)
                )
            },
            title = {
                Text(text = title)
            },
            text = {
                Text(text = message)
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirm,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFFD32F2F)
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * Diálogo de éxito
 */
@Composable
fun SuccessDialog(
    title: String = "¡Éxito!",
    message: String,
    onDismiss: () -> Unit,
    showDialog: Boolean
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50)
                )
            },
            title = {
                Text(text = title)
            },
            text = {
                Text(text = message)
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("Aceptar")
                }
            }
        )
    }
}

/**
 * Diálogo de error
 */
@Composable
fun ErrorDialog(
    title: String = "Error",
    message: String,
    onDismiss: () -> Unit,
    showDialog: Boolean
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    tint = Color(0xFFD32F2F)
                )
            },
            title = {
                Text(text = title)
            },
            text = {
                Text(text = message)
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("Aceptar")
                }
            }
        )
    }
}

/**
 * Diálogo de carga (loading)
 */
@Composable
fun LoadingDialog(
    message: String = "Cargando...",
    showDialog: Boolean
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { /* No se puede cerrar */ },
            text = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = message)
                }
            },
            confirmButton = {}
        )
    }
}

/**
 * Diálogo para ingresar un motivo/observación
 */
@Composable
fun MotivoDialog(
    title: String,
    label: String = "Motivo",
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    showDialog: Boolean,
    required: Boolean = true
) {
    var texto by remember { mutableStateOf("") }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(text = title)
            },
            text = {
                Column {
                    Text(
                        text = if (required) "$label (obligatorio)" else label,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    MultilineTextField(
                        value = texto,
                        onValueChange = { texto = it },
                        label = label,
                        placeholder = "Ingrese el $label..."
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (!required || texto.isNotBlank()) {
                            onConfirm(texto)
                            texto = ""
                        }
                    },
                    enabled = !required || texto.isNotBlank()
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    texto = ""
                    onDismiss()
                }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * Diálogo de información simple
 */
@Composable
fun InfoDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    showDialog: Boolean
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(text = title)
            },
            text = {
                Text(text = message)
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("Entendido")
                }
            }
        )
    }
}

