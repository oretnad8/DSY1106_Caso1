package com.smartlogix.app.ui.screen.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smartlogix.app.models.*

/**
 * Card para mostrar un producto en resultados de búsqueda
 */
@Composable
fun ProductoCard(
    producto: Producto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = com.smartlogix.app.ui.theme.NavyDashboard
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // SKU y descripción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = producto.sku,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = producto.descripcion,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Badge de stock
                StockBadge(stock = producto.stock)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Información adicional
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Ubicaciones
                if (!producto.ubicaciones.isNullOrEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = com.smartlogix.app.ui.theme.VibrantBlue
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${producto.ubicaciones.size} ubicación(es)",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                // Alerta de vencimiento
                if (producto.vencimientoCercano) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Vencimiento cercano",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFFFFA726) // Naranja
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Vence pronto",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFFA726)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Card para mostrar una solicitud de aprobación
 */
@Composable
fun AprobacionCard(
    aprobacion: Aprobacion,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = com.smartlogix.app.ui.theme.NavyDashboard
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Tipo de movimiento y estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (aprobacion.tipoMovimiento) {
                            TipoMovimiento.INGRESO -> Icons.Default.ArrowDownward
                            TipoMovimiento.EGRESO -> Icons.Default.ArrowUpward
                            TipoMovimiento.REUBICACION -> Icons.Default.SwapHoriz
                        },
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = com.smartlogix.app.ui.theme.VibrantBlue
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = aprobacion.tipoMovimiento.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                EstadoBadge(estado = aprobacion.estado)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Código de Barras y cantidad
            Text(
                text = "Código de Barras: ${aprobacion.codigoBarras}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Cantidad: ${aprobacion.cantidad}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 2.dp)
            )

            // Motivo
            Text(
                text = aprobacion.motivo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Solicitante y fecha
            Text(
                text = "Solicitado por: ${aprobacion.solicitante}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Card para mostrar una solicitud (alias de AprobacionCard)
 * Usado en MisSolicitudesScreen
 */
@Composable
fun SolicitudCard(
    solicitud: Aprobacion,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AprobacionCard(
        aprobacion = solicitud,
        onClick = onClick,
        modifier = modifier
    )
}

/**
 * Card para mostrar una ubicación
 */
@Composable
fun UbicacionCard(
    ubicacion: Ubicacion,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = com.smartlogix.app.ui.theme.NavyDashboard
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Código de ubicación
            Text(
                text = ubicacion.formattedDisplayName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (ubicacion.esEstante) {
                Spacer(modifier = Modifier.height(4.dp))
                Badge(
                    containerColor = com.smartlogix.app.ui.theme.NavyDark,
                    contentColor = com.smartlogix.app.ui.theme.VibrantBlue
                ) {
                    Text(
                        text = "ESTANTE",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }

            if (ubicacion.nivel != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Nivel: ${ubicacion.nivel}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Cantidad de productos
            val cantidadProductos = ubicacion.productos?.size ?: 0
            Text(
                text = if (cantidadProductos > 0)
                    "$cantidadProductos producto(s)"
                else
                    "Vacía",
                style = MaterialTheme.typography.bodyMedium,
                color = if (cantidadProductos > 0)
                    com.smartlogix.app.ui.theme.VibrantBlue
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


