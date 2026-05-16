package com.smartlogix.app.ui.pedidos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartlogix.app.SmartLogixApplication
import com.smartlogix.app.model.EstadoPedido
import com.smartlogix.app.model.PedidoResponse
import com.smartlogix.app.models.UiState
import com.smartlogix.app.ui.screen.componentes.AppTopBar
import com.smartlogix.app.viewmodels.PedidosViewModel

@Composable
fun ListaPedidosScreen(
    viewModel: PedidosViewModel,
    isMyOrders: Boolean = false,
    showDelivered: Boolean = false,
    isRevisionMode: Boolean = false,
    onNavigateToDetail: (PedidoResponse) -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.pedidosState.collectAsStateWithLifecycle()
    val detailState by viewModel.pedidoDetalleState.collectAsStateWithLifecycle()
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as com.smartlogix.app.SmartLogixApplication

    var selectedPhotoUrl by remember { mutableStateOf<String?>(null) }
    var pedidoARevisar by remember { mutableStateOf<PedidoResponse?>(null) }
    val itemsRevisados = remember { mutableStateMapOf<String, Boolean>() }

    // Resetear checklist al cerrar el diálogo
    LaunchedEffect(pedidoARevisar) {
        if (pedidoARevisar == null) {
            itemsRevisados.clear()
        }
    }

    LaunchedEffect(isMyOrders, showDelivered, isRevisionMode) {
        val userRole = app.getCurrentUserRole()
        if (isRevisionMode) {
            viewModel.getPedidosFacturados()
        } else if (showDelivered) {
            viewModel.getPedidosEntregados()
        } else if (isMyOrders) {
            val userId = app.getCurrentUserId()
            if (userId != -1) {
                viewModel.getPedidosEnPicking()
            }
        } else {
            // Usuarios finales (Operadores/Jefes) ven lista general
            // Usamos getPedidosTodos para que el operador vea también los "Revisados"
            viewModel.getPedidosTodos()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = when {
                    isRevisionMode -> "Revisar Pedidos"
                    showDelivered -> "Historial de Entregas"
                    isMyOrders -> "Mis Pedidos"
                    else -> "Notas de Venta"
                },
                onMenuClick = onNavigateBack,
                actions = {
                    val userRole = app.getCurrentUserRole()
                    IconButton(onClick = {
                        when {
                            isRevisionMode -> viewModel.getPedidosFacturados()
                            showDelivered -> viewModel.getPedidosEntregados()
                            isMyOrders -> viewModel.getPedidosEnPicking()
                            userRole == "OPERADOR" -> viewModel.getPedidosPendientes()
                            else -> viewModel.getPedidosTodos()
                        }
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is UiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is UiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                        Button(onClick = {
                            when {
                                isRevisionMode -> viewModel.getPedidosFacturados()
                                showDelivered -> viewModel.getPedidosEntregados()
                                isMyOrders -> viewModel.getPedidosEnPicking()
                                else -> viewModel.getPedidosTodos()
                            }
                        }) {
                            Text("Reintentar")
                        }
                    }
                }
                is UiState.Success -> {
                    val userRole = app.getCurrentUserRole()
                    val filteredData = if (userRole == "OPERADOR" && !showDelivered && !isRevisionMode && !isMyOrders) {
                        // El operador en "Notas de Venta" ve:
                        // 1. Pedidos nuevos o en picking (PENDIENTE hasta EN_PICKING)
                        // 2. Pedidos ya revisados por el jefe (REVISION_FACTURA en adelante)
                        // Excluimos ENTREGADO para no saturar la lista principal
                        state.data.filter { 
                            it.estado == null || 
                            it.estado <= EstadoPedido.EN_PICKING || 
                            (it.estado >= EstadoPedido.REVISION_FACTURA && it.estado < EstadoPedido.ENTREGADO)
                        }
                    } else {
                        state.data
                    }

                    if (filteredData.isEmpty()) {
                        Text(
                            "No hay pedidos disponibles",
                            modifier = Modifier.align(Alignment.Center),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                             items(filteredData) { pedido ->
                                PedidoCard(
                                    pedido = pedido,
                                    isMyOrders = isMyOrders,
                                    isRevisionMode = isRevisionMode,
                                    onClick = { 
                                        if (showDelivered || pedido.estado == EstadoPedido.ENTREGADO) {
                                            onNavigateToDetail(pedido)
                                        } else if (isRevisionMode) {
                                            pedidoARevisar = pedido
                                            viewModel.getPedidoDetalle(pedido.id)
                                        } else {
                                            onNavigateToDetail(pedido) 
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                UiState.Idle -> {}
            }

            // Diálogo para mostrar la foto
            selectedPhotoUrl?.let { photo ->
                AlertDialog(
                    onDismissRequest = { selectedPhotoUrl = null },
                    title = { Text("Evidencia de Entrega") },
                    text = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (photo.startsWith("data:image")) {
                                // Es una imagen base64
                                val base64Data = photo.substringAfter(",")
                                val imageBytes = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT)
                                val bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                if (bitmap != null) {
                                    androidx.compose.foundation.Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "Foto evidencia",
                                        modifier = Modifier.fillMaxWidth().height(300.dp)
                                    )
                                }
                            } else if (photo.isNotEmpty()) {
                                // Podría ser una URL o base64 sin prefijo
                                val bitmap = try {
                                    val imageBytes = android.util.Base64.decode(photo, android.util.Base64.DEFAULT)
                                    android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                } catch (e: Exception) {
                                    null
                                }
                                
                                if (bitmap != null) {
                                    androidx.compose.foundation.Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "Foto evidencia",
                                        modifier = Modifier.fillMaxWidth().height(300.dp)
                                    )
                                } else {
                                    Text("No se pudo cargar la imagen")
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { selectedPhotoUrl = null }) {
                            Text("Cerrar")
                        }
                    }
                )
            }

            // Diálogo de revisión de pedido con CHECKLIST
            pedidoARevisar?.let { pedido ->
                val fullPedido = if (detailState is UiState.Success && (detailState as UiState.Success).data.id == pedido.id) {
                    (detailState as UiState.Success).data
                } else {
                    pedido
                }
                
                val isLoadingDetail = detailState is UiState.Loading
                val totalItems = fullPedido.detalles?.size ?: 0
                val revisadosCount = itemsRevisados.values.count { it }
                val allChecked = totalItems > 0 && revisadosCount == totalItems

                AlertDialog(
                    onDismissRequest = { pedidoARevisar = null },
                    title = { 
                        Column {
                            Text("Revisar Pedido #${pedido.id}")
                            Text(
                                text = "Verifique cada item antes de confirmar",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    text = { 
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Contador de progreso
                            LinearProgressIndicator(
                                progress = if (totalItems > 0) revisadosCount.toFloat() / totalItems else 0f,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                color = if (allChecked) Color(0xFF43A047) else MaterialTheme.colorScheme.primary
                            )

                            if (isLoadingDetail) {
                                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            } else if (fullPedido.detalles.isNullOrEmpty()) {
                                Text("Este pedido no tiene items cargados.")
                            } else {
                                Box(modifier = Modifier.heightIn(max = 400.dp)) {
                                    LazyColumn(
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(fullPedido.detalles) { item ->
                                            val isChecked = itemsRevisados[item.sku] ?: false
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { itemsRevisados[item.sku] = !isChecked }
                                                    .padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Checkbox(
                                                    checked = isChecked,
                                                    onCheckedChange = { itemsRevisados[item.sku] = it }
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = item.descripcion ?: "Sin descripción",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = "SKU: ${item.sku} | Cant: ${item.cantidadSolicitada}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.revisarPedido(pedido.id) {
                                    pedidoARevisar = null
                                }
                            },
                            enabled = allChecked,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (allChecked) Color(0xFF43A047) else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Confirmar Revisión")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { pedidoARevisar = null }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun PedidoCard(
    pedido: PedidoResponse,
    isMyOrders: Boolean = false,
    isRevisionMode: Boolean = false,
    onClick: () -> Unit
) {
    val hasDeliveryPhoto = !pedido.fotoEntrega.isNullOrEmpty()
    val statusColor = when (pedido.estado) {
        EstadoPedido.PENDIENTE -> Color(0xFFE65100) // Naranja
        EstadoPedido.PICKING_ASIGNADO, EstadoPedido.EN_PICKING -> Color(0xFF1565C0) // Azul
        EstadoPedido.PICKING_COMPLETADO -> Color(0xFF00897B) // Teal
        EstadoPedido.FACTURADO -> Color(0xFF7B1FA2) // Púrpura
        EstadoPedido.REVISION_FACTURA -> Color(0xFF1976D2) // Azul fuerte
        EstadoPedido.TRANSPORTE_ASIGNADO -> Color(0xFFFBC02D) // Amarillo/Ambar
        EstadoPedido.EN_TRANSPORTE -> Color(0xFFF57C00) // Naranja fuerte
        EstadoPedido.ENTREGADO -> Color(0xFF43A047) // Verde
        else -> MaterialTheme.colorScheme.primary
    }

    val displayStatus = when (pedido.estado) {
        EstadoPedido.PENDIENTE -> "PENDIENTE"
        EstadoPedido.PICKING_ASIGNADO -> "EN PICKING"
        EstadoPedido.EN_PICKING -> "EN PICKING"
        EstadoPedido.PICKING_COMPLETADO -> "PICKING FINALIZADO"
        EstadoPedido.FACTURADO -> "FACTURADO"
        EstadoPedido.REVISION_FACTURA -> "REVISADO"
        EstadoPedido.TRANSPORTE_ASIGNADO -> "TRANSPORTE ASIGNADO"
        EstadoPedido.EN_TRANSPORTE -> "EN RUTA"
        EstadoPedido.ENTREGADO -> "ENTREGADO"
        null -> "DESCONOCIDO"
        else -> pedido.estado.name
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Nota de Venta #${pedido.id}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface // USA AZUL DEL TEMA
                )
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = displayStatus,
                        color = statusColor,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Cliente: ${pedido.cliente}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant // USA GRIS DEL TEMA
            )
            if (!isMyOrders && !pedido.operadorNombre.isNullOrEmpty()) {
                Text(
                    text = "Asignado a: ${pedido.operadorNombre}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF1565C0),
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = "Items: ${pedido.totalItems ?: 0}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Fecha: ${formatDate(pedido.fechaCreacion)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (pedido.estado == EstadoPedido.ENTREGADO && pedido.fechaEntrega != null) {
                Text(
                    text = "Entregado: ${formatDate(pedido.fechaEntrega)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF43A047),
                    fontWeight = FontWeight.Bold
                )
            }
            if (!pedido.fotoEntrega.isNullOrEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CheckCircle, 
                        contentDescription = null, 
                        tint = Color(0xFF43A047),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Ver Evidencia",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            if (isRevisionMode && pedido.estado == EstadoPedido.FACTURADO) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.FactCheck, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Revisar y Completar")
                }
            }
        }
    }
}

/**
 * Formatea una fecha ISO (yyyy-MM-ddTHH:mm:ss...) a HH:mm dd/MM/yyyy
 */
private fun formatDate(isoString: String): String {
    return try {
        val parts = isoString.split("T")
        val datePart = parts[0] // yyyy-MM-dd
        val timePart = parts[1].substring(0, 5) // HH:mm
        
        val dateComponents = datePart.split("-")
        val year = dateComponents[0]
        val month = dateComponents[1]
        val day = dateComponents[2]
        
        "$timePart $day/$month/$year"
    } catch (e: Exception) {
        isoString
    }
}


