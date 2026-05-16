package com.smartlogix.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartlogix.app.models.TipoMovimiento
import com.smartlogix.app.models.UiState
import com.smartlogix.app.ui.screen.componentes.*
import com.smartlogix.app.viewmodels.AprobacionViewModel

/**
 * Pantalla de Solicitud de Movimiento
 * Solo para OPERADORES
 *
 * Tipos de movimiento:
 * - INGRESO: Nueva entrada de productos
 * - EGRESO: Salida de productos
 * - REUBICACION: Cambio de ubicación
 *
 * Todas las solicitudes requieren aprobación del Jefe/Supervisor
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitudMovimientoScreen(
    aprobacionViewModel: AprobacionViewModel,
    productoViewModel: com.smartlogix.app.viewmodels.ProductoViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Estados
    val createState by aprobacionViewModel.createSolicitudState.collectAsStateWithLifecycle()
    val selectedProducto by productoViewModel.selectedProducto.collectAsStateWithLifecycle()
    val searchState by productoViewModel.searchState.collectAsStateWithLifecycle()

    // Estados del formulario
    var tipoMovimiento by remember { mutableStateOf<TipoMovimiento?>(null) }
    var codigoBarras by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var motivo by remember { mutableStateOf("") }
    var idUbicacionOrigen by remember { mutableStateOf("") }
    var idUbicacionDestino by remember { mutableStateOf("") }
    var ubicacionIngreso by remember { mutableStateOf("") }
    var ubicacionEgreso by remember { mutableStateOf("") }

    // Estados de validación
    var codigoBarrasError by remember { mutableStateOf<String?>(null) }
    var cantidadError by remember { mutableStateOf<String?>(null) }
    var motivoError by remember { mutableStateOf<String?>(null) }
    var ubicacionError by remember { mutableStateOf<String?>(null) }

    // Estados de UI
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Limpiar producto seleccionado al salir/entrar
    LaunchedEffect(Unit) {
        productoViewModel.clearSelectedProducto()
        productoViewModel.clearSearch()
    }
    
    // Manejar estados de creación
    LaunchedEffect(createState) {
        when (val state = createState) {
            is UiState.Success -> {
                showSuccessDialog = true
                // Limpiar formulario y producto
                tipoMovimiento = null
                codigoBarras = ""
                cantidad = ""
                motivo = ""
                idUbicacionOrigen = ""
                idUbicacionDestino = ""
                ubicacionIngreso = ""
                ubicacionEgreso = ""
                productoViewModel.clearSelectedProducto()
            }
            is UiState.Error -> {
                errorMessage = state.message
                showErrorDialog = true
            }
            else -> {}
        }
    }

    // Diálogos
    SuccessDialog(
        title = "¡Solicitud Enviada!",
        message = "Tu solicitud ha sido enviada al Jefe de Bodega para su aprobación.",
        onDismiss = {
            showSuccessDialog = false
            aprobacionViewModel.clearCreateState()
            onNavigateBack()
        },
        showDialog = showSuccessDialog
    )

    ErrorDialog(
        message = errorMessage,
        onDismiss = {
            showErrorDialog = false
            aprobacionViewModel.clearCreateState()
        },
        showDialog = showErrorDialog
    )

    Scaffold(
        topBar = {
            BackTopBar(
                title = "Solicitar Movimiento",
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
            // ========== SELECCIÓN DE TIPO DE MOVIMIENTO ==========
            Text(
                text = "Tipo de Movimiento",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Ingreso
                FilterChip(
                    text = "Ingreso",
                    selected = tipoMovimiento == TipoMovimiento.INGRESO,
                    onClick = { tipoMovimiento = TipoMovimiento.INGRESO },
                    modifier = Modifier.weight(1f)
                )

                // Egreso
                FilterChip(
                    text = "Egreso",
                    selected = tipoMovimiento == TipoMovimiento.EGRESO,
                    onClick = { tipoMovimiento = TipoMovimiento.EGRESO },
                    modifier = Modifier.weight(1f)
                )

                // Reubicación
                FilterChip(
                    text = "Reubicación",
                    selected = tipoMovimiento == TipoMovimiento.REUBICACION,
                    onClick = { tipoMovimiento = TipoMovimiento.REUBICACION },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ========== FORMULARIO ==========
            if (tipoMovimiento != null) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Datos del Movimiento",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Campo CodigoBarras con verificación
                        AppTextField(
                            value = codigoBarras,
                            onValueChange = {
                                codigoBarras = it.uppercase().trim()
                                codigoBarrasError = null
                                // Resetear producto si cambia el código manual
                                if (selectedProducto?.sku != it.trim() && selectedProducto?.codigoBarrasIndividual != it.trim()) {
                                    productoViewModel.clearSelectedProducto()
                                }
                            },
                            label = "Código de Barras / SKU",
                            placeholder = "Ej: 12345678901234",
                            leadingIcon = Icons.Default.QrCode,
                            trailingIcon = {
                                IconButton(onClick = { 
                                    if (codigoBarras.isNotBlank()) {
                                        productoViewModel.searchByBarcode(codigoBarras) 
                                    }
                                }) {
                                    Icon(Icons.Default.Search, contentDescription = "Verificar")
                                }
                            },
                            isError = codigoBarrasError != null,
                            errorMessage = codigoBarrasError
                        )

                        // Mostrar producto verificado
                        if (selectedProducto != null) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                ),
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        text = "Producto Verificado:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                    Text(
                                        text = selectedProducto!!.descripcion,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                    Text(
                                        text = "SKU: ${selectedProducto!!.sku}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                        } else if (searchState is UiState.Error) {
                             Text(
                                text = (searchState as UiState.Error).message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp, start = 8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        ScanSimpleButton(
                            onCodeScanned = {
                                codigoBarras = it
                                codigoBarrasError = null
                                productoViewModel.searchByBarcode(it)
                            },
                            label = "Escanear Producto",
                            isFullWidth = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Campo Cantidad
                        NumberTextField(
                            value = cantidad,
                            onValueChange = {
                                cantidad = it
                                cantidadError = null
                            },
                            label = "Cantidad",
                            placeholder = "0",
                            isError = cantidadError != null,
                            errorMessage = cantidadError,
                            leadingIcon = Icons.Default.Numbers
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // ========== REUBICACIÓN ==========
                        if (tipoMovimiento == TipoMovimiento.REUBICACION) {
                            Text(
                                text = "Ubicaciones",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            // Ubicación Origen
                            OutlinedTextField(
                                value = idUbicacionOrigen,
                                onValueChange = {
                                    idUbicacionOrigen = it
                                    ubicacionError = null
                                },
                                label = { Text("Ubicación Origen") },
                                placeholder = { Text("Ej: P1-A-01") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                isError = ubicacionError != null
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            ScanUbicacionButton(
                                onUbicacionScanned = { ubicacion ->
                                    idUbicacionOrigen = ubicacion
                                    ubicacionError = null
                                },
                                label = "Escanear Ubicación Origen",
                                isFullWidth = true
                            )

                            if (ubicacionError != null) {
                                Text(
                                    text = ubicacionError!!,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
                                )
                            }

                            // Ubicación Destino
                            OutlinedTextField(
                                value = idUbicacionDestino,
                                onValueChange = {
                                    idUbicacionDestino = it
                                    ubicacionError = null
                                },
                                label = { Text("Ubicación Destino") },
                                placeholder = { Text("Ej: P2-B-05") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                isError = ubicacionError != null
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            ScanUbicacionButton(
                                onUbicacionScanned = { ubicacion ->
                                    idUbicacionDestino = ubicacion
                                    ubicacionError = null
                                },
                                label = "Escanear Ubicación Destino",
                                isFullWidth = true
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // ========== INGRESO ==========
                        if (tipoMovimiento == TipoMovimiento.INGRESO) {
                            Text(
                                text = "Ubicación de Ingreso",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            OutlinedTextField(
                                value = ubicacionIngreso,
                                onValueChange = {
                                    ubicacionIngreso = it.trim()
                                    ubicacionError = null
                                },
                                label = { Text("Ubicación") },
                                placeholder = { Text("Ej: P1-A-01") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                isError = ubicacionError != null
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            ScanUbicacionButton(
                                onUbicacionScanned = { ubicacion ->
                                    ubicacionIngreso = ubicacion.trim()
                                    ubicacionError = null
                                },
                                label = "Escanear Ubicación de Ingreso",
                                isFullWidth = true
                            )

                            if (ubicacionError != null) {
                                Text(
                                    text = ubicacionError!!,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // ========== EGRESO ==========
                        if (tipoMovimiento == TipoMovimiento.EGRESO) {
                            Text(
                                text = "Ubicación de Egreso",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            OutlinedTextField(
                                value = ubicacionEgreso,
                                onValueChange = {
                                    ubicacionEgreso = it.trim()
                                    ubicacionError = null
                                },
                                label = { Text("Ubicación") },
                                placeholder = { Text("Ej: P1-A-01") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                isError = ubicacionError != null
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            ScanUbicacionButton(
                                onUbicacionScanned = { ubicacion ->
                                    ubicacionEgreso = ubicacion.trim()
                                    ubicacionError = null
                                },
                                label = "Escanear Ubicación de Egreso",
                                isFullWidth = true
                            )

                            if (ubicacionError != null) {
                                Text(
                                    text = ubicacionError!!,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Campo Motivo
                        MultilineTextField(
                            value = motivo,
                            onValueChange = {
                                motivo = it
                                motivoError = null
                            },
                            label = "Motivo de la Solicitud",
                            placeholder = "Describe el motivo del movimiento...",
                            minLines = 3,
                            maxLines = 5
                        )

                        if (motivoError != null) {
                            Text(
                                text = motivoError!!,
                                color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ========== INFORMACIÓN ==========
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Tu solicitud será enviada al Jefe de Bodega para su revisión y aprobación.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ========== BOTÓN ENVIAR ==========
                PrimaryButton(
                    text = "Enviar Solicitud",
                    onClick = {
                        // Validar formulario
                        var isValid = true

                        if (codigoBarras.isBlank()) {
                            codigoBarrasError = "El código de barras es obligatorio"
                            isValid = false
                        }

                        if (cantidad.isBlank() || cantidad.toIntOrNull() == null || cantidad.toInt() <= 0) {
                            cantidadError = "Ingresa una cantidad válida"
                            isValid = false
                        }

                        if (motivo.isBlank()) {
                            motivoError = "El motivo es obligatorio"
                            isValid = false
                        }

                        when (tipoMovimiento) {
                            TipoMovimiento.REUBICACION -> {
                                if (idUbicacionOrigen.isBlank() || idUbicacionDestino.isBlank()) {
                                    ubicacionError = "Las ubicaciones son obligatorias"
                                    isValid = false
                                }
                            }
                            TipoMovimiento.INGRESO -> {
                                if (ubicacionIngreso.isBlank()) {
                                    ubicacionError = "La ubicación de ingreso es obligatoria"
                                    isValid = false
                                }
                            }
                            TipoMovimiento.EGRESO -> {
                                if (ubicacionEgreso.isBlank()) {
                                    ubicacionError = "La ubicación de egreso es obligatoria"
                                    isValid = false
                                }
                            }
                            else -> {}
                        }

                        // Si es válido, enviar solicitud
                        if (isValid) {
                            // Usar el SKU verificado si existe, sino el código ingresado (y que el backend decida)
                            val skuParaEnviar = selectedProducto?.sku ?: codigoBarras
                            
                            when (tipoMovimiento) {
                                TipoMovimiento.INGRESO -> {
                                    aprobacionViewModel.solicitarIngreso(
                                        codigoBarras = skuParaEnviar,
                                        cantidad = cantidad.toInt(),
                                        ubicacionDestino = ubicacionIngreso,
                                        motivo = motivo
                                    )
                                }
                                TipoMovimiento.EGRESO -> {
                                    aprobacionViewModel.solicitarEgreso(
                                        codigoBarras = skuParaEnviar,
                                        cantidad = cantidad.toInt(),
                                        ubicacionOrigen = ubicacionEgreso,
                                        motivo = motivo
                                    )
                                }
                                TipoMovimiento.REUBICACION -> {
                                    aprobacionViewModel.solicitarReubicacion(
                                        codigoBarras = skuParaEnviar,
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
                    enabled = createState !is UiState.Loading,
                    icon = Icons.Default.Send
                )

                // Indicador de carga
                if (createState is UiState.Loading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}


