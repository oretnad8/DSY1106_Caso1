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
 * Pantalla de Mis Solicitudes
 * Solo para OPERADORES
 *
 * Funcionalidades:
 * - Lista de solicitudes del usuario actual
 * - Filtros por estado (Pendiente, Aprobado, Rechazado)
 * - Ver detalles de cada solicitud
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisSolicitudesScreen(
    authViewModel: AuthViewModel,
    aprobacionViewModel: AprobacionViewModel,
    onNavigateToDetail: (Int) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Estados
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val misSolicitudesState by aprobacionViewModel.misSolicitudesState.collectAsStateWithLifecycle()
    val estadoFiltro by aprobacionViewModel.estadoFiltro.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Cargar mis solicitudes al iniciar
    LaunchedEffect(Unit) {
        aprobacionViewModel.getMisSolicitudes()
    }

    // Drawer con menú lateral
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                currentUser = currentUser,
                currentRoute = "mis_solicitudes",
                onNavigate = { route ->
                    scope.launch {
                        drawerState.close()
                        onNavigateBack()
                    }
                },
                onLogout = {
                    authViewModel.logout()
                    onNavigateBack()
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                AppTopBar(
                    title = "Mis Solicitudes",
                    onMenuClick = {
                        scope.launch { drawerState.open() }
                    }
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
                            FilterChip(
                                text = "Todos",
                                selected = estadoFiltro == null,
                                onClick = {
                                    aprobacionViewModel.clearEstadoFilter()
                                    aprobacionViewModel.getMisSolicitudes()
                                },
                                modifier = Modifier.weight(1f)
                            )

                            FilterChip(
                                text = "Pendiente",
                                selected = estadoFiltro == EstadoAprobacion.PENDIENTE,
                                onClick = {
                                    // TODO: Implementar filtro por estado en backend
                                    aprobacionViewModel.getMisSolicitudes()
                                },
                                modifier = Modifier.weight(1f)
                            )

                            FilterChip(
                                text = "Aprobado",
                                selected = estadoFiltro == EstadoAprobacion.APROBADO,
                                onClick = {
                                    aprobacionViewModel.getMisSolicitudes()
                                },
                                modifier = Modifier.weight(1f)
                            )

                            FilterChip(
                                text = "Rechazado",
                                selected = estadoFiltro == EstadoAprobacion.RECHAZADO,
                                onClick = {
                                    aprobacionViewModel.getMisSolicitudes()
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // ========== CONTENIDO ==========
                when (val state = misSolicitudesState) {
                    is UiState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    is UiState.Success -> {
                        if (state.data.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                contentAlignment = androidx.compose.ui.Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(64.dp)
                                            .padding(bottom = 16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "No hay solicitudes",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Todas tus solicitudes han sido procesadas",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(vertical = 16.dp)
                            ) {
                                items(state.data) { solicitud ->
                                    SolicitudCard(
                                        solicitud = solicitud,
                                        onClick = { onNavigateToDetail(solicitud.id) }
                                    )
                                }
                            }
                        }
                    }

                    is UiState.Error -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .padding(bottom = 16.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = "Error",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = state.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                                PrimaryButton(
                                    text = "Reintentar",
                                    onClick = { aprobacionViewModel.getMisSolicitudes() },
                                    modifier = Modifier.padding(top = 16.dp)
                                )
                            }
                        }
                    }

                    is UiState.Idle -> {
                        // No hacer nada
                    }
                }
            }
        }
    }
}


