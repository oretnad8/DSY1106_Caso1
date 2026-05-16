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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartlogix.app.ui.screen.componentes.*
import com.smartlogix.app.viewmodels.AuthViewModel
import com.smartlogix.app.viewmodels.AprobacionViewModel

import kotlinx.coroutines.launch
import com.smartlogix.app.navigation.Screen

/**
 * Dashboard para rol JEFE DE BODEGA
 *
 * Funcionalidades principales:
 * - Alertas del sistema (stock bajo, solicitudes pendientes)
 * - Contadores de aprobaciones pendientes y aprobadas del día
 * - Acciones rápidas (ver aprobaciones, enviar mensajes)
 * - Alertas de productos con vencimiento cercano
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardJefeScreen(
    authViewModel: AuthViewModel,
    aprobacionViewModel: AprobacionViewModel,
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
                currentRoute = "dashboard_jefe",
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
                    title = "Dashboard Jefe",
                    onMenuClick = {
                        scope.launch { drawerState.open() }
                    },
                    actions = {
                        val pendingCount by app.pollingManager.pendingOrdersCount.collectAsStateWithLifecycle()
                        NotificationBell(
                            pendingCount = pendingCount,
                            onClick = { onNavigate("lista_pedidos") }
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
                    text = "Bienvenido, ${currentUser?.nombre ?: "Jefe"}",
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

                // Estadísticas
                QuickActionButton(
                    icon = Icons.Default.BarChart,
                    text = "Ver Estadísticas",
                    onClick = { onNavigate("estadisticas") }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Revisar Pedidos (Facturados)
                QuickActionButton(
                    icon = Icons.Default.FactCheck,
                    text = "Revisar Pedidos",
                    onClick = { onNavigate(Screen.RevisarPedidos.route) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Ver Aprobaciones
                QuickActionButton(
                    icon = Icons.Default.CheckCircle,
                    text = "Ver Aprobaciones",
                    onClick = { onNavigate("aprobaciones") }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Registro Directo
                QuickActionButton(
                    icon = Icons.Default.Assignment,
                    text = "Registro Directo",
                    onClick = { onNavigate("registro_directo") }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Buscar Producto
                QuickActionButton(
                    icon = Icons.Default.Search,
                    text = "Buscar Producto",
                    onClick = { onNavigate("busqueda") }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Ver Ubicaciones y Notas de Venta
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionButton(
                        icon = Icons.Default.LocationOn,
                        text = "Ubicaciones",
                        onClick = { onNavigate("gestion_ubicaciones") },
                        modifier = Modifier.weight(1f)
                    )

                    QuickActionButton(
                        icon = Icons.Default.Assignment,
                        text = "Notas de Venta",
                        onClick = { onNavigate(Screen.ListaPedidos.buildRoute(false)) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Tarjeta contador con ícono
 */
@Composable
private fun CounterCard(
    icon: ImageVector,
    title: String,
    count: Int,
    backgroundColor: Color,
    iconColor: Color,
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
                modifier = Modifier.size(40.dp),
                tint = iconColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = iconColor
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

/**
 * Botón de acción rápida con ícono
 */
@Composable
private fun QuickActionButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Ir",
                tint = MaterialTheme.colorScheme.onSurface // AZUL VIBRANTE
            )
        }
    }
}

