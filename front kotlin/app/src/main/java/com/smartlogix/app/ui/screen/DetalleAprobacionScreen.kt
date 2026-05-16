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
import com.smartlogix.app.models.EstadoAprobacion
import com.smartlogix.app.models.Rol
import com.smartlogix.app.models.TipoMovimiento
import com.smartlogix.app.models.UiState
import com.smartlogix.app.ui.screen.componentes.*
import com.smartlogix.app.viewmodels.AprobacionViewModel
import com.smartlogix.app.viewmodels.AuthViewModel

/**
 * Pantalla de Detalle de Aprobación
 * Solo para JEFE y SUPERVISOR
 *
 * Permite:
 * - Ver información completa de la solicitud
 * - Aprobar con observaciones opcionales
 * - Rechazar con observaciones obligatorias
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleAprobacionScreen(
    aprobacionId: Int,
    aprobacionViewModel: AprobacionViewModel,
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Estados
    val aprobacionDetailState by aprobacionViewModel.aprobacionDetailState.collectAsStateWithLifecycle()
    val selectedAprobacion by aprobacionViewModel.selectedAprobacion.collectAsStateWithLifecycle()
    val respuestaState by aprobacionViewModel.respuestaState.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    
    // Verificar si el usuario puede aprobar/rechazar (solo JEFE y SUPERVISOR)
    val canApprove = currentUser?.rol == Rol.JEFE || currentUser?.rol == Rol.SUPERVISOR

    // Estados de UI
    var showAprobarDialog by remember { mutableStateOf(false) }
    var showRechazarDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }

    // Cargar detalle al iniciar
    LaunchedEffect(aprobacionId) {
        aprobacionViewModel.getAprobacionDetail(aprobacionId)
    }

    // Manejar respuesta
    LaunchedEffect(respuestaState) {
        when (val state = respuestaState) {
            is UiState.Success -> {
                showSuccessDialog = true
            }
            is UiState.Error -> {
                // El error se muestra en un diálogo o snackbar
            }
            else -> {}
        }
    }

    // Diálogo para aprobar
    MotivoDialog(
        title = "Aprobar Solicitud",
        label = "Observaciones",
        onConfirm = { observaciones ->
            aprobacionViewModel.aprobarSolicitud(aprobacionId, observaciones)
            successMessage = "Solicitud aprobada exitosamente"
            showAprobarDialog = false
        },
        onDismiss = { showAprobarDialog = false },
        showDialog = showAprobarDialog,
        required = false
    )

    // Diálogo para rechazar
    MotivoDialog(
        title = "Rechazar Solicitud",
        label = "Motivo del rechazo",
        onConfirm = { motivo ->
            aprobacionViewModel.rechazarSolicitud(aprobacionId, motivo)
            successMessage = "Solicitud rechazada"
            showRechazarDialog = false
        },
        onDismiss = { showRechazarDialog = false },
        showDialog = showRechazarDialog,
        required = true
    )

    // Diálogo de éxito
    SuccessDialog(
        message = successMessage,
        onDismiss = {
            showSuccessDialog = false
            aprobacionViewModel.clearRespuestaState()
            onNavigateBack()
        },
        showDialog = showSuccessDialog
    )

    // Diálogo de error
    if (respuestaState is UiState.Error) {
        val error = (respuestaState as UiState.Error).message
        ErrorDialog(
            message = error,
            showDialog = true,
            onDismiss = {
                aprobacionViewModel.clearRespuestaState()
            }
        )
    }

    Scaffold(
        topBar = {
            BackTopBar(
                title = "Detalle de Solicitud",
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = aprobacionDetailState) {
                is UiState.Loading -> {
                    LoadingState(message = "Cargando solicitud...")
                }

                is UiState.Success -> {
                    val aprobacion = state.data

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        // ========== ESTADO Y TIPO ==========
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = when (aprobacion.tipoMovimiento) {
                                            TipoMovimiento.INGRESO -> Icons.Default.ArrowDownward
                                            TipoMovimiento.EGRESO -> Icons.Default.ArrowUpward
                                            TipoMovimiento.REUBICACION -> Icons.Default.SwapHoriz
                                        },
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = aprobacion.tipoMovimiento.name,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            EstadoBadge(estado = aprobacion.estado)
                        }

                        // ========== INFORMACIÓN DEL PRODUCTO ==========
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Información del Producto",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                DetailRow(label = "Código de Barras", value = aprobacion.codigoBarras)
                                DetailRow(label = "Cantidad", value = aprobacion.cantidad.toString())
                            }
                        }

                        // ========== UBICACIONES ==========
                        val mostrarOrigen = aprobacion.tipoMovimiento == TipoMovimiento.REUBICACION || aprobacion.tipoMovimiento == TipoMovimiento.EGRESO
                        val mostrarDestino = aprobacion.tipoMovimiento == TipoMovimiento.REUBICACION || aprobacion.tipoMovimiento == TipoMovimiento.INGRESO

                        if (mostrarOrigen || mostrarDestino) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = "Ubicaciones",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )

                                    if (mostrarOrigen) {
                                        DetailRow(
                                            label = "Origen (Desde)",
                                            value = aprobacion.ubicacionOrigen ?: "N/A"
                                        )
                                    }
                                    
                                    if (mostrarDestino) {
                                        DetailRow(
                                            label = "Destino (Hacia)",
                                            value = aprobacion.ubicacionDestino ?: "N/A"
                                        )
                                    }
                                }
                            }
                        }

                        // ========== MOTIVO ==========
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Motivo",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                Text(
                                    text = aprobacion.motivo,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        // ========== INFORMACIÓN DEL SOLICITANTE ==========
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Solicitante",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                DetailRow(label = "Nombre", value = aprobacion.solicitante)
                                DetailRow(
                                    label = "Fecha",
                                    value = aprobacion.fechaSolicitud
                                )
                            }
                        }

                        // ========== RESPUESTA (SI YA FUE APROBADO/RECHAZADO) ==========
                        if (aprobacion.estado != EstadoAprobacion.PENDIENTE) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (aprobacion.estado == EstadoAprobacion.APROBADO)
                                        MaterialTheme.colorScheme.surfaceVariant
                                    else
                                        MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = "Respuesta",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )

                                    DetailRow(
                                        label = "Aprobador",
                                        value = aprobacion.aprobador ?: "N/A"
                                    )
                                    DetailRow(
                                        label = "Fecha",
                                        value = aprobacion.fechaRespuesta ?: "N/A"
                                    )

                                    if (aprobacion.observaciones != null) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Observaciones:",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = aprobacion.observaciones,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // ========== BOTONES DE ACCIÓN ==========
                        // Solo mostrar botones si el usuario es JEFE o SUPERVISOR
                        if (aprobacion.estado == EstadoAprobacion.PENDIENTE && canApprove) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Botón Rechazar
                                RejectButton(
                                    onClick = { showRechazarDialog = true },
                                    modifier = Modifier.weight(1f),
                                    enabled = respuestaState !is UiState.Loading
                                )

                                // Botón Aprobar
                                ApproveButton(
                                    onClick = { showAprobarDialog = true },
                                    modifier = Modifier.weight(1f),
                                    enabled = respuestaState !is UiState.Loading
                                )
                            }

                            if (respuestaState is UiState.Loading) {
                                Spacer(modifier = Modifier.height(16.dp))
                                LinearProgressIndicator(
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                is UiState.Error -> {
                    ErrorState(
                        message = state.message,
                        onRetry = {
                            aprobacionViewModel.getAprobacionDetail(aprobacionId)
                        }
                    )
                }

                is UiState.Idle -> {}
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
    }
}

