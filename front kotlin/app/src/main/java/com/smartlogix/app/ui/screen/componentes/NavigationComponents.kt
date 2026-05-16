package com.smartlogix.app.ui.screen.componentes

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.smartlogix.app.models.Rol
import com.smartlogix.app.models.Usuario

/**
 * TopAppBar estándar con menú hamburguesa
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = { Text(text = title) },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menú"
                )
            }
        },
        actions = {
            actions()
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
    )
}

/**
 * TopAppBar con botón de retroceso
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackTopBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = { Text(text = title) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver"
                )
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
    )
}

/**
 * Contenido del Drawer (menú lateral) según el rol del usuario
 */
@Composable
fun DrawerContent(
    currentUser: Usuario?,
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        modifier = modifier,
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerContentColor = MaterialTheme.colorScheme.onSurface,
        drawerShape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp, 16.dp, 16.dp, 0.dp)
    ) {
        // Header del drawer con info del usuario
        DrawerHeader(usuario = currentUser)

        Divider()

        Spacer(modifier = Modifier.height(8.dp))

        // Items según el rol
        when (currentUser?.rol) {
            Rol.ADMIN -> DrawerItemsAdmin(
                currentRoute = currentRoute,
                onNavigate = onNavigate
            )
            Rol.JEFE -> DrawerItemsJefe(
                currentRoute = currentRoute,
                onNavigate = onNavigate
            )
            Rol.SUPERVISOR -> DrawerItemsSupervisor(
                currentRoute = currentRoute,
                onNavigate = onNavigate
            )
            Rol.OPERADOR -> DrawerItemsOperador(
                currentRoute = currentRoute,
                onNavigate = onNavigate
            )
            null -> {}
        }

        Spacer(modifier = Modifier.weight(1f))

        Divider()

        // Botón de cerrar sesión
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.ExitToApp, contentDescription = null) },
            label = { Text("Cerrar Sesión") },
            selected = false,
            onClick = onLogout,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Header del drawer con información del usuario
 */
@Composable
private fun DrawerHeader(usuario: Usuario?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = "SmartLogix WMS",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (usuario != null) {
            Text(
                text = usuario.nombre,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = usuario.email,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            RolBadge(rol = usuario.rol)
        }
    }
}

/**
 * Items del drawer para rol ADMIN
 */
@Composable
private fun DrawerItemsAdmin(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    DrawerMenuItem(
        icon = Icons.Default.Dashboard,
        label = "Dashboard",
        route = "dashboard_admin",
        currentRoute = currentRoute,
        onClick = { onNavigate("dashboard_admin") }
    )
    DrawerMenuItem(
        icon = Icons.Default.Person,
        label = "Gestionar Usuarios",
        route = "gestion_usuarios",
        currentRoute = currentRoute,
        onClick = { onNavigate("gestion_usuarios") }
    )
    DrawerMenuItem(
        icon = Icons.Default.Assessment,
        label = "Reportes",
        route = "reportes",
        currentRoute = currentRoute,
        onClick = { onNavigate("reportes") }
    )
    DrawerMenuItem(
        icon = Icons.Default.Settings,
        label = "Configuración",
        route = "configuracion",
        currentRoute = currentRoute,
        onClick = { onNavigate("configuracion") }
    )
}

/**
 * Items del drawer para rol JEFE
 */
@Composable
private fun DrawerItemsJefe(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    DrawerMenuItem(
        icon = Icons.Default.Dashboard,
        label = "Dashboard",
        route = "dashboard_jefe",
        currentRoute = currentRoute,
        onClick = { onNavigate("dashboard_jefe") }
    )
    DrawerMenuItem(
        icon = Icons.Default.Search,
        label = "Buscar Productos",
        route = "busqueda",
        currentRoute = currentRoute,
        onClick = { onNavigate("busqueda") }
    )
    DrawerMenuItem(
        icon = Icons.Default.LocationOn,
        label = "Ubicaciones",
        route = "gestion_ubicaciones",
        currentRoute = currentRoute,
        onClick = { onNavigate("gestion_ubicaciones") }
    )
    DrawerMenuItem(
        icon = Icons.Default.Assignment,
        label = "Registro Directo",
        route = "registro_directo",
        currentRoute = currentRoute,
        onClick = { onNavigate("registro_directo") }
    )
    DrawerMenuItem(
        icon = Icons.Default.CheckCircle,
        label = "Aprobaciones",
        route = "aprobaciones",
        currentRoute = currentRoute,
        onClick = { onNavigate("aprobaciones") }
    )
}

/**
 * Items del drawer para rol SUPERVISOR
 */
@Composable
private fun DrawerItemsSupervisor(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    // Supervisor tiene casi los mismos accesos que Jefe
    DrawerItemsJefe(currentRoute, onNavigate)
}

/**
 * Items del drawer para rol OPERADOR
 */
@Composable
private fun DrawerItemsOperador(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    DrawerMenuItem(
        icon = Icons.Default.Dashboard,
        label = "Dashboard",
        route = "dashboard_operador",
        currentRoute = currentRoute,
        onClick = { onNavigate("dashboard_operador") }
    )
    DrawerMenuItem(
        icon = Icons.Default.Search,
        label = "Buscar Productos",
        route = "busqueda",
        currentRoute = currentRoute,
        onClick = { onNavigate("busqueda") }
    )
    DrawerMenuItem(
        icon = Icons.Default.LocationOn,
        label = "Ubicaciones",
        route = "gestion_ubicaciones",
        currentRoute = currentRoute,
        onClick = { onNavigate("gestion_ubicaciones") }
    )
    DrawerMenuItem(
        icon = Icons.Default.AddCircle,
        label = "Solicitar Movimiento",
        route = "solicitud_movimiento",
        currentRoute = currentRoute,
        onClick = { onNavigate("solicitud_movimiento") }
    )
    DrawerMenuItem(
        icon = Icons.Default.List,
        label = "Mis Solicitudes",
        route = "mis_solicitudes",
        currentRoute = currentRoute,
        onClick = { onNavigate("mis_solicitudes") }
    )
}

/**
 * Item individual del menú drawer
 */
@Composable
private fun DrawerMenuItem(
    icon: ImageVector,
    label: String,
    route: String,
    currentRoute: String,
    onClick: () -> Unit
) {
    val isSelected = currentRoute == route
    NavigationDrawerItem(
        icon = { 
            Icon(
                imageVector = icon, 
                contentDescription = null,
                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.primary
            ) 
        },
        label = { 
            Text(
                text = label,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            ) 
        },
        selected = isSelected,
        onClick = onClick,
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = Color.White,
            unselectedContainerColor = Color.Transparent,
            unselectedTextColor = MaterialTheme.colorScheme.onSurface,
            selectedIconColor = Color.White,
            unselectedIconColor = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
    )
}

/**
 * Icono de campana con Badge de notificaciones
 */
@Composable
fun NotificationBell(
    pendingCount: Int,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        BadgedBox(
            badge = {
                if (pendingCount > 0) {
                    Badge(containerColor = Color.Red) {
                        Text(pendingCount.toString(), color = Color.White)
                    }
                }
            }
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notificaciones"
            )
        }
    }
}

