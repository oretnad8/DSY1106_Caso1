package com.smartlogix.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartlogix.app.models.Pasillo
import com.smartlogix.app.models.Piso
import com.smartlogix.app.models.UiState
import com.smartlogix.app.ui.screen.componentes.*
import com.smartlogix.app.viewmodels.UbicacionViewModel

/**
 * Pantalla de Gestión de Ubicaciones
 *
 * Funcionalidades:
 * - Vista de todas las ubicaciones por pasillo y piso
 * - Filtro por pasillo (1-5)
 * - Filtro por piso (A, B, C)
 * - Vista en cuadrícula de ubicaciones
 * - Jefe: Asignación directa de productos
 * - Operador: Solicitar cambio de ubicación (requiere aprobación)
 * 
 * Actualizado para soportar 5 pasillos × 60 posiciones × 3 pisos = 900 ubicaciones
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionUbicacionesScreen(
    ubicacionViewModel: UbicacionViewModel,
    onNavigateToDetail: (Int) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Estados
    val ubicacionesState by ubicacionViewModel.ubicacionesState.collectAsStateWithLifecycle()
    val pisoSeleccionado by ubicacionViewModel.pisoSeleccionado.collectAsStateWithLifecycle()
    val pasilloSeleccionado by ubicacionViewModel.pasilloSeleccionado.collectAsStateWithLifecycle()
    val esEstanteSolo by ubicacionViewModel.esEstanteSolo.collectAsStateWithLifecycle()

    // Cargar ubicaciones al iniciar
    LaunchedEffect(Unit) {
        ubicacionViewModel.getAllUbicaciones()
    }
    Scaffold(
        topBar = {
            BackTopBar(
                title = "Ubicaciones",
                onBackClick = onNavigateBack
            )
        }
        ) { paddingValues ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // ========== FILTRO POR PASILLO ==========
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = com.smartlogix.app.ui.theme.NavyDashboard
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Filtrar por pasillo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Botón Todos
                            FilterChip(
                                text = "Todos",
                                selected = pasilloSeleccionado == null,
                                onClick = {
                                    ubicacionViewModel.clearPasilloFilter()
                                },
                                modifier = Modifier.weight(1f)
                            )

                            // Botones de pasillos 1-5
                            Pasillo.values().forEach { pasillo ->
                                FilterChip(
                                    text = "P${pasillo.numero}",
                                    selected = pasilloSeleccionado == pasillo,
                                    onClick = {
                                        ubicacionViewModel.getUbicacionesByPasillo(pasillo)
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // ========== FILTRO POR PISO ==========
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = com.smartlogix.app.ui.theme.NavyDashboard
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Filtrar por piso",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Botón Todos
                            FilterChip(
                                text = "Todos",
                                selected = pisoSeleccionado == null,
                                onClick = {
                                    ubicacionViewModel.clearPisoFilter()
                                },
                                modifier = Modifier.weight(1f)
                            )

                            // Botón Piso A
                            FilterChip(
                                text = "Piso A",
                                selected = pisoSeleccionado == Piso.A,
                                onClick = {
                                    ubicacionViewModel.getUbicacionesByPiso(Piso.A)
                                },
                                modifier = Modifier.weight(1f)
                            )

                            // Botón Piso B
                            FilterChip(
                                text = "Piso B",
                                selected = pisoSeleccionado == Piso.B,
                                onClick = {
                                    ubicacionViewModel.getUbicacionesByPiso(Piso.B)
                                },
                                modifier = Modifier.weight(1f)
                            )

                            // Botón Piso C
                            FilterChip(
                                text = "Piso C",
                                selected = pisoSeleccionado == Piso.C,
                                onClick = {
                                    ubicacionViewModel.getUbicacionesByPiso(Piso.C)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // ========== FILTROS AVANZADOS ==========
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = com.smartlogix.app.ui.theme.NavyDashboard
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = null,
                                tint = com.smartlogix.app.ui.theme.VibrantBlue
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Solo Estantes",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Switch(
                            checked = esEstanteSolo,
                            onCheckedChange = { ubicacionViewModel.toggleSoloEstantes() }
                        )
                    }
                }

                // ========== LISTA DE UBICACIONES ==========
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    when (val state = ubicacionesState) {
                        is UiState.Loading -> {
                            LoadingState(message = "Cargando ubicaciones...")
                        }

                        is UiState.Success -> {
                            if (state.data.isEmpty()) {
                                EmptyState(
                                    icon = Icons.Default.LocationOff,
                                    title = "Sin ubicaciones",
                                    message = "No se encontraron ubicaciones para los filtros seleccionados"
                                )
                            } else {
                                Column {
                                    // Header con contador y filtros activos
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${state.data.size} ubicación(es)",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )

                                        // Mostrar filtros activos
                                        val filtrosActivos = buildString {
                                            if (pasilloSeleccionado != null) {
                                                append("Pasillo ${pasilloSeleccionado!!.numero}")
                                            }
                                            if (pisoSeleccionado != null) {
                                                if (isNotEmpty()) append(" • ")
                                                append("Piso ${pisoSeleccionado!!.codigo}")
                                            }
                                            if (esEstanteSolo) {
                                                if (isNotEmpty()) append(" • ")
                                                append("Estantes")
                                            }
                                            if (isEmpty()) {
                                                append("Todos")
                                            }
                                        }

                                        Text(
                                            text = filtrosActivos,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = com.smartlogix.app.ui.theme.VibrantBlue
                                        )
                                    }

                                    // Grid de ubicaciones
                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(3),
                                        contentPadding = PaddingValues(16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        items(state.data) { ubicacion ->
                                            UbicacionCard(
                                                ubicacion = ubicacion,
                                                onClick = {
                                                    onNavigateToDetail(ubicacion.idUbicacion)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        is UiState.Error -> {
                            ErrorState(
                                message = state.message,
                                onRetry = {
                                    when {
                                        pasilloSeleccionado != null && pisoSeleccionado != null -> {
                                            ubicacionViewModel.getUbicacionesByPasilloYPiso(
                                                pasilloSeleccionado!!,
                                                pisoSeleccionado!!
                                            )
                                        }
                                        pasilloSeleccionado != null -> {
                                            ubicacionViewModel.getUbicacionesByPasillo(pasilloSeleccionado!!)
                                        }
                                        pisoSeleccionado != null -> {
                                            ubicacionViewModel.getUbicacionesByPiso(pisoSeleccionado!!)
                                        }
                                        else -> {
                                            ubicacionViewModel.getAllUbicaciones()
                                        }
                                    }
                                }
                            )
                        }

                        is UiState.Idle -> {
                            EmptyState(
                                icon = Icons.Default.LocationOn,
                                title = "Ubicaciones",
                                message = "Selecciona un pasillo o piso para ver las ubicaciones"
                            )
                        }
                    }
                }
            }
        }
    }



