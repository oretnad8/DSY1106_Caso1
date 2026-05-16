package com.smartlogix.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.smartlogix.app.ui.screen.componentes.*

/**
 * Pantalla para asignar un producto a una o varias ubicaciones
 * 
 * Permite:
 * - Seleccionar piso (A, B, C)
 * - Ingresar número de ubicación (1-60)
 * - Especificar cantidad
 * - Asignar a múltiples ubicaciones
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsignarUbicacionScreen(
    sku: String,
    onNavigateBack: () -> Unit,
    onAsignar: (String, String, Int) -> Unit, // sku, codigoUbicacion, cantidad
    modifier: Modifier = Modifier
) {
    // Estados
    var selectedPiso by remember { mutableStateOf("A") }
    var numeroUbicacion by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    // Lista de asignaciones pendientes
    var asignacionesPendientes by remember { mutableStateOf(listOf<AsignacionPendiente>()) }

    Scaffold(
        topBar = {
            BackTopBar(
                title = "Asignar Ubicación",
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Información del producto
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Inventory,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Producto",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = sku,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Formulario de asignación
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Nueva Asignación",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

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
                                            modifier = Modifier.size(18.dp)
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
                        label = { Text("Número de Ubicación (1-60)") },
                        placeholder = { Text("Ej: 12") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

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
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Vista previa del código de ubicación
                    if (numeroUbicacion.isNotEmpty()) {
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
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Código de Ubicación",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Text(
                                        text = "$selectedPiso-${numeroUbicacion.padStart(2, '0')}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botón para agregar a la lista
                    Button(
                        onClick = {
                            val numero = numeroUbicacion.toIntOrNull()
                            val cant = cantidad.toIntOrNull()

                            when {
                                numero == null || numero !in 1..60 -> {
                                    showError = true
                                    errorMessage = "Ingrese un número de ubicación válido (1-60)"
                                }
                                cant == null || cant <= 0 -> {
                                    showError = true
                                    errorMessage = "Ingrese una cantidad válida"
                                }
                                else -> {
                                    val codigoUbicacion = "$selectedPiso-${numero.toString().padStart(2, '0')}"
                                    asignacionesPendientes = asignacionesPendientes + AsignacionPendiente(
                                        codigoUbicacion = codigoUbicacion,
                                        cantidad = cant
                                    )
                                    // Limpiar campos
                                    numeroUbicacion = ""
                                    cantidad = ""
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = numeroUbicacion.isNotEmpty() && cantidad.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Agregar a la Lista")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Lista de asignaciones pendientes
            if (asignacionesPendientes.isNotEmpty()) {
                Text(
                    text = "Asignaciones a Realizar (${asignacionesPendientes.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(asignacionesPendientes) { asignacion ->
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = asignacion.codigoUbicacion,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Cantidad: ${asignacion.cantidad}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        asignacionesPendientes = asignacionesPendientes.filter { it != asignacion }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Eliminar",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botón para confirmar todas las asignaciones
                Button(
                    onClick = {
                        asignacionesPendientes.forEach { asignacion ->
                            onAsignar(sku, asignacion.codigoUbicacion, asignacion.cantidad)
                        }
                        onNavigateBack()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Confirmar Asignaciones")
                }
            }
        }

        // Snackbar para errores
        if (showError) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    TextButton(onClick = { showError = false }) {
                        Text("OK")
                    }
                }
            ) {
                Text(errorMessage)
            }
        }
    }
}

/**
 * Clase de datos para asignaciones pendientes
 */
data class AsignacionPendiente(
    val codigoUbicacion: String,
    val cantidad: Int
)


