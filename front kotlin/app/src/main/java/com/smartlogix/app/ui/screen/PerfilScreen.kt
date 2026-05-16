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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartlogix.app.ui.screen.componentes.*
import com.smartlogix.app.viewmodels.AuthViewModel

/**
 * Pantalla de Perfil del Usuario
 *
 * Funcionalidades:
 * - Ver información del usuario actual
 * - Ver rol y permisos
 * - Cambiar contraseña
 * - Cerrar sesión
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    authViewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Estados
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()

    // Estados de UI
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }

    // Diálogo de confirmación de cierre de sesión
    ConfirmDialog(
        title = "Cerrar Sesión",
        message = "¿Estás seguro de que deseas cerrar sesión?",
        onConfirm = {
            authViewModel.logout()
            onNavigateToLogin()
        },
        onDismiss = { showLogoutDialog = false },
        showDialog = showLogoutDialog,
        confirmText = "Cerrar Sesión",
        dismissText = "Cancelar"
    )

    // Diálogo de información (cambio de contraseña no implementado)
    InfoDialog(
        title = "Cambiar Contraseña",
        message = "Esta funcionalidad estará disponible próximamente.",
        onDismiss = { showChangePasswordDialog = false },
        showDialog = showChangePasswordDialog
    )

    Scaffold(
        topBar = {
            BackTopBar(
                title = "Mi Perfil",
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
            // ========== AVATAR Y NOMBRE ==========
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar grande
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(120.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(64.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Nombre
                    Text(
                        text = currentUser?.nombre ?: "Usuario",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    // Email
                    Text(
                        text = currentUser?.email ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Badge de rol
                    currentUser?.let { user ->
                        RolBadge(rol = user.rol)
                    }
                }
            }

            // ========== INFORMACIÓN DE LA CUENTA ==========
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Información de la Cuenta",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    ProfileInfoRow(
                        icon = Icons.Default.Person,
                        label = "Nombre",
                        value = currentUser?.nombre ?: "N/A"
                    )

                    ProfileInfoRow(
                        icon = Icons.Default.Email,
                        label = "Email",
                        value = currentUser?.email ?: "N/A"
                    )

                    ProfileInfoRow(
                        icon = Icons.Default.Badge,
                        label = "Rol",
                        value = when (currentUser?.rol?.name) {
                            "ADMIN" -> "Administrador"
                            "JEFE" -> "Jefe de Bodega"
                            "SUPERVISOR" -> "Supervisor"
                            "OPERADOR" -> "Operador"
                            else -> "N/A"
                        }
                    )

                    ProfileInfoRow(
                        icon = Icons.Default.CheckCircle,
                        label = "Estado",
                        value = if (currentUser?.activo == true) "Activo" else "Inactivo"
                    )
                }
            }

            // ========== PERMISOS SEGÚN ROL ==========
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "ℹ️ Tus Permisos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    when (currentUser?.rol?.name) {
                        "ADMIN" -> {
                            PermissionItem("✓ Crear y gestionar usuarios")
                            PermissionItem("✓ Acceso a reportes del sistema")
                            PermissionItem("✓ Configuración general")
                        }
                        "JEFE" -> {
                            PermissionItem("✓ Aprobar solicitudes sin restricciones")
                            PermissionItem("✓ Registro directo de movimientos")
                            PermissionItem("✓ Enviar mensajes a operadores")
                            PermissionItem("✓ Gestión completa de ubicaciones")
                        }
                        "SUPERVISOR" -> {
                            PermissionItem("✓ Aprobar solicitudes (notifica a jefe)")
                            PermissionItem("✓ Registro directo de movimientos")
                            PermissionItem("✓ Enviar mensajes a operadores")
                        }
                        "OPERADOR" -> {
                            PermissionItem("✓ Buscar y consultar productos")
                            PermissionItem("✓ Solicitar movimientos (requiere aprobación)")
                            PermissionItem("✓ Realizar conteos de inventario")
                            PermissionItem("✓ Ver mensajes del jefe")
                        }
                    }
                }
            }

            // ========== ACCIONES ==========
            Text(
                text = "Acciones",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Cambiar contraseña
            Card(
                onClick = { showChangePasswordDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Cambiar Contraseña",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Cerrar sesión
            Card(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Cerrar Sesión",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Versión de la app
            Text(
                text = "SmartLogix v1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

/**
 * Fila de información del perfil
 */
@Composable
private fun ProfileInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Item de permiso
 */
@Composable
private fun PermissionItem(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

