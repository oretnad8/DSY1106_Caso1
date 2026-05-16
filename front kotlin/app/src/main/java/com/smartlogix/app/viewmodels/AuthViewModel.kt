package com.smartlogix.app.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartlogix.app.SmartLogixApplication
import com.smartlogix.app.models.*
import com.smartlogix.app.network.LoginRequest
import com.smartlogix.app.network.LoginResponse
import com.smartlogix.app.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para autenticación y gestión de sesión
 * Maneja login, logout, validación de token y estado del usuario actual
 * USA MICROSERVICIOS REALES - SIN MOCKS
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = RetrofitClient.authService
    private val app = application as? SmartLogixApplication

    // ========== ESTADO DE AUTENTICACIÓN ==========

    private val _authState = MutableStateFlow<AuthState>(AuthState.NotAuthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        // Escuchar eventos globales de sesión expirada
        viewModelScope.launch {
            app?.authEvents?.collect { expired ->
                if (expired) {
                    _currentUser.value = null
                    _authState.value = AuthState.NotAuthenticated
                }
            }
        }
    }

    private val _currentUser = MutableStateFlow<Usuario?>(null)
    val currentUser: StateFlow<Usuario?> = _currentUser.asStateFlow()

    // ========== FUNCIONES DE AUTENTICACIÓN ==========

    /**
     * Realiza el login con email y password
     * Conecta con: POST /api/auth/login
     * @param email Email del usuario
     * @param password Contraseña del usuario
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading

                // Llamar al API real
                val request = LoginRequest(email = email, password = password)
                val response = apiService.login(request)

                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!

                    // Guardar usuario en estado
                    _currentUser.value = Usuario(
                        id = loginResponse.id,
                        nombre = loginResponse.nombre,
                        email = loginResponse.email,
                        rol = Rol.valueOf(loginResponse.rol),
                        activo = true
                    )

                    // Guardar token en SharedPreferences usando SmartLogixApplication
                    app?.saveAuthToken(
                        token = loginResponse.token,
                        rol = loginResponse.rol,
                        userId = loginResponse.id,
                        nombre = loginResponse.nombre,
                        email = loginResponse.email
                    )

                    _authState.value = AuthState.Authenticated(
                        usuario = _currentUser.value!!,
                        token = loginResponse.token
                    )
                } else {
                    // Error del servidor
                    val errorMessage = when (response.code()) {
                        401 -> "Credenciales incorrectas"
                        403 -> "Usuario no autorizado"
                        404 -> "Usuario no encontrado"
                        else -> "Error al iniciar sesión: ${response.message()}"
                    }
                    _authState.value = AuthState.Error(message = errorMessage)
                }

            } catch (e: Exception) {
                _authState.value = AuthState.Error(
                    message = e.message ?: "Error de conexión. Verifica tu conexión a internet."
                )
            }
        }
    }

    /**
     * Cierra la sesión del usuario actual
     * Conecta con: POST /api/auth/logout
     */
    fun logout() {
        viewModelScope.launch {
            try {
                // Llamar al backend para invalidar el token
                val response = apiService.logout()

                // Incluso si falla el logout en el servidor, limpiar sesión local
                // Limpiar token de SharedPreferences
                app?.clearAuthToken()

                // Limpiar estado local
                _currentUser.value = null
                _authState.value = AuthState.NotAuthenticated

            } catch (e: Exception) {
                // Incluso si falla, limpiar sesión local
                app?.clearAuthToken()
                _currentUser.value = null
                _authState.value = AuthState.NotAuthenticated
            }
        }
    }

    /**
     * Valida el token JWT almacenado
     * Conecta con: GET /api/auth/validate
     * Se ejecuta al iniciar la app para verificar si hay sesión activa
     */
    fun validateToken() {
        viewModelScope.launch {
            try {
                // Recuperar token de SharedPreferences
                val prefs = getApplication<Application>().getSharedPreferences("auth", Application.MODE_PRIVATE)
                val storedToken = prefs.getString("token", null)
                val storedRol = prefs.getString("rol", null)
                val storedUserId = prefs.getInt("userId", -1)
                val storedNombre = prefs.getString("nombre", null)
                val storedEmail = prefs.getString("email", null)

                if (storedToken == null) {
                    _authState.value = AuthState.NotAuthenticated
                    return@launch
                }

                _authState.value = AuthState.Loading

                // Validar token con backend (el interceptor agregará el token)
                val response = try {
                    apiService.validateToken()
                } catch (e: Exception) {
                    null
                }

                if (response != null && response.isSuccessful) {
                    // Token válido, recuperar info del usuario desde SharedPreferences
                    if (storedRol != null && storedUserId != -1 && storedNombre != null && storedEmail != null) {
                        val usuario = Usuario(
                            id = storedUserId,
                            nombre = storedNombre,
                            email = storedEmail,
                            rol = Rol.valueOf(storedRol),
                            activo = true
                        )
                        _currentUser.value = usuario
                        _authState.value = AuthState.Authenticated(
                            usuario = usuario,
                            token = storedToken
                        )
                    } else {
                        // Datos incompletos, requerir login
                        app?.clearAuthToken()
                        _authState.value = AuthState.NotAuthenticated
                    }
                } else {
                    // Token inválido, expirado o error de red
                    app?.clearAuthToken()
                    _authState.value = AuthState.NotAuthenticated
                }
            } catch (e: Exception) {
                // Error inesperado
                app?.clearAuthToken()
                _authState.value = AuthState.NotAuthenticated
            }
        }
    }

    /**
     * Verifica si el usuario actual tiene un rol específico
     * @param requiredRole Rol requerido para la acción
     * @return true si el usuario tiene el rol requerido
     */
    fun hasRole(requiredRole: Rol): Boolean {
        return _currentUser.value?.rol == requiredRole
    }

    /**
     * Verifica si el usuario actual tiene permisos de jefe o superior
     * @return true si es ADMIN, JEFE o SUPERVISOR
     */
    fun isJefeOrAbove(): Boolean {
        val rol = _currentUser.value?.rol
        return rol == Rol.ADMIN || rol == Rol.JEFE || rol == Rol.SUPERVISOR
    }

    /**
     * Obtiene el header de autorización para las peticiones
     * @return String con formato "Bearer {token}"
     */
    fun getAuthHeader(): String? {
        val prefs = getApplication<Application>().getSharedPreferences("auth", Application.MODE_PRIVATE)
        return prefs.getString("token", null)?.let { "Bearer $it" }
    }

    /**
     * Limpia el estado de error para poder reintentar login
     */
    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.NotAuthenticated
        }
    }
}

