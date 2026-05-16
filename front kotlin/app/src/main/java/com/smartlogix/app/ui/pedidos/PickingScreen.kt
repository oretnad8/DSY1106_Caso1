package com.smartlogix.app.ui.pedidos

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartlogix.app.model.DetallePedido
import com.smartlogix.app.model.EstadoPedido
import com.smartlogix.app.model.PedidoResponse
import com.smartlogix.app.models.UiState
import com.smartlogix.app.ui.screen.componentes.AppTopBar
import com.smartlogix.app.ui.screen.componentes.BarcodeScanner
import com.smartlogix.app.viewmodels.PedidosViewModel

@Composable
fun PickingScreen(
    pedidoId: Int,
    pedidosViewModel: PedidosViewModel,
    productoViewModel: com.smartlogix.app.viewmodels.ProductoViewModel, // AGREGADO
    onNavigateBack: () -> Unit,
    onPickingFinished: () -> Unit
) {
    val uiState by pedidosViewModel.pedidoDetalleState.collectAsStateWithLifecycle()
    val actionState by pedidosViewModel.actionState.collectAsStateWithLifecycle()
    val productoState by productoViewModel.productoDetailState.collectAsStateWithLifecycle() // AGREGADO
    val context = LocalContext.current

    // Estado local para el flujo de escaneo
    var selectedItem by remember { mutableStateOf<DetallePedido?>(null) }
    var showScanner by remember { mutableStateOf(false) }
    var scanMode by remember { mutableStateOf<ScanMode>(ScanMode.NONE) }
    var scannedLocation by remember { mutableStateOf("") }
    var scannedProduct by remember { mutableStateOf("") }
    var manualQuantity by remember { mutableStateOf("") }

    // Manejo de errores/éxitos de acciones
    LaunchedEffect(actionState) {
        when (val state = actionState) {
            is UiState.Success -> {
                selectedItem = null // Cerrar bottom sheet
                pedidosViewModel.resetActionState()
                Toast.makeText(context, "Operación exitosa", Toast.LENGTH_SHORT).show()
            }
            is UiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                pedidosViewModel.resetActionState()
            }
            else -> {}
        }
    }

    LaunchedEffect(pedidoId) {
        pedidosViewModel.getPedidoDetalle(pedidoId)
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Picking NV #$pedidoId",
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
                    PickingContent(
                        pedido = pedido,
                        onAction = { handlePedidoAction(pedido, pedidosViewModel) },
                        onItemClick = { item ->
                            if (pedido.estado == EstadoPedido.EN_PICKING || pedido.estado == EstadoPedido.PICKING_ASIGNADO) {
                                selectedItem = item
                                scannedLocation = ""
                                scannedProduct = ""
                                manualQuantity = item.cantidadSolicitada.toString()
                                // Cargar detalle del producto para obtener codigoBarrasIndividual
                                productoViewModel.getProductoDetail(item.sku)
                            }
                        },
                        onFinishPicking = {
                             pedidosViewModel.completarPicking(pedido.id, onSuccess = onPickingFinished)
                        }
                    )

                    // Bottom Sheet para Picking de Item
                    if (selectedItem != null) {
                        val product = if (productoState is UiState.Success) (productoState as UiState.Success).data else null
                        val individualCode = product?.codigoBarrasIndividual

                        // Selección de ubicación recomendada: Priorizar Piso A
                        val suggestedLocation = selectedItem!!.ubicacionSugerida
                        val floorALocation = product?.ubicaciones?.find { it.codigoUbicacion.contains("-A-") || it.codigoUbicacion.contains("/A") }?.codigoUbicacion
                        val effectiveLocation = if (suggestedLocation?.contains("-A-") == true || suggestedLocation?.contains("/A") == true) {
                            suggestedLocation
                        } else {
                            floorALocation ?: suggestedLocation ?: ""
                        }

                        PickingItemSheet(
                            item = selectedItem!!,
                            scannedLocation = scannedLocation,
                            scannedProduct = scannedProduct,
                            manualQuantity = manualQuantity,
                            individualCode = individualCode,
                            recommendedLocation = effectiveLocation, // PASAR UBICACIÓN PRIORIZADA
                            onQuantityChange = { manualQuantity = it },
                            onScanLocation = {
                                scanMode = ScanMode.LOCATION
                                showScanner = true
                            },
                            onScanProduct = {
                                scanMode = ScanMode.PRODUCT
                                showScanner = true
                            },
                            onConfirm = {
                                pedidosViewModel.confirmarItem(
                                    pedido.id,
                                    selectedItem!!.sku,
                                    manualQuantity.toIntOrNull() ?: 0,
                                    scannedLocation // Se envía el código realmente escaneado
                                )
                            },
                            onDismiss = { selectedItem = null }
                        )
                    }
                }
                UiState.Idle -> {}
            }

            // Scanner Overlay
            if (showScanner) {
                Dialog(
                    onDismissRequest = { showScanner = false },
                    properties = DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    BarcodeScanner(
                         onBarcodeScanned = { code ->
                            showScanner = false
                            if (scanMode == ScanMode.LOCATION) {
                                val normalizedCode = com.smartlogix.app.models.UbicacionFormatter.parseScannedCode(code)
                                
                                // Volver a calcular la ubicación recomendada para validar
                                val product = if (productoState is UiState.Success) (productoState as UiState.Success).data else null
                                val suggestedLocation = selectedItem!!.ubicacionSugerida
                                val floorALocation = product?.ubicaciones?.find { it.codigoUbicacion.contains("-A-") || it.codigoUbicacion.contains("/A") }?.codigoUbicacion
                                val effectiveLocation = if (suggestedLocation?.contains("-A-") == true || suggestedLocation?.contains("/A") == true) {
                                    suggestedLocation
                                } else {
                                    floorALocation ?: suggestedLocation ?: ""
                                }

                                if (normalizedCode == effectiveLocation) {
                                    scannedLocation = normalizedCode ?: code
                                    Toast.makeText(context, "Ubicación Correcta ✅", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Ubicación incorrecta ❌\n(Escaneado: $code, Esperado: $effectiveLocation)", Toast.LENGTH_LONG).show()
                                }
                            } else if (scanMode == ScanMode.PRODUCT) {
                                val individualCode = if (productoState is UiState.Success) {
                                    (productoState as UiState.Success).data.codigoBarrasIndividual
                                } else null

                                // Validar contra código individual si existe, sino SKU
                                val targetCode = individualCode ?: selectedItem?.sku
                                
                                if (code == targetCode) {
                                    scannedProduct = code
                                    Toast.makeText(context, "Producto/Serie Correcto ✅", Toast.LENGTH_SHORT).show()
                                } else {
                                    if (code.contains(targetCode ?: "XYZ")) {
                                         scannedProduct = code
                                         Toast.makeText(context, "Producto Correcto (Parcial) ✅", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "El código no coincide ❌\n(Escaneado: $code)", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        },
                        onClose = { showScanner = false }
                    )
                }
            }
        }
    }
}

enum class ScanMode { NONE, LOCATION, PRODUCT }

@Composable
fun PickingContent(
    pedido: PedidoResponse,
    onAction: () -> Unit,
    onItemClick: (DetallePedido) -> Unit,
    onFinishPicking: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Cabecera Info
        Card(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = com.smartlogix.app.ui.theme.NavyDark
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Cliente: ${pedido.cliente}", 
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface, // AZUL VIBRANTE
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Estado: ${com.smartlogix.app.models.UbicacionFormatter.formatEstadoPedido(pedido.estado)}", 
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f), // AZUL VIBRANTE
                    fontWeight = FontWeight.Medium
                )
                if (pedido.estado == EstadoPedido.EN_PICKING || pedido.estado == EstadoPedido.PICKING_COMPLETADO) {
                     val progress = calculateProgress(pedido.detalles)
                     Spacer(modifier = Modifier.height(8.dp))
                     LinearProgressIndicator(
                         progress = progress,
                         modifier = Modifier.fillMaxWidth().height(8.dp)
                     )
                     Text("${(progress * 100).toInt()}% Completado", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // Lista de Productos
        if (pedido.detalles != null) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(pedido.detalles) { item ->
                    PickingItemCard(item = item, onClick = { onItemClick(item) })
                }
            }
        }

        // Acciones Principales
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (pedido.estado) {
                EstadoPedido.PENDIENTE -> {
                    val currentUserId = (LocalContext.current.applicationContext as com.smartlogix.app.SmartLogixApplication).getCurrentUserId()
                    val isAssignedToOther = pedido.operadorId != null && pedido.operadorId != currentUserId
                    
                    Button(
                        onClick = onAction,
                        enabled = !isAssignedToOther,
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Text(if (isAssignedToOther) "ASIGNADO A: ${pedido.operadorNombre}" else "TOMAR PEDIDO")
                    }
                }
                EstadoPedido.PICKING_ASIGNADO, EstadoPedido.EN_PICKING -> {
                     val isComplete = calculateProgress(pedido.detalles) >= 1.0f
                     Button(
                        onClick = onFinishPicking,
                        enabled = isComplete,
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Text("TERMINAR PICKING")
                    }
                }
                else -> {
                     Text(
                        "Estado: ${com.smartlogix.app.models.UbicacionFormatter.formatEstadoPedido(pedido.estado)}", 
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
fun PickingItemCard(
    item: DetallePedido,
    onClick: () -> Unit
) {
    val isPicked = item.cantidadPickeada >= item.cantidadSolicitada
    val cardColor = MaterialTheme.colorScheme.surface

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.sku, 
                    style = MaterialTheme.typography.titleSmall, 
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface // AZUL
                )
                Text(
                    item.descripcion ?: "", 
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant // GRIS
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Badge(containerColor = com.smartlogix.app.ui.theme.NavyDark) {
                        Text(
                            "Ubicacion detectada: ${com.smartlogix.app.models.UbicacionFormatter.getFriendlyLocationName(item.ubicacionSugerida)}", 
                            color = MaterialTheme.colorScheme.onSurface, // AZUL
                            modifier = Modifier.padding(horizontal = 4.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${item.cantidadPickeada} / ${item.cantidadSolicitada}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isPicked) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface
                )
                if (isPicked) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickingItemSheet(
    item: DetallePedido,
    scannedLocation: String,
    scannedProduct: String,
    manualQuantity: String,
    individualCode: String?,
    recommendedLocation: String, // AGREGADO
    onQuantityChange: (String) -> Unit,
    onScanLocation: () -> Unit,
    onScanProduct: () -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            Text("Confirmar Item", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            Text("1. Confirmar Ubicación", style = MaterialTheme.typography.titleSmall)
            val friendlyLocation = com.smartlogix.app.models.UbicacionFormatter.getFriendlyLocationName(recommendedLocation)
            
            OutlinedButton(
                onClick = onScanLocation,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (scannedLocation.isNotEmpty()) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent
                )
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (scannedLocation.isNotEmpty()) "Confirmado ✅" 
                    else "Escanear: $friendlyLocation"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("2. Validar Código Individual / Serie", style = MaterialTheme.typography.titleSmall)
             OutlinedButton(
                onClick = onScanProduct,
                modifier = Modifier.fillMaxWidth(),
                 colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (scannedProduct.isNotEmpty()) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent
                )
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                 Text(
                    if (scannedProduct.isNotEmpty()) "Serie: $scannedProduct ✅" 
                    else if (individualCode != null) "Escanear Serie: $individualCode"
                    else "Escanear Código / SKU"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("3. Cantidad Física", style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(
                value = manualQuantity,
                onValueChange = onQuantityChange,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                label = { Text("Cantidad") }
            )

            Spacer(modifier = Modifier.height(24.dp))

            val isLocationValid = scannedLocation == item.ubicacionSugerida
            // Validación laxa de producto para demo, idealmente estricta
            val isProductValid = scannedProduct.isNotEmpty() 
            
            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = isLocationValid && isProductValid && manualQuantity.isNotEmpty()
            ) {
                Text("CONFIRMAR ITEM")
            }
             Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

fun handlePedidoAction(pedido: PedidoResponse, viewModel: PedidosViewModel) {
    when (pedido.estado) {
        EstadoPedido.PENDIENTE -> viewModel.asignarPicking(pedido.id) {}
        // EstadoPedido.PICKING_ASIGNADO -> Ya no hay endpoint explícito "iniciar", el primer escaneo cambia el estado.
        // Podríamos llamar a getHojaPicking aquí si quisiéramos refrescar.
        else -> {}
    }
}

fun calculateProgress(detalles: List<DetallePedido>?): Float {
    if (detalles.isNullOrEmpty()) return 0f
    val total = detalles.sumOf { it.cantidadSolicitada }
    val picked = detalles.sumOf { it.cantidadPickeada }
    if (total == 0) return 1f
    return picked.toFloat() / total
}


