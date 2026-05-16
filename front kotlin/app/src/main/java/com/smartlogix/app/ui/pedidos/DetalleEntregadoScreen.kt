package com.smartlogix.app.ui.pedidos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartlogix.app.model.*
import com.smartlogix.app.models.UiState
import com.smartlogix.app.ui.screen.componentes.AppTopBar
import com.smartlogix.app.ui.theme.NavyDark
import com.smartlogix.app.ui.theme.VibrantBlue
import com.smartlogix.app.viewmodels.PedidosViewModel

@Composable
fun DetalleEntregadoScreen(
    pedidoId: Long,
    viewModel: PedidosViewModel,
    onNavigateBack: () -> Unit
) {
    val detailState by viewModel.pedidoDetalleCompletoState.collectAsStateWithLifecycle()

    LaunchedEffect(pedidoId) {
        viewModel.getPedidoDetalleCompleto(pedidoId)
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Detalle de Entrega",
                onMenuClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = detailState) {
                is UiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is UiState.Success -> {
                    DetalleEntregadoContent(state.data)
                }
                is UiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                        Button(onClick = { viewModel.getPedidoDetalleCompleto(pedidoId) }) {
                            Text("Reintentar")
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun DetalleEntregadoContent(pedido: PedidoDetalleDTO) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Nota de Venta #${pedido.id}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Cliente: ${pedido.cliente}", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = NavyDark,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            pedido.estadoActual,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = VibrantBlue
                        )
                    }
                }
            }
        }

        // 2. Factura e Imagen de Entrega
        item {
            SectionHeader(title = "Evidencia y Documentación", icon = Icons.Default.Receipt)
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Número de Factura: ${pedido.numeroFactura ?: "Pendiente"}", fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (!pedido.fotoEntrega.isNullOrEmpty()) {
                        Text("Foto de Entrega:", style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Base64ImageViewer(pedido.fotoEntrega)
                    } else {
                        Text("No hay foto de entrega disponible", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }
        }

        // 3. Responsables
        item {
            SectionHeader(title = "Equipo Responsable", icon = Icons.Default.Groups)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ResponsibleCard(title = "Vendedor", name = pedido.vendedor?.nombre, modifier = Modifier.weight(1f), icon = Icons.Default.Person)
                ResponsibleCard(title = "Picking", name = pedido.operadorPicking?.nombre, modifier = Modifier.weight(1f), icon = Icons.Default.Hardware)
                ResponsibleCard(title = "Transporte", name = pedido.operadorTransporte?.nombre, modifier = Modifier.weight(1f), icon = Icons.Default.LocalShipping)
            }
        }

        // 4. Items
        item {
            SectionHeader(title = "Items del Pedido", icon = Icons.Default.List)
        }
        items(pedido.detalles) { item ->
            ItemRow(item)
        }

        // 5. Timeline
        item {
             SectionHeader(title = "Historial de Estados", icon = Icons.Default.History)
             Timeline(pedido.historial)
        }
    }
}

@Composable
fun Timeline(historial: List<HistorialEstadoDTO>) {
    Column(modifier = Modifier.fillMaxWidth().padding(start = 8.dp)) {
        historial.forEachIndexed { index, event ->
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.size(12.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                    if (index < historial.size - 1) {
                        Box(modifier = Modifier.width(2.dp).fillMaxHeight().background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)))
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.padding(bottom = 16.dp)) {
                    Text(event.estado, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(event.usuarioNombre, style = MaterialTheme.typography.bodySmall)
                    Text(formatDate(event.timestamp), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun Base64ImageViewer(base64String: String) {
    val bitmap = remember(base64String) {
        try {
            val cleanBase64 = if (base64String.contains(",")) base64String.split(",")[1] else base64String
            val decodedBytes = android.util.Base64.decode(cleanBase64, android.util.Base64.DEFAULT)
            android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            null
        }
    }

    if (bitmap != null) {
        androidx.compose.foundation.Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Evidencia de entrega",
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(modifier = Modifier.fillMaxWidth().height(100.dp).background(Color.LightGray), contentAlignment = Alignment.Center) {
            Text("Error al cargar imagen")
        }
    }
}

@Composable
fun ResponsibleCard(title: String, name: String?, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))) {
        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
            Text(title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text(name ?: "N/A", style = MaterialTheme.typography.bodySmall, maxLines = 1)
        }
    }
}

@Composable
fun ItemRow(item: ItemDetalleDTO) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.descripcion, fontWeight = FontWeight.Bold)
                Text("SKU: ${item.sku}", style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Cant: ${item.cantidadPickeada}/${item.cantidadSolicitada}", fontWeight = FontWeight.Medium)
                if (!item.ubicacionSugerida.isNullOrEmpty()) {
                    Text("Ubic: ${item.ubicacionSugerida}", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

private fun formatDate(isoString: String): String {
    return try {
        val parts = isoString.split("T")
        val datePart = parts[0]
        val timePart = parts[1].substring(0, 5)
        "$timePart ${datePart.split("-").reversed().joinToString("/")}"
    } catch (e: Exception) {
        isoString
    }
}


