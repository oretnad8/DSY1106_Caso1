package com.smartlogix.app.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartlogix.app.db.AppDatabase
import com.smartlogix.app.db.entities.UsuarioLocal
import com.smartlogix.app.models.*
import com.smartlogix.app.network.RetrofitClient
import com.smartlogix.app.network.UsuarioRequest
import com.smartlogix.app.network.UsuarioResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para gestión de usuarios
 * USA MICROSERVICIOS REALES - SIN MOCKS
 */
class UsuarioViewModel(application: Application) : AndroidViewModel(application) {

    private val usuarioDao = AppDatabase.getDatabase(application).usuarioDao()
    private val apiService = RetrofitClient.usuariosService

    // ========== ESTADOS ==========

    private val _usuariosState = MutableStateFlow<UiState<List<Usuario>>>(UiState.Idle)
    val usuariosState: StateFlow<UiState<List<Usuario>>> = _usuariosState.asStateFlow()

    private val _selectedUsuario = MutableStateFlow<Usuario?>(null)
    val selectedUsuario: StateFlow<Usuario?> = _selectedUsuario.asStateFlow()

    private val _usuarioDetailState = MutableStateFlow<UiState<Usuario>>(UiState.Idle)
    val usuarioDetailState: StateFlow<UiState<Usuario>> = _usuarioDetailState.asStateFlow()

    private val _createUsuarioState = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val createUsuarioState: StateFlow<UiState<Boolean>> = _createUsuarioState.asStateFlow()

    private val _updateUsuarioState = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val updateUsuarioState: StateFlow<UiState<Boolean>> = _updateUsuarioState.asStateFlow()

    private val _deleteUsuarioState = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val deleteUsuarioState: StateFlow<UiState<Boolean>> = _deleteUsuarioState.asStateFlow()

    // ========== CONSULTA DE USUARIOS ==========

    /**
     * Obtiene todos los usuarios
     * GET http://SmartLogix.ddns.net:8082/api/usuarios
     */
    fun getAllUsuarios() {
        viewModelScope.launch {
            try {
                _usuariosState.value = UiState.Loading

                val response = apiService.getUsuarios()
                
                if (response.isSuccessful && response.body() != null) {
                    val usuarios = response.body()!!.map { it.toDomainModel() }
                    _usuariosState.value = UiState.Success(usuarios)
                } else {
                    _usuariosState.value = UiState.Error(
                        message = "Error ${response.code()}: ${response.message()}"
                    )
                }

            } catch (e: Exception) {
                _usuariosState.value = UiState.Error(
                    message = "Error al obtener usuarios: ${e.message}"
                )
            }
        }
    }

    /**
     * Obtiene un usuario por ID
     * GET http://SmartLogix.ddns.net:8082/api/usuarios/{id}
     */
    fun getUsuarioDetail(id: Int) {
        viewModelScope.launch {
            try {
                _usuarioDetailState.value = UiState.Loading

                val response = apiService.getUsuarioById(id)
                
                if (response.isSuccessful && response.body() != null) {
                    val usuario = response.body()!!.toDomainModel()
                    _selectedUsuario.value = usuario
                    _usuarioDetailState.value = UiState.Success(usuario)
                } else {
                    _usuarioDetailState.value = UiState.Error(
                        message = "Error ${response.code()}: ${response.message()}"
                    )
                }

            } catch (e: Exception) {
                _usuarioDetailState.value = UiState.Error(
                    message = "Error al obtener usuario: ${e.message}"
                )
            }
        }
    }

    /**
     * Filtra usuarios por rol (filtro local)
     */
    fun getUsuariosByRol(rol: String) {
        viewModelScope.launch {
            try {
                _usuariosState.value = UiState.Loading

                val response = apiService.getUsuarios()
                
                if (response.isSuccessful && response.body() != null) {
                    val usuarios = response.body()!!.map { it.toDomainModel() }
                    val filtered = usuarios.filter { it.rol.name == rol }
                    _usuariosState.value = UiState.Success(filtered)
                } else {
                    _usuariosState.value = UiState.Error(
                        message = "Error ${response.code()}: ${response.message()}"
                    )
                }

            } catch (e: Exception) {
                _usuariosState.value = UiState.Error(
                    message = "Error al filtrar usuarios: ${e.message}"
                )
            }
        }
    }

    /**
     * Obtiene solo usuarios activos (filtro local)
     */
    fun getUsuariosActivos() {
        viewModelScope.launch {
            try {
                _usuariosState.value = UiState.Loading

                val response = apiService.getUsuarios()
                
                if (response.isSuccessful && response.body() != null) {
                    val usuarios = response.body()!!.map { it.toDomainModel() }
                    val filtered = usuarios.filter { it.activo }
                    _usuariosState.value = UiState.Success(filtered)
                } else {
                    _usuariosState.value = UiState.Error(
                        message = "Error ${response.code()}: ${response.message()}"
                    )
                }

            } catch (e: Exception) {
                _usuariosState.value = UiState.Error(
                    message = "Error al obtener usuarios activos: ${e.message}"
                )
            }
        }
    }

    // ========== GESTIÓN DE USUARIOS ==========

