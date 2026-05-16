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
import com.smartlogix.app.ui.screen.componentes.*

/**
 * Pantalla de Configuración
 * Solo para ADMIN
 * 
 * Configuraciones del sistema:
 * - Notificaciones
 * - Temas
 * - Parámetros de bodega
 * - Respaldos
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracionScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Estados de configuración
    var notificacionesActivas by remember { mutableStateOf(true) }
    var alertasStock by remember { mutableStateOf(true) }
    var alertasVencimiento by remember { mutableStateOf(true) }
    var temaModo by remember { mutableStateOf("Sistema") }
    
    Scaffold(
        topBar = {
            BackTopBar(
                title = "Configuración",
                onBackClick = onNavigateBack
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
            // ========== NOTIFICACIONES ==========
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Notificaciones",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Notificaciones generales
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Notificaciones activas",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Switch(
                            checked = notificacionesActivas,
                            onCheckedChange = { notificacionesActivas = it }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Alertas de stock
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Alertas de stock bajo",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Notificar cuando stock < 10",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = alertasStock,
                            onCheckedChange = { alertasStock = it },
                            enabled = notificacionesActivas
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Alertas de vencimiento
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Alertas de vencimiento",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Notificar 2 meses antes",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = alertasVencimiento,
                            onCheckedChange = { alertasVencimiento = it },
                            enabled = notificacionesActivas
                        )
                    }
                }
            }
            
            // ========== APARIENCIA ==========
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Apariencia",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Tema",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            text = "Sistema",
                            selected = temaModo == "Sistema",
                            onClick = { temaModo = "Sistema" },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            text = "Claro",
                            selected = temaModo == "Claro",
                            onClick = { temaModo = "Claro" },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            text = "Oscuro",
                            selected = temaModo == "Oscuro",
                            onClick = { temaModo = "Oscuro" },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            // ========== BODEGA ==========
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warehouse,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Parámetros de Bodega",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ConfigRow(
                        label = "Total de pisos",
                        value = "3 (A, B, C)"
                    )
                    
                    ConfigRow(
                        label = "Ubicaciones por piso",
                        value = "60"
                    )
                    
                    ConfigRow(
                        label = "Total de ubicaciones",
                        value = "180"
                    )
                    
                    ConfigRow(
                        label = "Formato SKU",
                        value = "AB12345"
                    )
                }
            }
            
            // ========== SISTEMA ==========
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Sistema",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Botón de respaldo
                    SecondaryButton(
                        text = "Realizar Respaldo",
                        onClick = { /* TODO: Implementar */ },
                        icon = Icons.Default.CloudUpload
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Botón de limpiar caché
                    SecondaryButton(
                        text = "Limpiar Caché",
                        onClick = { /* TODO: Implementar */ },
                        icon = Icons.Default.CleaningServices
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // ========== INFORMACIÓN ==========
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Información",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    ConfigRow(label = "Versión", value = "1.0.0")
                    ConfigRow(label = "Build", value = "2025.10.11")
                    ConfigRow(label = "Base de datos", value = "MySQL 8.0")
                    ConfigRow(label = "Backend", value = "Spring Boot")
                }
            }
        }
    }
}

/**
 * Fila de configuración (label + value)
 */
@Composable
private fun ConfigRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

