package com.smartlogix.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartlogix.app.models.TipoMovimiento
import com.smartlogix.app.models.UiState
import com.smartlogix.app.ui.screen.componentes.*
import com.smartlogix.app.viewmodels.RegistroDirectoViewModel

/**
 * Pantalla de Registro Directo de Movimientos
 * Solo para JEFE y SUPERVISOR
 *
 * A diferencia de las solicitudes, estos movimientos NO requieren aprobación
 * y se ejecutan inmediatamente en el sistema
 *
 * Tipos de movimiento:
 * - INGRESO: Nueva entrada de productos
 * - EGRESO: Salida de productos
 * - REUBICACION: Cambio de ubicación
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroDirectoScreen(
    registroDirectoViewModel: RegistroDirectoViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val registroState by registroDirectoViewModel.registroState.collectAsStateWithLifecycle()

    // Estados del formulario
    var tipoMovimiento by rememberSaveable { mutableStateOf<TipoMovimiento?>(null) }
    var codigoBarras by rememberSaveable { mutableStateOf("") }
    var cantidad by rememberSaveable { mutableStateOf("") }
    var motivo by rememberSaveable { mutableStateOf("") }
    var idUbicacionOrigen by rememberSaveable { mutableStateOf("") }
    var idUbicacionDestino by rememberSaveable { mutableStateOf("") }
    var ubicacionIngreso by rememberSaveable { mutableStateOf("") }
    var ubicacionEgreso by rememberSaveable { mutableStateOf("") }

    // Estados de validación
    var codigoBarrasError by remember { mutableStateOf<String?>(null) }
    var cantidadError by remember { mutableStateOf<String?>(null) }
    var motivoError by remember { mutableStateOf<String?>(null) }
    var ubicacionError by remember { mutableStateOf<String?>(null) }

    // Estados de UI
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showErrorDialog by remember { mutableStateOf(false) }

    // Manejar estados de registro
    LaunchedEffect(registroState) {
        when (val state = registroState) {
            is UiState.Success -> {
                showSuccessDialog = true
                // Limpiar formulario
                tipoMovimiento = null
                codigoBarras = ""
                cantidad = ""
                motivo = ""
                idUbicacionOrigen = ""
                idUbicacionDestino = ""
                ubicacionIngreso = ""
                ubicacionEgreso = ""
            }
            is UiState.Error -> {
                errorMessage = state.message
                showErrorDialog = true
            }
            else -> {}
        }
    }

    // Diálogo de confirmación
    ConfirmDialog(
        title = "Confirmar Registro",
        message = "¿Estás seguro de registrar este movimiento? Esta acción se ejecutará inmediatamente sin necesidad de aprobación.",
        onConfirm = {
            showConfirmDialog = false
            
            // Validar y ejecutar
            val isValid = validarFormulario(
                tipoMovimiento = tipoMovimiento,
                codigoBarras = codigoBarras,
                cantidad = cantidad,
                motivo = motivo,
                idUbicacionOrigen = idUbicacionOrigen,
                idUbicacionDestino = idUbicacionDestino,
                ubicacionIngreso = ubicacionIngreso,
                ubicacionEgreso = ubicacionEgreso,
                onCodigoBarrasError = { codigoBarrasError = it },
                onCantidadError = { cantidadError = it },
                onMotivoError = { motivoError = it },
                onUbicacionError = { ubicacionError = it }
            )

            if (isValid) {
                when (tipoMovimiento) {
                    TipoMovimiento.INGRESO -> {
                        registroDirectoViewModel.registrarIngreso(
                            codigoBarras = codigoBarras,
                            cantidad = cantidad.toInt(),
                            ubicacionDestino = ubicacionIngreso,
                            motivo = motivo
                        )
                    }
                    TipoMovimiento.EGRESO -> {
                        registroDirectoViewModel.registrarEgreso(
                            codigoBarras = codigoBarras,
                            cantidad = cantidad.toInt(),
                            ubicacionOrigen = ubicacionEgreso,
                            motivo = motivo
                        )
                    }
                    TipoMovimiento.REUBICACION -> {
                        registroDirectoViewModel.registrarReubicacion(
                            codigoBarras = codigoBarras,
                            cantidad = cantidad.toInt(),
                            ubicacionOrigen = idUbicacionOrigen,
                            ubicacionDestino = idUbicacionDestino,
                            motivo = motivo
                        )
                    }
                    else -> {}
                }
            }
        },
        onDismiss = { showConfirmDialog = false },
        showDialog = showConfirmDialog,
        confirmText = "Registrar",
        dismissText = "Cancelar"
    )

    // Diálogo de éxito
    SuccessDialog(
        title = "¡Movimiento Registrado!",
        message = "El movimiento ha sido registrado exitosamente en el sistema.",
        onDismiss = {
            showSuccessDialog = false
            registroDirectoViewModel.clearState()
            onNavigateBack()
        },
        showDialog = showSuccessDialog
    )

    // Diálogo de error
    ErrorDialog(
        message = errorMessage,
        onDismiss = {
            showErrorDialog = false
            registroDirectoViewModel.clearState()
        },
        showDialog = showErrorDialog
    )

    Scaffold(
        topBar = {
            BackTopBar(
                title = "Registro Directo",
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Seleccionar tipo de movimiento
            Text(
                text = "Tipo de Movimiento",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TipoMovimiento.values().forEach { tipo ->
                    FilterChip(
                        selected = tipoMovimiento == tipo,
                        onClick = { tipoMovimiento = tipo },
                        label = { Text(tipo.label) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Campos comunes
            AppTextField(
                value = codigoBarras,
                onValueChange = {
                    codigoBarras = it.uppercase().trim()
                    codigoBarrasError = null
                },
                label = "Código de Barras",
                placeholder = "Ej: 12345678901234",
                leadingIcon = Icons.Default.QrCode,
                isError = codigoBarrasError != null,
                errorMessage = codigoBarrasError
            )

            Spacer(modifier = Modifier.height(8.dp))

            ScanSimpleButton(
                onCodeScanned = {
                    codigoBarras = it.trim()
                    codigoBarrasError = null
                },
                label = "Escanear Producto",
                isFullWidth = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = cantidad,
                onValueChange = { cantidad = it; cantidadError = null },
                label = { Text("Cantidad") },
                isError = cantidadError != null,
                supportingText = { if (cantidadError != null) Text(cantidadError!!) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            OutlinedTextField(
                value = motivo,
                onValueChange = { motivo = it; motivoError = null },
                label = { Text("Motivo") },
                isError = motivoError != null,
                supportingText = { if (motivoError != null) Text(motivoError!!) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            // Campos según tipo de movimiento
            when (tipoMovimiento) {
                TipoMovimiento.INGRESO -> {
                    Text(
                        text = "Ubicación de Destino",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    ScanUbicacionButton(
                        onUbicacionScanned = { codigo ->
                            ubicacionIngreso = codigo.trim()
                            ubicacionError = null
                        },
                        isFullWidth = true
                    )

                    OutlinedTextField(
                        value = ubicacionIngreso,
                        onValueChange = { ubicacionIngreso = it.trim(); ubicacionError = null },
                        label = { Text("Código de Ubicación") },
                        isError = ubicacionError != null,
                        supportingText = { if (ubicacionError != null) Text(ubicacionError!!) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp, bottom = 12.dp)
                    )
                }

                TipoMovimiento.EGRESO -> {
                    Text(
                        text = "Ubicación de Origen",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    ScanUbicacionButton(
                        onUbicacionScanned = { codigo ->
                            ubicacionEgreso = codigo.trim()
                            ubicacionError = null
                        },
                        isFullWidth = true
                    )

                    OutlinedTextField(
                        value = ubicacionEgreso,
                        onValueChange = { ubicacionEgreso = it.trim(); ubicacionError = null },
                        label = { Text("Código de Ubicación") },
                        isError = ubicacionError != null,
                        supportingText = { if (ubicacionError != null) Text(ubicacionError!!) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp, bottom = 12.dp)
                    )
                }

                TipoMovimiento.REUBICACION -> {
                    Text(
                        text = "Ubicación de Origen",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    ScanUbicacionButton(
                        onUbicacionScanned = { codigo ->
                            idUbicacionOrigen = codigo.trim()
                            ubicacionError = null
                        },
                        isFullWidth = true
                    )

                    OutlinedTextField(
                        value = idUbicacionOrigen,
                        onValueChange = { idUbicacionOrigen = it.trim(); ubicacionError = null },
                        label = { Text("Código de Ubicación") },
                        isError = ubicacionError != null,
                        supportingText = { if (ubicacionError != null) Text(ubicacionError!!) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp, bottom = 12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Ubicación de Destino",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    ScanUbicacionButton(
                        onUbicacionScanned = { codigo ->
                            idUbicacionDestino = codigo.trim()
                            ubicacionError = null
                        },
                        isFullWidth = true
                    )

                    OutlinedTextField(
                        value = idUbicacionDestino,
                        onValueChange = { idUbicacionDestino = it.trim(); ubicacionError = null },
                        label = { Text("Código de Ubicación") },
                        isError = ubicacionError != null,
                        supportingText = { if (ubicacionError != null) Text(ubicacionError!!) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp, bottom = 12.dp)
                    )
                }

                else -> {}
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón de envío
            Button(
                onClick = { showConfirmDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = registroState !is UiState.Loading && tipoMovimiento != null
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("Registrar Movimiento")
            }

            // Indicador de carga
            if (registroState is UiState.Loading) {
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

private fun validarFormulario(
    tipoMovimiento: TipoMovimiento?,
    codigoBarras: String,
    cantidad: String,
    motivo: String,
    idUbicacionOrigen: String,
    idUbicacionDestino: String,
    ubicacionIngreso: String,
    ubicacionEgreso: String,
    onCodigoBarrasError: (String?) -> Unit,
    onCantidadError: (String?) -> Unit,
    onMotivoError: (String?) -> Unit,
    onUbicacionError: (String?) -> Unit
): Boolean {
    var isValid = true

    // Validar Código de Barras
    if (codigoBarras.isBlank()) {
        onCodigoBarrasError("Código de barras es requerido")
        isValid = false
    } else {
        onCodigoBarrasError(null)
    }

    // Validar cantidad
    if (cantidad.isBlank()) {
        onCantidadError("Cantidad es requerida")
        isValid = false
    } else if (cantidad.toIntOrNull() == null || cantidad.toInt() <= 0) {
        onCantidadError("Cantidad debe ser un número positivo")
        isValid = false
    } else {
        onCantidadError(null)
    }

    // Validar motivo
    if (motivo.isBlank()) {
        onMotivoError("Motivo es requerido")
        isValid = false
    } else {
        onMotivoError(null)
    }

    // Validar ubicaciones según tipo
    when (tipoMovimiento) {
        TipoMovimiento.INGRESO -> {
            if (ubicacionIngreso.isBlank()) {
                onUbicacionError("Ubicación de destino es requerida")
                isValid = false
            } else {
                onUbicacionError(null)
            }
        }
        TipoMovimiento.EGRESO -> {
            if (ubicacionEgreso.isBlank()) {
                onUbicacionError("Ubicación de origen es requerida")
                isValid = false
            } else {
                onUbicacionError(null)
            }
        }
        TipoMovimiento.REUBICACION -> {
            if (idUbicacionOrigen.isBlank() || idUbicacionDestino.isBlank()) {
                onUbicacionError("Ambas ubicaciones son requeridas")
                isValid = false
            } else {
                onUbicacionError(null)
            }
        }
        else -> {}
    }

    return isValid
}


