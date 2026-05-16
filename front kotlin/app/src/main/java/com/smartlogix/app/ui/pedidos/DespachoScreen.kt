package com.smartlogix.app.ui.pedidos

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartlogix.app.model.EstadoPedido
import com.smartlogix.app.models.UiState
import com.smartlogix.app.ui.screen.componentes.AppTopBar
import com.smartlogix.app.ui.screen.componentes.CameraCapture
import com.smartlogix.app.viewmodels.PedidosViewModel
import java.io.File

@Composable
fun DespachoScreen(
    pedidoId: Int,
    pedidosViewModel: PedidosViewModel,
    onNavigateBack: () -> Unit,
    onDeliveryFinished: () -> Unit
) {
    val uiState by pedidosViewModel.pedidoDetalleState.collectAsStateWithLifecycle()
    val actionState by pedidosViewModel.actionState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showCamera by remember { mutableStateOf(false) }
    var customSuccessMessage by remember { mutableStateOf<String?>(null) }

    val app = context.applicationContext as com.smartlogix.app.SmartLogixApplication
    val currentUserId = app.getCurrentUserId()
    val userRole = app.getCurrentUserRole()
    val isJefe = userRole == "JEFE" || userRole == "SUPERVISOR" || userRole == "ADMIN"

    LaunchedEffect(pedidoId) {
        pedidosViewModel.getPedidoDetalle(pedidoId)
    }

    LaunchedEffect(actionState) {
        if (actionState is UiState.Success) {
            val message = customSuccessMessage ?: "Operación exitosa"
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            customSuccessMessage = null
            pedidosViewModel.resetActionState()
            if (uiState is UiState.Success && (uiState as UiState.Success).data.estado == EstadoPedido.ENTREGADO) {
                onDeliveryFinished()
            }
        } else if (actionState is UiState.Error) {
             Toast.makeText(context, (actionState as UiState.Error).message, Toast.LENGTH_LONG).show()
             pedidosViewModel.resetActionState()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Despacho Pedido #$pedidoId",
                onMenuClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is UiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is UiState.Error -> Text("Error: ${state.message}", modifier = Modifier.align(Alignment.Center))
                is UiState.Success -> {
                    val pedido = state.data
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Consideramos 0 o null como "no asignado"
                        val efectivoOperadorId = if ((pedido.operadorId ?: 0) <= 0) null else pedido.operadorId
                        val isMyAssignment = efectivoOperadorId == currentUserId
                        val isAssignedToOther = efectivoOperadorId != null && efectivoOperadorId != currentUserId

                        // Info Pedido
                        Text("Cliente: ${pedido.cliente}", style = MaterialTheme.typography.titleLarge)
                        
                        // Barra de Progreso (Progress Stepper)
                        if (pedido.estado != null && pedido.estado >= EstadoPedido.REVISION_FACTURA) {
                            DeliveryStepper(currentStatus = pedido.estado)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Spacer(modifier = Modifier.height(16.dp))

                        // Checklist de Items para Validación (Solo en estados de revisión)
                        if (pedido.estado == EstadoPedido.PICKING_COMPLETADO) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        "El picking ha finalizado. Por favor, genere la factura para habilitar la revisión final.",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }

                        if (pedido.estado == EstadoPedido.FACTURADO) {
                            Text("Validación de Items", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            
                            val itemsChecked = remember { mutableStateMapOf<String, Boolean>() }
                            val allChecked = pedido.detalles?.all { itemsChecked[it.sku] == true } ?: false

                            val userRole = (LocalContext.current.applicationContext as com.smartlogix.app.SmartLogixApplication).getCurrentUserRole()
                            val isJefe = userRole == "JEFE" || userRole == "SUPERVISOR" || userRole == "ADMIN"

                            if (isJefe) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        pedido.detalles?.forEach { item ->
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                            ) {
                                                Checkbox(
                                                    checked = itemsChecked[item.sku] ?: false,
                                                    onCheckedChange = { itemsChecked[item.sku] = it }
                                                )
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(item.descripcion ?: item.sku, style = MaterialTheme.typography.bodyMedium)
                                                    Text("Cant: ${item.cantidadPickeada}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = { pedidosViewModel.revisarPedido(pedido.id) {} },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    enabled = allChecked,
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("CONFIRMAR REVISIÓN Y EGRESO")
                                }
                            } else {
                                // Si es operador pero está en estos estados, solo puede esperar
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
                                ) {
                                    Text(
                                        "Esperando validación del Jefe de Bodega",
                                        modifier = Modifier.padding(16.dp),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }

                        // Flujo según estado (Resto de estados)
                        when (pedido.estado) {
                            EstadoPedido.PICKING_COMPLETADO -> {
                                if (pedido.detalles.isNullOrEmpty()) {
                                    Text("Esperando detalles del pedido...")
                                }
                            }

                            EstadoPedido.FACTURADO -> {
                                // El checklist ya se muestra arriba
                            }
                            
                            EstadoPedido.REVISION_FACTURA -> {
                                Text("Pedido revisado. Listo para asignar transporte.", style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(
                                    onClick = { 
                                        customSuccessMessage = "te haz asignado el transporte del pedido de ${pedido.cliente}"
                                        pedidosViewModel.asignarTransporte(pedido.id)
                                    },
                                    enabled = !isAssignedToOther || isJefe,
                                    modifier = Modifier.fillMaxWidth().height(56.dp)
                                ) {
                                    Icon(Icons.Default.LocalShipping, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(if (isAssignedToOther && !isJefe) "ASIGNADO A: ${pedido.operadorNombre}" else "ASIGNARME TRANSPORTE")
                                }
                            }
                            
                            EstadoPedido.TRANSPORTE_ASIGNADO -> {
                                Text("Transporte asignado. Listo para iniciar ruta.", style = MaterialTheme.typography.bodyLarge)
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(
                                    onClick = { 
                                        pedidosViewModel.iniciarTransporte(pedido.id)
                                    },
                                    enabled = !isAssignedToOther || isJefe,
                                    modifier = Modifier.fillMaxWidth().height(56.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        when {
                                            isMyAssignment || isJefe -> "INICIAR TRANSPORTE"
                                            isAssignedToOther -> "ASIGNADO A: ${pedido.operadorNombre ?: "OTRA PERSONA"}"
                                            else -> "INICIAR TRANSPORTE"
                                        }
                                    )
                                }
                            }

                            EstadoPedido.EN_TRANSPORTE -> {
                                Text("Pedido en ruta. Al entregar, capture la factura firmada.", style = MaterialTheme.typography.bodyLarge)
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(
                                    onClick = { showCamera = true },
                                    enabled = !isAssignedToOther || isJefe,
                                    modifier = Modifier.fillMaxWidth().height(56.dp)
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        when {
                                            isMyAssignment || isJefe -> "FINALIZAR ENTREGA (FOTO)"
                                            isAssignedToOther -> "ASIGNADO A: ${pedido.operadorNombre ?: "OTRA PERSONA"}"
                                            else -> "FINALIZAR ENTREGA (FOTO)"
                                        }
                                    )
                                }
                            }

                            EstadoPedido.ENTREGADO -> {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(64.dp))
                                    Text("Pedido Entregado Exitosamente", style = MaterialTheme.typography.titleMedium, color = Color(0xFF2E7D32))
                                }
                            }

                            else -> {
                                // No mostrar nada técnico aquí
                            }
                        }
                    }
                }
                UiState.Idle -> {}
            }

            if (showCamera) {
                Dialog(
                    onDismissRequest = { showCamera = false },
                    properties = DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    CameraCapture(
                        onImageCaptured = { file ->
                            showCamera = false
                            // Enviar foto
                            pedidosViewModel.finalizarEntrega(pedidoId, file)
                        },
                        onError = {
                             Toast.makeText(context, "Error cámara: ${it.message}", Toast.LENGTH_SHORT).show()
                        },
                        onClose = { showCamera = false }
                    )
                }
            }
        }
    }
}

@Composable
fun DeliveryStepper(currentStatus: EstadoPedido) {
    val steps = listOf(
        "Listo" to EstadoPedido.REVISION_FACTURA,
        "Asignado" to EstadoPedido.TRANSPORTE_ASIGNADO,
        "En Ruta" to EstadoPedido.EN_TRANSPORTE,
        "Entregado" to EstadoPedido.ENTREGADO
    )

    val currentStepIndex = steps.indexOfFirst { it.second == currentStatus }.let { if (it == -1) steps.size else it }
    val isFinished = currentStatus == EstadoPedido.ENTREGADO

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            steps.forEachIndexed { index, pair ->
                val isActive = index <= currentStepIndex
                val isCurrent = index == currentStepIndex

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isActive && index < currentStepIndex || isFinished) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        } else if (isActive) {
                            Box(modifier = Modifier.size(8.dp).background(Color.White, CircleShape))
                        }
                    }
                    Text(
                        text = pair.first,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                if (index < steps.size - 1) {
                    Box(
                        modifier = Modifier
                            .weight(0.5f)
                            .height(2.dp)
                            .background(
                                if (index < currentStepIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            )
                            .offset(y = (-10).dp)
                    )
                }
            }
        }
    }
}


