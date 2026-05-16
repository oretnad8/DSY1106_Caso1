package com.smartlogix.app.ui.screen.componentes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.smartlogix.app.models.UbicacionFormatter

/**
 * Diálogo para asignar un producto a una ubicación
 * Versión actualizada con soporte para 5 pasillos y escáner de código de barras
 */
@Composable
fun AsignarUbicacionDialog(
    codigoBarras: String,
    onDismiss: () -> Unit,
    onConfirm: (String, Int) -> Unit, // codigoUbicacion (formato P1-A-01), cantidad
    modifier: Modifier = Modifier
) {
    var selectedPasillo by remember { mutableStateOf(1) }
    var selectedPiso by remember { mutableStateOf("A") }
    var numeroUbicacion by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showBarcodeScanner by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Título
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Asignar Ubicación",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Código de Barras del producto
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Código de Barras",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = codigoBarras,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botón de escanear código de barras
                OutlinedButton(
                    onClick = { showBarcodeScanner = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Escanear Código de Ubicación")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Selector de pasillo
                Text(
                    text = "Pasillo",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    (1..5).forEach { pasillo ->
                        FilterChip(
                            selected = selectedPasillo == pasillo,
                            onClick = { selectedPasillo = pasillo },
                            label = { Text("P$pasillo") },
                            leadingIcon = if (selectedPasillo == pasillo) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Selector de piso
                Text(
                    text = "Piso",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("A", "B", "C").forEach { piso ->
                        FilterChip(
                            selected = selectedPiso == piso,
                            onClick = { selectedPiso = piso },
                            label = { Text("Piso $piso") },
                            leadingIcon = if (selectedPiso == piso) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Número de ubicación
                OutlinedTextField(
                    value = numeroUbicacion,
                    onValueChange = { 
                        if (it.isEmpty() || (it.toIntOrNull() != null && it.toInt() in 1..60)) {
                            numeroUbicacion = it
                        }
                    },
                    label = { Text("Número (1-60)") },
                    placeholder = { Text("Ej: 12") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Cantidad
                OutlinedTextField(
                    value = cantidad,
                    onValueChange = { 
                        if (it.isEmpty() || it.toIntOrNull() != null) {
                            cantidad = it
                        }
                    },
                    label = { Text("Cantidad") },
                    placeholder = { Text("Ej: 10") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Vista previa del código
                if (numeroUbicacion.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Código de Ubicación",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                val numero = numeroUbicacion.toIntOrNull() ?: 0
                                Text(
                                    text = UbicacionFormatter.formatCodigo(
                                        selectedPasillo,
                                        selectedPiso[0],
                                        numero
                                    ),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }

                // Mensaje de error
                if (showError) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            val numero = numeroUbicacion.toIntOrNull()
                            val cant = cantidad.toIntOrNull()

                            when {
                                numero == null || numero !in 1..60 -> {
                                    showError = true
                                    errorMessage = "Número de ubicación inválido (1-60)"
                                }
                                cant == null || cant <= 0 -> {
                                    showError = true
                                    errorMessage = "Cantidad inválida"
                                }
                                else -> {
                                    val codigoUbicacion = UbicacionFormatter.formatCodigo(
                                        selectedPasillo,
                                        selectedPiso[0],
                                        numero
                                    )
                                    onConfirm(codigoUbicacion, cant)
                                    onDismiss()
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = numeroUbicacion.isNotEmpty() && cantidad.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Asignar")
                    }
                }
            }
        }
    }

    // Escáner de código de barras para ubicaciones
    if (showBarcodeScanner) {
        BarcodeScanner(
            onBarcodeScanned = { scannedCode ->
                // Parsear el código escaneado (formato P1/A1) a formato estándar (P1-A-01)
                val parsedCode = UbicacionFormatter.parseScannedCode(scannedCode)
                
                if (parsedCode != null) {
                    // Extraer componentes del código parseado
                    val components = UbicacionFormatter.parseCodigo(parsedCode)
                    if (components != null) {
                        val (pasillo, piso, numero) = components
                        selectedPasillo = pasillo
                        selectedPiso = piso.toString()
                        numeroUbicacion = numero.toString()
                        showBarcodeScanner = false
                        showError = false
                    }
                } else {
                    showError = true
                    errorMessage = "Código de ubicación inválido. Formato esperado: P1/A1"
                    showBarcodeScanner = false
                }
            },
            onClose = {
                showBarcodeScanner = false
            }
        )
    }
}


