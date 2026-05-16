package com.smartlogix.app.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartlogix.app.R
import com.smartlogix.app.models.AuthState
import com.smartlogix.app.navigation.getDashboardForRole
import com.smartlogix.app.ui.screen.componentes.*
import com.smartlogix.app.ui.theme.BlueLighter
import com.smartlogix.app.viewmodels.AuthViewModel

/**
 * Pantalla de Login
 * Permite autenticación de usuarios y redirige al dashboard según el rol
 *
 * Características:
 * - Logo de la empresa centrado en la parte superior
 * - Validación de campos en tiempo real
 * - Manejo de estados (cargando, error, autenticado)
 * - Diferenciación de roles automática después del login
 */
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onNavigateToHome: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Estados del ViewModel
    val authState by authViewModel.authState.collectAsStateWithLifecycle()

    // Estados locales del formulario
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var emailError by rememberSaveable { mutableStateOf<String?>(null) }
    var passwordError by rememberSaveable { mutableStateOf<String?>(null) }

    // Estados de UI
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Efecto para navegar cuando se autentica exitosamente
    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.Authenticated -> {
                val dashboardRoute = getDashboardForRole(state.usuario.rol.name)
                onNavigateToHome(dashboardRoute.route)
            }
            is AuthState.Error -> {
                errorMessage = state.message
                showErrorDialog = true
            }
            else -> {}
        }
    }

    // Diálogo de error
    ErrorDialog(
        message = errorMessage,
        onDismiss = {
            showErrorDialog = false
            authViewModel.clearError()
        },
        showDialog = showErrorDialog
    )

    // Layout principal
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo SmartLogix",
                modifier = Modifier.width(180.dp).wrapContentHeight(), // MAS ANCHO Y FLEXIBLE
                contentScale = ContentScale.Fit // MANTIENE RELACION DE ASPECTO
            )


            // ========== TÍTULO ==========
            Text(
                text = "SmartLogix",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Sistema de Gestión de Bodega",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
            )

            // ========== FORMULARIO ==========
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = com.smartlogix.app.ui.theme.NavyDashboard
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Iniciar Sesión",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = BlueLighter, // AZUL SOLICITADO #50A2FF
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // Campo Email
                    AppTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = null
                        },
                        label = "Correo electrónico",
                        placeholder = "ejemplo@SmartLogix.cl",
                        leadingIcon = Icons.Default.Email,
                        isError = emailError != null,
                        errorMessage = emailError,
                        imeAction = ImeAction.Next,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo Password
                    PasswordTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = null
                        },
                        isError = passwordError != null,
                        errorMessage = passwordError,
                        imeAction = ImeAction.Done,
                        onImeAction = {
                            // Validar y hacer login al presionar Done
                            if (validateAndLogin(
                                    email = email,
                                    password = password,
                                    onEmailError = { emailError = it },
                                    onPasswordError = { passwordError = it },
                                    onLogin = { authViewModel.login(email, password) }
                                )) {
                                // Login iniciado
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botón de Login
                    PrimaryButton(
                        text = "Ingresar",
                        onClick = {
                            validateAndLogin(
                                email = email,
                                password = password,
                                onEmailError = { emailError = it },
                                onPasswordError = { passwordError = it },
                                onLogin = { authViewModel.login(email, password) }
                            )
                        },
                        enabled = authState !is AuthState.Loading,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Indicador de carga
                    if (authState is AuthState.Loading) {
                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

/**
 * Función para validar campos y ejecutar login
 * @return true si la validación es exitosa
 */
private fun validateAndLogin(
    email: String,
    password: String,
    onEmailError: (String?) -> Unit,
    onPasswordError: (String?) -> Unit,
    onLogin: () -> Unit
): Boolean {
    var isValid = true

    // Validar email
    if (email.isBlank()) {
        onEmailError("El correo es obligatorio")
        isValid = false
    } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        onEmailError("Correo electrónico inválido")
        isValid = false
    } else {
        onEmailError(null)
    }

    // Validar password
    if (password.isBlank()) {
        onPasswordError("La contraseña es obligatoria")
        isValid = false
    } else if (password.length < 3) {
        onPasswordError("La contraseña debe tener al menos 3 caracteres")
        isValid = false
    } else {
        onPasswordError(null)
    }

    // Si todo es válido, ejecutar login
    if (isValid) {
        onLogin()
    }

    return isValid
}

/**
 * Componente para mostrar información de usuario de prueba
 */
@Composable
private fun TestUserInfo(email: String, role: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "•",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = email,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = role,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

