package com.smartlogix.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartlogix.app.models.Rol
import com.smartlogix.app.models.UiState
import com.smartlogix.app.models.Usuario
import com.smartlogix.app.ui.screen.componentes.*
import com.smartlogix.app.viewmodels.UsuarioViewModel
import com.smartlogix.app.models.*

/**
 * Pantalla de Gestión de Usuarios
 * Solo para ADMIN
 *
 * Funcionalidades:
 * - Lista de todos los usuarios del sistema
 * - Crear nuevo usuario
 * - Editar usuario existente
 * - Activar/Desactivar usuario
 * - Eliminar usuario
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionUsuariosScreen(
    usuarioViewModel: UsuarioViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Estados
    val usuariosState by usuarioViewModel.usuariosState.collectAsStateWithLifecycle()
    val createState by usuarioViewModel.createUsuarioState.collectAsStateWithLifecycle()
    val deleteState by usuarioViewModel.deleteUsuarioState.collectAsStateWithLifecycle()

    // Estados de UI
    var showCreateDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var usuarioToDelete by remember { mutableStateOf<Usuario?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }

    // Cargar usuarios al iniciar
    LaunchedEffect(Unit) {
        usuarioViewModel.getAllUsuarios()
    }

    // Manejar éxito de creación
    LaunchedEffect(createState) {
        when (val state = createState) {
            is UiState.Success -> {
                successMessage = "Usuario creado exitosamente"
                showSuccessDialog = true
                showCreateDialog = false
                usuarioViewModel.clearCreateState()
            }
            else -> {}
        }
    }

    // Manejar éxito de eliminación
    LaunchedEffect(deleteState) {
        when (val state = deleteState) {
            is UiState.Success -> {
                successMessage = "Usuario eliminado exitosamente"
                showSuccessDialog = true
                showDeleteDialog = false
                usuarioToDelete = null
                usuarioViewModel.clearDeleteState()
            }
            else -> {}
        }
    }

    // Diálogos
    SuccessDialog(
        message = successMessage,
        onDismiss = { showSuccessDialog = false },
        showDialog = showSuccessDialog
    )

    DeleteDialog(
        title = "Eliminar Usuario",
        message = "¿Estás seguro de eliminar a ${usuarioToDelete?.nombre}? Esta acción no se puede deshacer.",
        onConfirm = {
            usuarioToDelete?.let {
                usuarioViewModel.deleteUsuario(it.id)
            }
        },
        onDismiss = {
            showDeleteDialog = false
            usuarioToDelete = null
        },
        showDialog = showDeleteDialog
    )

    Scaffold(
        topBar = {
            BackTopBar(
                title = "Gestión de Usuarios",
                onBackClick = onNavigateBack
            )
        },
        floatingActionButton = {
            ActionFab(
                onClick = { showCreateDialog = true },
                icon = Icons.Default.PersonAdd,
                contentDescription = "Crear usuario"
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = usuariosState) {
                is UiState.Loading -> {
                    LoadingState(message = "Cargando usuarios...")
                }

                is UiState.Success -> {
                    if (state.data.isEmpty()) {
                        EmptyState(
                            icon = Icons.Default.PersonOff,
                            title = "Sin usuarios",
                            message = "No hay usuarios registrados en el sistema"
                        )
                    } else {
                        Column {
                            // Header con contador
                            Text(
                                text = "${state.data.size} usuario(s)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(16.dp)
                            )

                            // Lista de usuarios
                            LazyColumn(
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(state.data) { usuario ->
                                    UsuarioCard(
                                        usuario = usuario,
                                        onToggleActivo = {
                                            usuarioViewModel.toggleActivoUsuario(usuario.id)
                                        },
                                        onDelete = {
                                            usuarioToDelete = usuario
                                            showDeleteDialog = true
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
                        onRetry = { usuarioViewModel.getAllUsuarios() }
                    )
                }

                is UiState.Idle -> {}
            }
        }
    }

    // Diálogo de crear usuario
    if (showCreateDialog) {
        CreateUsuarioDialog(
            usuarioViewModel = usuarioViewModel,
            onDismiss = { showCreateDialog = false },
            isLoading = createState is UiState.Loading
        )
    }
}

/**
 * Card de usuario con acciones
 */
@Composable
private fun UsuarioCard(
    usuario: Usuario,
    onToggleActivo: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(56.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Info del usuario
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = usuario.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = usuario.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RolBadge(rol = usuario.rol)

                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = if (usuario.activo)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            text = if (usuario.activo) "Activo" else "Inactivo",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = if (usuario.activo)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // Acciones
            Column(horizontalAlignment = Alignment.End) {
                IconButton(onClick = onToggleActivo) {
                    Icon(
                        imageVector = if (usuario.activo)
                            Icons.Default.Lock
                        else
                            Icons.Default.LockOpen,
                        contentDescription = if (usuario.activo)
                            "Desactivar"
                        else
                            "Activar"
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/**
 * Diálogo para crear un nuevo usuario
 */
@Composable
private fun CreateUsuarioDialog(
    usuarioViewModel: UsuarioViewModel,
    onDismiss: () -> Unit,
    isLoading: Boolean
) {
    var nombre by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var rolSeleccionado by rememberSaveable { mutableStateOf<Rol?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.PersonAdd, contentDescription = null)
        },
        title = {
            Text("Crear Usuario")
        },
        text = {
            Column {
                AppTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = "Nombre completo",
                    placeholder = "Juan Pérez"
                )
                Spacer(modifier = Modifier.height(8.dp))
                AppTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    placeholder = "juan@SmartLogix.cl"
                )
                Spacer(modifier = Modifier.height(8.dp))
                PasswordTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Contraseña inicial"
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Rol:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Rol.values().forEach { rol ->
                        FilterChip(
                            text = usuarioViewModel.getRolDisplayName(rol),
                            selected = rolSeleccionado == rol,
                            onClick = { rolSeleccionado = rol },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (nombre.isNotBlank() && email.isNotBlank() &&
                        password.isNotBlank() && rolSeleccionado != null) {

                        // Llamamos a la función del ViewModel con parámetros individuales
                        usuarioViewModel.createUsuario(
                            nombre = nombre,
                            email = email,
                            password = password,
                            rol = rolSeleccionado!!.name
                        )

                        // Y probablemente quieras cerrar el diálogo después de crear
                        // onDismiss() o onConfirm()
                    }
                }
            ) {
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