    /**
     * Crea un nuevo usuario
     * POST http://SmartLogix.ddns.net:8082/api/usuarios
     * Patrón local-first
     */
    fun createUsuario(nombre: String, email: String, password: String, rol: String) {
        viewModelScope.launch {
            try {
                _createUsuarioState.value = UiState.Loading

                // 1. Guardar localmente
                val usuarioLocal = UsuarioLocal(
                    nombre = nombre,
                    email = email,
                    rol = rol, // Guardamos como String
                    timestamp = System.currentTimeMillis()
                )
                val localId = usuarioDao.insertarUsuarioPendiente(usuarioLocal)

                try {
                    // 2. Enviar al backend
                    val request = UsuarioRequest(
                        nombre = nombre,
                        email = email,
                        password = password,
                        rol = rol
                    )
                    val response = apiService.createUsuario(request)
                    
                    if (response.isSuccessful && response.code() == 200) {
                        // 3. Eliminar local si éxito
                        usuarioDao.deleteById(localId)
                        _createUsuarioState.value = UiState.Success(true)
                        // Recargar lista
                        getAllUsuarios()
                    } else {
                        _createUsuarioState.value = UiState.Error(
                            "Guardado localmente. Error backend: ${response.code()}"
                        )
                    }
                } catch (e: Exception) {
                    _createUsuarioState.value = UiState.Error(
                        "Guardado localmente. Se sincronizará después."
                    )
                }

            } catch (e: Exception) {
                _createUsuarioState.value = UiState.Error(
                    message = "Error al crear usuario: ${e.message}"
                )
            }
        }
    }

    /**
     * Actualiza un usuario
     * PUT http://SmartLogix.ddns.net:8082/api/usuarios/{id}
     */
    fun updateUsuario(id: Int, nombre: String, email: String, rol: String, password: String? = null) {
        viewModelScope.launch {
            try {
                _updateUsuarioState.value = UiState.Loading

                val request = UsuarioRequest(
                    nombre = nombre,
                    email = email,
                    password = password ?: "", // Si no se proporciona, mantener la actual
                    rol = rol
                )
                val response = apiService.updateUsuario(id, request)
                
                if (response.isSuccessful && response.code() == 200) {
                    _updateUsuarioState.value = UiState.Success(true)
                    // Recargar detalle
                    getUsuarioDetail(id)
                } else {
                    _updateUsuarioState.value = UiState.Error(
                        message = "Error ${response.code()}: ${response.message()}"
                    )
                }

            } catch (e: Exception) {
                _updateUsuarioState.value = UiState.Error(
                    message = "Error al actualizar usuario: ${e.message}"
                )
            }
        }
    }

    /**
     * Elimina un usuario
     * DELETE http://SmartLogix.ddns.net:8082/api/usuarios/{id}
     */
    fun deleteUsuario(id: Int) {
        viewModelScope.launch {
            try {
                _deleteUsuarioState.value = UiState.Loading

                val response = apiService.deleteUsuario(id)
                
                if (response.isSuccessful && response.code() == 200) {
                    _deleteUsuarioState.value = UiState.Success(true)
                    // Recargar lista
                    getAllUsuarios()
                } else {
                    _deleteUsuarioState.value = UiState.Error(
                        message = "Error ${response.code()}: ${response.message()}"
                    )
                }

            } catch (e: Exception) {
                _deleteUsuarioState.value = UiState.Error(
                    message = "Error al eliminar usuario: ${e.message}"
                )
            }
        }
    }

    /**
     * Toggle activo/inactivo
     * POST http://SmartLogix.ddns.net:8082/api/usuarios/{id}/toggle-activo
     */
    fun toggleActivo(id: Int) {
        viewModelScope.launch {
            try {
                val response = apiService.toggleActivo(id)
                
                if (response.isSuccessful) {
                    // Recargar lista
                    getAllUsuarios()
                }

            } catch (e: Exception) {
                // Error silencioso
            }
        }
    }

    // ========== UTILIDADES ==========

    fun clearUsuarios() {
        _usuariosState.value = UiState.Idle
    }

    fun clearSelectedUsuario() {
        _selectedUsuario.value = null
        _usuarioDetailState.value = UiState.Idle
    }

    fun clearCreateState() {
        _createUsuarioState.value = UiState.Idle
    }

    fun clearUpdateState() {
        _updateUsuarioState.value = UiState.Idle
    }

    fun clearDeleteState() {
        _deleteUsuarioState.value = UiState.Idle
    }

    fun toggleActivoUsuario(id: Int) {
        toggleActivo(id)
    }

    fun getRolDisplayName(rol: Rol): String {
        return when (rol) {
            Rol.ADMIN -> "Administrador"
            Rol.JEFE -> "Jefe de Bodega"
            Rol.SUPERVISOR -> "Supervisor"
            Rol.OPERADOR -> "Operador"
        }
    }

    // ========== CONVERSIÓN ==========

    /**
     * Convierte UsuarioResponse a modelo de dominio
     */
    private fun UsuarioResponse.toDomainModel(): Usuario {
        return Usuario(
            id = this.id,
            nombre = this.nombre,
            email = this.email,
            rol = Rol.valueOf(this.rol),
            activo = this.activo
        )
    }
}


