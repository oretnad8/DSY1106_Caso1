package com.smartlogix.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartlogix.app.models.EstadoAprobacion
import com.smartlogix.app.models.UiState
import com.smartlogix.app.ui.screen.componentes.*
import com.smartlogix.app.viewmodels.AprobacionViewModel
import com.smartlogix.app.viewmodels.AuthViewModel
import kotlinx.coroutines.launch

/**
 * Pantalla de Aprobaciones
 * Solo para JEFE y SUPERVISOR
 *
 * Funcionalidades:
 * - Lista de solicitudes de movimiento pendientes de aprobación
 * - Filtros por estado (Pendiente, Aprobado, Rechazado)
 * - Acceso rápido a aprobar/rechazar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AprobacionesScreen(
    authViewModel: AuthViewModel,
    aprobacionViewModel: AprobacionViewModel,
    onNavigateToDetail: (Int) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Estados
    val aprobacionesState by aprobacionViewModel.aprobacionesState.collectAsStateWithLifecycle()
    val estadoFiltro by aprobacionViewModel.estadoFiltro.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    // Cargar aprobaciones al iniciar
    LaunchedEffect(Unit) {
        aprobacionViewModel.getAllAprobaciones()
    }

    Scaffold(
        topBar = {
            BackTopBar(
                title = "Aprobaciones",
                onBackClick = onNavigateBack
                )
            }
        ) { paddingValues ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // ========== FILTROS POR ESTADO ==========
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Filtrar por estado",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Todos
                            FilterChip(
                                text = "Todos",
                                selected = estadoFiltro == null,
                                onClick = {
                                    aprobacionViewModel.getAllAprobaciones()
                                },
                                modifier = Modifier.weight(1f)
                            )

                            // Pendientes
                            FilterChip(
                                text = "Pendientes",
                                selected = estadoFiltro == EstadoAprobacion.PENDIENTE,
                                onClick = {
                                    aprobacionViewModel.getAprobacionesByEstado(EstadoAprobacion.PENDIENTE)
                                },
                                modifier = Modifier.weight(1f)
                            )

                            // Aprobados
                            FilterChip(
                                text = "Aprobados",
                                selected = estadoFiltro == EstadoAprobacion.APROBADO,
                                onClick = {
                                    aprobacionViewModel.getAprobacionesByEstado(EstadoAprobacion.APROBADO)
                                },
                                modifier = Modifier.weight(1f)
                            )

                            // Rechazados
                            FilterChip(
                                text = "Rechazados",
                                selected = estadoFiltro == EstadoAprobacion.RECHAZADO,
                                onClick = {
                                    aprobacionViewModel.getAprobacionesByEstado(EstadoAprobacion.RECHAZADO)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // ========== LISTA DE APROBACIONES ==========
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    when (val state = aprobacionesState) {
                        is UiState.Loading -> {
                            LoadingState(message = "Cargando solicitudes...")
                        }

                        is UiState.Success -> {
                            if (state.data.isEmpty()) {
                                EmptyState(
                                    icon = Icons.Default.CheckCircle,
                                    title = "Sin solicitudes",
                                    message = if (estadoFiltro != null) {
                                        "No hay solicitudes con estado ${estadoFiltro!!.name}"
                                    } else {
                                        "No hay solicitudes de aprobación"
                                    }
                                )
                            } else {
                                Column {
                                    // Header con contador
                                    Text(
                                        text = "${state.data.size} solicitud(es)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(16.dp)
                                    )

                                    // Lista de aprobaciones
                                    LazyColumn(
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        items(state.data) { aprobacion ->
                                            AprobacionCard(
                                                aprobacion = aprobacion,
                                                onClick = {
                                                    onNavigateToDetail(aprobacion.id)
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
                                    if (estadoFiltro != null) {
                                        aprobacionViewModel.getAprobacionesByEstado(estadoFiltro!!)
                                    } else {
                                        aprobacionViewModel.getAllAprobaciones()
                                    }
                                }
                            )
                        }

                        is UiState.Idle -> {
                            EmptyState(
                                icon = Icons.Default.Assignment,
                                title = "Aprobaciones",
                                message = "Selecciona un filtro para ver las solicitudes"
                            )
                        }
                    }
                }
            }
        }
    }


