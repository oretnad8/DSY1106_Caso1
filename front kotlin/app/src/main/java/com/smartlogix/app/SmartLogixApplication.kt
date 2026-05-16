package com.smartlogix.app

import android.app.Application
import com.smartlogix.app.db.AppDatabase
import com.smartlogix.app.network.RetrofitClient
import com.smartlogix.app.repository.ProductoRepository
import com.smartlogix.app.repository.UbicacionRepository

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Application class para SmartLogix
 * Inicializa base de datos y repositorios
 */
class SmartLogixApplication : Application() {
    
    // Eventos globales de autenticación
    private val _authEvents = MutableSharedFlow<Boolean>()
    val authEvents = _authEvents.asSharedFlow()

    suspend fun emitSessionExpired() {
        _authEvents.emit(true)
    }
    
    companion object {
        private var _instance: SmartLogixApplication? = null
        val instance: SmartLogixApplication
            get() = _instance ?: throw IllegalStateException("Application not initialized")
    }


    // Base de datos
    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(this)
    }

    // Repositorios
    val productoRepository: ProductoRepository by lazy {
        ProductoRepository(
            productoDao = database.productoDao(),
            apiService = RetrofitClient.productosService
        )
    }

    val ubicacionRepository: UbicacionRepository by lazy {
        UbicacionRepository(
            ubicacionDao = database.ubicacionDao(),
            asignacionDao = database.asignacionUbicacionDao(),
            apiService = RetrofitClient.ubicacionesService
        )
    }

    val pedidosRepository: com.smartlogix.app.repository.PedidosRepository by lazy {
        com.smartlogix.app.repository.PedidosRepository()
    }

    val pollingManager: com.smartlogix.app.utils.PollingManager by lazy {
        com.smartlogix.app.utils.PollingManager(pedidosRepository)
    }

    override fun onCreate() {
        super.onCreate()
        _instance = this
        
        // Restaurar token de autenticación si existe
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = prefs.getString("token", null)
        if (token != null) {
            pollingManager.startPolling(this)
        }
    }

    /**
     * Guarda el token de autenticación
     */
    fun saveAuthToken(token: String, rol: String, userId: Int, nombre: String? = null, email: String? = null) {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val editor = prefs.edit()
            .putString("token", token)
            .putString("rol", rol)
            .putInt("userId", userId)
        
        nombre?.let { editor.putString("nombre", it) }
        email?.let { editor.putString("email", it) }
        
        editor.apply()
        
        pollingManager.startPolling(this)
    }

    /**
     * Limpia el token de autenticación (logout)
     */
    fun clearAuthToken() {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        prefs.edit().clear().apply()
        
        pollingManager.stopPolling()
    }

    /**
     * Obtiene el rol del usuario actual
     */
    fun getCurrentUserRole(): String? {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        return prefs.getString("rol", null)
    }

    /**
     * Obtiene el ID del usuario actual
     */
    fun getCurrentUserId(): Int {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        return prefs.getInt("userId", -1)
    }

    /**
     * Verifica si el usuario está autenticado
     */
    fun isAuthenticated(): Boolean {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        return prefs.getString("token", null) != null
    }
}


