package com.smartlogix.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartlogix.app.models.UiState
import com.smartlogix.app.ui.screen.componentes.*
import com.smartlogix.app.viewmodels.UbicacionViewModel

/**
 * Pantalla de Detalle de Ubicación
 *
 * Muestra:
 * - Código de ubicación
 * - Piso y número
 * - Lista de productos almacenados
 * - Capacidad y ocupación
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleUbicacionScreen(
    id: Int,
    ubicacionViewModel: UbicacionViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToProducto: (String) -> Unit,
    onNavigateToConteo: (Int) -> Unit,   // 👈 NUEVO CALLBACK
    modifier: Modifier = Modifier
) {
    // Estados
    val ubicacionDetailState by ubicacionViewModel.ubicacionDetailState.collectAsStateWithLifecycle()

    // Cargar detalle al iniciar
    LaunchedEffect(id) {
        ubicacionViewModel.getUbicacionDetailById(id)
    }

    Scaffold(
        topBar = {
            BackTopBar(
                title = "Detalle de Ubicación",
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = ubicacionDetailState) {
                is UiState.Loading -> {
                    LoadingState(message = "Cargando ubicación...")
                }

                is UiState.Success -> {
                    val ubicacion = state.data

                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            // ======= INFORMACIÓN DE LA UBICACIÓN =======
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = com.smartlogix.app.ui.theme.NavyDashboard
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Pasillo ${ubicacion.pasillo}",
                                        style = MaterialTheme.typography.displayMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = com.smartlogix.app.ui.theme.VibrantBlue
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                        Surface(
                                            shape = MaterialTheme.shapes.medium,
                                            color = com.smartlogix.app.ui.theme.NavyDark
                                        ) {
                                            Text(
                                                text = "Piso ${ubicacion.piso}",
                                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = com.smartlogix.app.ui.theme.VibrantBlue
                                            )
                                        }

                                        Surface(
                                            shape = MaterialTheme.shapes.medium,
                                            color = com.smartlogix.app.ui.theme.NavyDark
                                        ) {
                                            Text(
                                                text = "Posición ${ubicacion.numero}",
                                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = com.smartlogix.app.ui.theme.VibrantBlue
                                            )
                                        }
                                    }

                                    if (ubicacion.esEstante || ubicacion.nivel != null) {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            if (ubicacion.esEstante) {
                                                Badge(
                                                    containerColor = MaterialTheme.colorScheme.tertiary,
                                                    contentColor = MaterialTheme.colorScheme.onTertiary
                                                ) {
                                                    Text(
                                                        text = "ESTANTE",
                                                        style = MaterialTheme.typography.labelLarge,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }

                                            if (ubicacion.nivel != null) {
                                                Badge(
                                                    containerColor = MaterialTheme.colorScheme.secondary,
                                                    contentColor = MaterialTheme.colorScheme.onSecondary
                                                ) {
                                                    Text(
                                                        text = "Nivel: ${ubicacion.nivel}",
                                                        style = MaterialTheme.typography.labelLarge,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            // ======= ESTADÍSTICAS =======
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = com.smartlogix.app.ui.theme.NavyDashboard
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    StatItem(
                                        icon = Icons.Default.Inventory,
                                        label = "Productos",
                                        value = ubicacion.productos?.size?.toString() ?: "0"
                                    )

                                    VerticalDivider(modifier = Modifier.height(60.dp))

                                    StatItem(
                                        icon = Icons.Default.Numbers,
                                        label = "Unidades",
                                        value = ubicacion.productos?.sumOf { it.cantidad }?.toString() ?: "0"
                                    )
                                }
                            }
                        }

                        item {
                            // ======= BOTÓN: CONTEO FÍSICO =======
                            Button(
                                onClick = {
                                    ubicacion.idUbicacion?.let {
                                        onNavigateToConteo(it)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Realizar conteo físico")
                            }
                        }

                        item {
                            Text(
                                text = "Productos Almacenados",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (ubicacion.productos.isNullOrEmpty()) {
                            item {
                                EmptyState(
                                    icon = Icons.Default.Inbox,
                                    title = "Ubicación Vacía",
                                    message = "No hay productos en esta ubicación"
                                )
                            }
                        } else {
                            items(ubicacion.productos) { productoEnUbicacion ->
                                Card(
                                    onClick = {
                                        onNavigateToProducto(productoEnUbicacion.sku)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = com.smartlogix.app.ui.theme.NavyDashboard
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = MaterialTheme.shapes.medium,
                                            color = com.smartlogix.app.ui.theme.NavyDark,
                                            modifier = Modifier.size(56.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Inventory,
                                                    contentDescription = null,
                                                    tint = com.smartlogix.app.ui.theme.VibrantBlue
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(16.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = productoEnUbicacion.sku,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = com.smartlogix.app.ui.theme.VibrantBlue
                                            )
                                            Text(
                                                text = productoEnUbicacion.descripcion,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Text(
                                                text = "Cantidad: ${productoEnUbicacion.cantidad}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = "Ver producto"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                is UiState.Error -> {
                    ErrorState(
                        message = state.message,
                        onRetry = {
                            ubicacionViewModel.getUbicacionDetailById(id)
                        }
                    )
                }

                is UiState.Idle -> {}
            }
        }
    }
}

/**
 * Item de estadística
 */
@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = com.smartlogix.app.ui.theme.VibrantBlue
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = com.smartlogix.app.ui.theme.VibrantBlue
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


