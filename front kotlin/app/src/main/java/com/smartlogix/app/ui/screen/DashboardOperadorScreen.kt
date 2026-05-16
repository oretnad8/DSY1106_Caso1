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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartlogix.app.models.UiState
import com.smartlogix.app.ui.screen.componentes.*
import com.smartlogix.app.viewmodels.AuthViewModel

import kotlinx.coroutines.launch
import com.smartlogix.app.navigation.Screen

/**
 * Dashboard para rol OPERADOR
 *
 * Funcionalidades principales:
 * - Mensajes del jefe de bodega
 * - Tareas pendientes asignadas
 * - Acciones rápidas (buscar productos, solicitar movimientos)
 * - Estado de mis solicitudes
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardOperadorScreen(
    authViewModel: AuthViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Estados
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Drawer con menú lateral
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                currentUser = currentUser,
                currentRoute = "dashboard_operador",
                onNavigate = { route ->
                    scope.launch {
                        drawerState.close()
                        onNavigate(route)
                    }
                },
                onLogout = {
                    authViewModel.logout()
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as com.smartlogix.app.SmartLogixApplication

                AppTopBar(
                    title = "Dashboard Operador",
                    onMenuClick = {
                        scope.launch { drawerState.open() }
                    },
                    actions = {
                        val pendingCount by app.pollingManager.pendingOrdersCount.collectAsStateWithLifecycle()
                        NotificationBell(
                            pendingCount = pendingCount,
                            onClick = { 
                                onNavigate("lista_pedidos")
                            }
                        )
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // ========== SALUDO ==========
                Text(
                    text = "Bienvenido, ${currentUser?.nombre ?: "Operador"}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ========== ACCIONES RÁPIDAS ==========
                Text(
                    text = "Acciones Rápidas",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Notas de Venta (Pendientes)
                    QuickActionCard(
                        icon = Icons.Default.Assignment,
                        text = "Notas de Venta",
                        onClick = { onNavigate(Screen.ListaPedidos.buildRoute(false)) },
                        modifier = Modifier.weight(1f)
                    )

                    // Mis Pedidos (Historial y En Curso)
                    QuickActionCard(
                        icon = Icons.Default.History,
                        text = "Mis Pedidos",
                        onClick = { onNavigate(Screen.ListaPedidos.buildRoute(true)) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Ver Ubicaciones
                    QuickActionCard(
                        icon = Icons.Default.LocationOn,
                        text = "Ver Ubicaciones",
                        onClick = { onNavigate("gestion_ubicaciones") },
                        modifier = Modifier.weight(1f)
                    )

                    // Solicitar Movimiento
                    QuickActionCard(
                        icon = Icons.Default.AddCircle,
                        text = "Solicitar Movimiento",
                        onClick = { onNavigate("solicitud_movimiento") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Buscar Producto
                    QuickActionCard(
                        icon = Icons.Default.Search,
                        text = "Buscar Producto",
                        onClick = { onNavigate("busqueda") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Tarjeta de tarea pendiente
 */
@Composable
private fun TaskCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    priority: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val priorityColor = when (priority) {
        "ALTA" -> Color(0xFFD32F2F)
        "MEDIA" -> Color(0xFFF57C00)
        else -> Color(0xFF388E3C)
    }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
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
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = priorityColor
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface // AZUL VIBRANTE
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = priorityColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = priority,
                            style = MaterialTheme.typography.labelSmall,
                            color = priorityColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

/**
 * Tarjeta de acción rápida compacta
 */
@Composable
private fun QuickActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = com.smartlogix.app.ui.theme.NavyDashboard
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSurface // AZUL VIBRANTE
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

