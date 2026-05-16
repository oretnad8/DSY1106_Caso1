package com.smartlogix.app.ui.pedidos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartlogix.app.model.OperadorStats
import com.smartlogix.app.model.PedidoStats
import com.smartlogix.app.models.UiState
import com.smartlogix.app.ui.screen.componentes.AppTopBar
import com.smartlogix.app.ui.theme.NavyDashboard
import com.smartlogix.app.viewmodels.PedidosViewModel

@Composable
fun StatsDashboardScreen(
    viewModel: PedidosViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEntregas: () -> Unit
) {
    val statsState by viewModel.statsState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.getStats()
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Dashboard de Control",
                onMenuClick = onNavigateBack,
                actions = {
                    IconButton(onClick = { viewModel.getStats() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refrescar")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = statsState) {
                is UiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is UiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.getStats() }) {
                            Text("Reintentar")
                        }
                    }
                }
                is UiState.Success -> {
                    StatsContent(
                        stats = state.data,
                        onNavigateToEntregas = onNavigateToEntregas
                    )
                }
                UiState.Idle -> {}
            }
        }
    }
}

@Composable
fun StatsContent(
    stats: PedidoStats,
    onNavigateToEntregas: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 1. KPI Cards (Grid 2x2 simulated)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    KPICard(
                        title = "Pedidos Totales",
                        value = "${stats.pedidosTotales}",
                        icon = Icons.Default.AllInbox,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    KPICard(
                        title = "Promedio Picking",
                        value = "${String.format("%.1f", stats.promedioPickingMinutos)} min",
                        icon = Icons.Default.Timer,
                        color = Color(0xFFFB8C00), // Naranja
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    KPICard(
                        title = "Eficiencia Global",
                        value = "${String.format("%.1f", stats.eficienciaGlobal)}%",
                        icon = Icons.Default.TrendingUp,
                        color = Color(0xFF43A047), // Verde
                        modifier = Modifier.weight(1f)
                    )
                    KPICard(
                        title = "Operadores Activos",
                        value = "${stats.operadoresActivos}",
                        icon = Icons.Default.Badge,
                        color = Color(0xFF1E88E5), // Azul
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 2. Eficiencia por Operador
        item {
            Text("Eficiencia por Operador", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = NavyDashboard)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (stats.eficienciaPorOperador.isEmpty()) {
                        Text("No hay datos de operadores", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                    }
                    stats.eficienciaPorOperador.values.forEach { op ->
                        OperadorEfficiencyItem(op)
                    }
                }
            }
        }

        // 3. Distribución de Estados
        item {
            Text("Distribución por Estado", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = NavyDashboard)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    stats.distribucionEstados.forEach { (estado, conteo) ->
                        StateDistributionRow(estado, conteo, stats.pedidosTotales)
                    }
                }
            }
        }

        // 4. Action Button
        item {
            Button(
                onClick = onNavigateToEntregas,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.History, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ver Historial de Entregas")
            }
        }
    }
}

@Composable
fun KPICard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun OperadorEfficiencyItem(op: OperadorStats) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(op.nombre, fontWeight = FontWeight.Bold, color = Color.White)
            Text("${op.entregados}/${op.totalAsignados} entregas", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LinearProgressIndicator(
                progress = (op.eficiencia / 100).toFloat().coerceIn(0f, 1f),
                modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = if (op.eficiencia > 70) Color(0xFF43A047) else if (op.eficiencia > 40) Color(0xFFFDD835) else Color(0xFFE53935),
                trackColor = Color.White.copy(alpha = 0.1f)
            )
            Text("${String.format("%.1f", op.eficiencia)}%", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun StateDistributionRow(estado: String, conteo: Int, total: Int) {
    val progress = if (total > 0) conteo.toFloat() / total else 0f
    val color = when(estado) {
        "PENDIENTE" -> Color(0xFFFDD835) // Amarillo
        "EN_PICKING", "PICKING_ASIGNADO" -> Color(0xFF1E88E5) // Azul
        "PICKING_COMPLETADO", "FACTURADO" -> Color(0xFF8E24AA) // Morado
        "TRANSPORTE_ASIGNADO", "EN_TRANSPORTE" -> Color(0xFFFB8C00) // Naranja
        "ENTREGADO" -> Color(0xFF43A047) // Verde
        else -> MaterialTheme.colorScheme.secondary
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).background(color, RoundedCornerShape(2.dp)))
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            estado.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White
        )
        Text(conteo.toString(), fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.width(16.dp))
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.width(60.dp).height(4.dp).clip(RoundedCornerShape(2.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
    }
}


