package com.smartlogix.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartlogix.app.models.Producto
import com.smartlogix.app.models.UiState
import com.smartlogix.app.network.ProductoRequest
import com.smartlogix.app.ui.screen.componentes.*
import com.smartlogix.app.viewmodels.ProductoViewModel
import com.smartlogix.app.viewmodels.UbicacionViewModel

/**
 * Pantalla de Detalle de Producto
 * CON FUNCIONALIDAD DE EDICIÓN Y ESCÁNER
 *
 * Características:
 * - Ver detalle completo del producto
 * - Editar código de barras individual (manual + escáner)
 * - Editar LPN (manual + escáner)
 * - Asignar a ubicaciones (diálogo + pantalla)
 * - Actualizar usando microservicio real
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleProductoScreen(
    sku: String,
    productoViewModel: ProductoViewModel,
    ubicacionViewModel: UbicacionViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToUbicacion: (Int) -> Unit,
    onNavigateToAsignarUbicacion: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Estados
    val productoDetailState by productoViewModel.productoDetailState.collectAsStateWithLifecycle()
    val selectedProducto by productoViewModel.selectedProducto.collectAsStateWithLifecycle()
    val updateState by productoViewModel.updateState.collectAsStateWithLifecycle()

    // Estados de edición
    var isEditing by remember { mutableStateOf(false) }
    var editedCodigoBarras by remember { mutableStateOf("") }
    var editedLpn by remember { mutableStateOf("") }

    // Estados de escáner
    var showBarcodeScannerForIndividual by remember { mutableStateOf(false) }
    var showBarcodeScannerForLPN by remember { mutableStateOf(false) }

    // Estados de diálogo
    var showAsignarDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Cargar detalle del producto al iniciar
    LaunchedEffect(sku) {
        productoViewModel.getProductoDetail(sku)
    }

    // Inicializar valores de edición cuando se carga el producto
    LaunchedEffect(selectedProducto) {
        selectedProducto?.let { producto ->
            editedCodigoBarras = producto.codigoBarrasIndividual ?: ""
            editedLpn = producto.lpn ?: ""
        }
    }

    // Observar estado de actualización
    LaunchedEffect(updateState) {
        when (updateState) {
            is UiState.Success -> {
                showSuccessDialog = true
                isEditing = false
                productoViewModel.clearUpdateState()
                // Recargar producto
                productoViewModel.getProductoDetail(sku)
            }
            is UiState.Error -> {
                errorMessage = (updateState as UiState.Error).message
                showErrorDialog = true
                productoViewModel.clearUpdateState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            BackTopBar(
                title = if (isEditing) "Editar Producto" else "Detalle del Producto",
                onBackClick = {
                    if (isEditing) {
                        isEditing = false
                    } else {
                        onNavigateBack()
                    }
                }
            )
        },
        floatingActionButton = {
            if (!isEditing && selectedProducto != null) {
                FloatingActionButton(
                    onClick = { isEditing = true }
                ) {
                    Icon(Icons.Default.Edit, "Editar")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = productoDetailState) {
                is UiState.Loading -> {
                    LoadingState(message = "Cargando producto...")
                }

                is UiState.Success -> {
                    val producto = state.data

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        // ========== INFORMACIÓN PRINCIPAL ==========
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = com.smartlogix.app.ui.theme.NavyDashboard
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                // SKU
                                Text(
                                    text = producto.sku,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = com.smartlogix.app.ui.theme.VibrantBlue
                                )

                                // Descripción
                                Text(
                                    text = producto.descripcion,
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.padding(top = 8.dp)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Badge de stock
                                StockBadge(stock = producto.stock)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // ========== CÓDIGOS (CON EDICIÓN Y ESCÁNER) ==========
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = com.smartlogix.app.ui.theme.NavyDashboard
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QrCode,
                                        contentDescription = null,
                                        tint = com.smartlogix.app.ui.theme.VibrantBlue
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Códigos",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                if (isEditing) {
                                    // MODO EDICIÓN: Código de barras individual
                                    Text(
                                        text = "Código Individual",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = editedCodigoBarras,
                                            onValueChange = { editedCodigoBarras = it },
                                            modifier = Modifier.weight(1f),
                                            placeholder = { Text("Escanear o ingresar") },
                                            singleLine = true
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        IconButton(
                                            onClick = { showBarcodeScannerForIndividual = true }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CameraAlt,
                                                contentDescription = "Escanear código",
                                                tint = com.smartlogix.app.ui.theme.VibrantBlue
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // MODO EDICIÓN: LPN
                                    Text(
                                        text = "LPN (Código de Caja)",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = editedLpn,
                                            onValueChange = { editedLpn = it },
                                            modifier = Modifier.weight(1f),
                                            placeholder = { Text("Escanear o ingresar") },
                                            singleLine = true
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        IconButton(
                                            onClick = { showBarcodeScannerForLPN = true }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CameraAlt,
                                                contentDescription = "Escanear LPN",
                                                tint = com.smartlogix.app.ui.theme.VibrantBlue
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Botones de acción
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                isEditing = false
                                                editedCodigoBarras = producto.codigoBarrasIndividual ?: ""
                                                editedLpn = producto.lpn ?: ""
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Cancelar")
                                        }
                                        Button(
                                            onClick = {
                                                // Actualizar producto
                                                val request = ProductoRequest(
                                                    sku = producto.sku,
                                                    descripcion = producto.descripcion,
                                                    stock = producto.stock,
                                                    codigoBarrasIndividual = editedCodigoBarras.ifBlank { null },
                                                    lpn = editedLpn.ifBlank { null },
                                                    lpnDesc = producto.lpnDesc,
                                                    fechaVencimiento = producto.fechaVencimiento
                                                )
                                                productoViewModel.updateProducto(producto.sku, request)
                                            },
                                            modifier = Modifier.weight(1f),
                                            enabled = updateState !is UiState.Loading
                                        ) {
                                            if (updateState is UiState.Loading) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(20.dp),
                                                    strokeWidth = 2.dp
                                                )
                                            } else {
                                                Text("Guardar")
                                            }
                                        }
                                    }

                                } else {
                                    // MODO VISTA: Solo mostrar
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Código Individual",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = producto.codigoBarrasIndividual ?: "No disponible",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "LPN (Código de Caja)",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = producto.lpn ?: "No disponible",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    if (producto.lpnDesc != null) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Descripción LPN",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = producto.lpnDesc,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // ========== FECHA DE VENCIMIENTO ==========
                        if (producto.fechaVencimiento != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (producto.vencimientoCercano)
                                        MaterialTheme.colorScheme.errorContainer
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (producto.vencimientoCercano)
                                            Icons.Default.Warning
                                        else
                                            Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = if (producto.vencimientoCercano)
                                            MaterialTheme.colorScheme.error
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = if (producto.vencimientoCercano)
                                                "⚠️ Vencimiento Cercano"
                                            else
                                                "Fecha de Vencimiento",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (producto.vencimientoCercano)
                                                MaterialTheme.colorScheme.error
                                            else
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = producto.fechaVencimiento,
                                            style = MaterialTheme.typography.bodyLarge,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // ========== UBICACIONES ==========
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = com.smartlogix.app.ui.theme.NavyDashboard
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = com.smartlogix.app.ui.theme.VibrantBlue
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Ubicaciones",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // Botón para asignar ubicación
                                    Row {
                                        IconButton(
                                            onClick = { showAsignarDialog = true }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Asignar ubicación",
                                                tint = com.smartlogix.app.ui.theme.VibrantBlue
                                            )
                                        }
                                        IconButton(
                                            onClick = { onNavigateToAsignarUbicacion(producto.sku) }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.OpenInNew,
                                                contentDescription = "Gestionar ubicaciones",
                                                tint = com.smartlogix.app.ui.theme.VibrantBlue
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                if (producto.ubicaciones.isNullOrEmpty()) {
                                    Text(
                                        text = "Sin ubicación asignada",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    producto.ubicaciones.forEach { ubicacion ->
                                        Card(
                                            onClick = {
                                                onNavigateToUbicacion(ubicacion.idUbicacion)
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = com.smartlogix.app.ui.theme.NavyDashboard
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    // Formato legible: "Pasillo 3 - Piso A - Posición 13 - Nivel 1"
                                                    val displayName = com.smartlogix.app.models.UbicacionFormatter
                                                        .getFriendlyLocationName(ubicacion.codigoUbicacion)
                                                    Text(
                                                        text = displayName,
                                                        style = MaterialTheme.typography.titleSmall,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = "Cantidad: ${ubicacion.cantidad}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                Icon(
                                                    imageVector = Icons.Default.ChevronRight,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(80.dp)) // Espacio para FAB
                    }
                }

                is UiState.Error -> {
                    ErrorState(
                        message = state.message,
                        onRetry = { productoViewModel.getProductoDetail(sku) }
                    )
                }

                is UiState.Idle -> {
                    // Estado inicial
                }
            }
        }
    }

    // ========== ESCÁNER DE CÓDIGO INDIVIDUAL ==========
    if (showBarcodeScannerForIndividual) {
        BarcodeScanner(
            onBarcodeScanned = { code ->
                editedCodigoBarras = code
                showBarcodeScannerForIndividual = false
            },
            onClose = {
                showBarcodeScannerForIndividual = false
            }
        )
    }

    // ========== ESCÁNER DE LPN ==========
    if (showBarcodeScannerForLPN) {
        BarcodeScanner(
            onBarcodeScanned = { code ->
                editedLpn = code
                showBarcodeScannerForLPN = false
            },
            onClose = {
                showBarcodeScannerForLPN = false
            }
        )
    }

    // ========== DIÁLOGO DE ASIGNAR UBICACIÓN ==========
    if (showAsignarDialog && selectedProducto != null) {
        val codigoParaAsignar = selectedProducto!!.codigoBarrasIndividual ?: selectedProducto!!.sku
        AsignarUbicacionDialog(
            codigoBarras = codigoParaAsignar,
            onDismiss = { showAsignarDialog = false },
            onConfirm = { codigoUbicacion, cantidad ->
                ubicacionViewModel.asignarProducto(
                    productoViewModel = productoViewModel,
                    codigoBarras = codigoParaAsignar,
                    codigoUbicacion = codigoUbicacion,
                    cantidad = cantidad
                )
                showAsignarDialog = false
                // Recargar producto para ver nuevas ubicaciones
                productoViewModel.getProductoDetail(sku)
            }
        )
    }

    // ========== DIÁLOGO DE ÉXITO ==========
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Producto Actualizado") },
            text = { Text("Los cambios se han guardado correctamente.") },
            confirmButton = {
                TextButton(onClick = { showSuccessDialog = false }) {
                    Text("Aceptar")
                }
            }
        )
    }

    // ========== DIÁLOGO DE ERROR ==========
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("Aceptar")
                }
            }
        )
    }
}


