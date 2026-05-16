package com.smartlogix.app.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartlogix.app.db.AppDatabase
import com.smartlogix.app.db.entities.SolicitudMovimientoLocal
import com.smartlogix.app.models.*
import com.smartlogix.app.network.RetrofitClient
import com.smartlogix.app.network.AprobarRequest
import com.smartlogix.app.network.RechazarRequest
import com.smartlogix.app.network.AprobacionResponse
import com.smartlogix.app.network.AprobacionRequest as NetworkAprobacionRequest
import com.smartlogix.app.network.AsignarUbicacionRequest
import com.smartlogix.app.network.EgresoUbicacionRequest
import com.smartlogix.app.network.ReubicarUbicacionRequest
import com.smartlogix.app.services.UbicacionService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para gestión de aprobaciones
 * Maneja solicitudes de movimientos (INGRESO/EGRESO/REUBICACION)
 * y su flujo de aprobación/rechazo
 * USA MICROSERVICIOS REALES - SIN MOCKS
 */
class AprobacionViewModel(application: Application) : AndroidViewModel(application) {

    private val solicitudMovimientoDao = AppDatabase.getDatabase(application).solicitudMovimientoDao()
    private val apiService = RetrofitClient.aprobacionesService
    private val usuariosService = RetrofitClient.usuariosService
    private val ubicacionService = UbicacionService(application)
    private val prefs = application.getSharedPreferences("auth", android.content.Context.MODE_PRIVATE)
    
    // Obtener ID del usuario actual desde SharedPreferences
    private fun getCurrentUserId(): Int {
        return prefs.getInt("userId", -1)
    }

    // ========== ESTADOS ==========

    private val _aprobacionesState = MutableStateFlow<UiState<List<Aprobacion>>>(UiState.Idle)
    val aprobacionesState: StateFlow<UiState<List<Aprobacion>>> = _aprobacionesState.asStateFlow()

    private val _selectedAprobacion = MutableStateFlow<Aprobacion?>(null)
    val selectedAprobacion: StateFlow<Aprobacion?> = _selectedAprobacion.asStateFlow()

    private val _aprobacionDetailState = MutableStateFlow<UiState<Aprobacion>>(UiState.Idle)
    val aprobacionDetailState: StateFlow<UiState<Aprobacion>> = _aprobacionDetailState.asStateFlow()

    private val _createSolicitudState = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val createSolicitudState: StateFlow<UiState<Boolean>> = _createSolicitudState.asStateFlow()

    private val _respuestaState = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val respuestaState: StateFlow<UiState<Boolean>> = _respuestaState.asStateFlow()

    private val _misSolicitudesState = MutableStateFlow<UiState<List<Aprobacion>>>(UiState.Idle)
    val misSolicitudesState: StateFlow<UiState<List<Aprobacion>>> = _misSolicitudesState.asStateFlow()

    private val _estadoFiltro = MutableStateFlow<EstadoAprobacion?>(null)
    val estadoFiltro: StateFlow<EstadoAprobacion?> = _estadoFiltro.asStateFlow()

    /**
     * Convierte AprobacionResponse a modelo de dominio
     * Traduce IDs de ubicación a códigos legibles
     */
    private suspend fun AprobacionResponse.toDomainModel(): Aprobacion {
        val origenCodigo = if (this.idUbicacionOrigen != null) {
            ubicacionService.getCodigoById(this.idUbicacionOrigen)
        } else null

        val destinoCodigo = if (this.idUbicacionDestino != null) {
            ubicacionService.getCodigoById(this.idUbicacionDestino)
        } else null

        return Aprobacion(
            id = this.id,
            tipoMovimiento = TipoMovimiento.valueOf(this.tipoMovimiento),
            codigoBarras = this.codigoBarras,
            cantidad = this.cantidad,
            motivo = this.motivo,
            estado = EstadoAprobacion.valueOf(this.estado),
            solicitante = this.solicitante?.nombre ?: "Desconocido",
            idSolicitante = this.solicitante?.id ?: 0,
            aprobador = this.aprobador?.nombre,
            idAprobador = this.aprobador?.id,
            observaciones = this.observaciones,
            fechaSolicitud = this.fechaSolicitud,
            fechaRespuesta = this.fechaAprobacion,
            idUbicacionOrigen = this.idUbicacionOrigen,
            idUbicacionDestino = this.idUbicacionDestino,
            ubicacionOrigen = origenCodigo,
            ubicacionDestino = destinoCodigo
        )
    }

    /**
     * Obtiene todas las solicitudes de aprobación
     */
    fun getAllAprobaciones() {
        viewModelScope.launch {
            try {
                _aprobacionesState.value = UiState.Loading

                val response = apiService.getAprobaciones(null)
                
                if (response.isSuccessful && response.body() != null) {
                    val aprobaciones = response.body()!!.map { it.toDomainModel() }
                    _aprobacionesState.value = UiState.Success(aprobaciones)
                } else {
                    _aprobacionesState.value = UiState.Error(
                        message = "Error ${response.code()}: ${response.message()}"
                    )
                }

            } catch (e: Exception) {
                _aprobacionesState.value = UiState.Error(
                    message = "Error al obtener aprobaciones: ${e.message}"
                )
            }
        }
    }

    /**
     * Obtiene aprobaciones filtradas por estado
     */
    fun getAprobacionesByEstado(estado: EstadoAprobacion) {
        viewModelScope.launch {
            try {
                _aprobacionesState.value = UiState.Loading
                _estadoFiltro.value = estado

                val response = apiService.getAprobaciones(estado.name)
                
                if (response.isSuccessful && response.body() != null) {
                    val aprobaciones = response.body()!!.map { it.toDomainModel() }
                    _aprobacionesState.value = UiState.Success(aprobaciones)
                } else {
                    _aprobacionesState.value = UiState.Error(
                        message = "Error ${response.code()}: ${response.message()}"
                    )
                }

            } catch (e: Exception) {
                _aprobacionesState.value = UiState.Error(
                    message = "Error al filtrar aprobaciones: ${e.message}"
                )
            }
        }
    }

    /**
     * Obtiene las solicitudes del usuario actual
     */
    fun getMisSolicitudes() {
        viewModelScope.launch {
            try {
                _misSolicitudesState.value = UiState.Loading

                val response = apiService.getMisSolicitudes()
                
                if (response.isSuccessful && response.body() != null) {
                    val aprobaciones = response.body()!!.map { it.toDomainModel() }
                    _misSolicitudesState.value = UiState.Success(aprobaciones)
                } else {
                    _misSolicitudesState.value = UiState.Error(
                        message = "Error ${response.code()}: ${response.message()}"
                    )
                }

            } catch (e: Exception) {
                _misSolicitudesState.value = UiState.Error(
                    message = "Error al obtener mis solicitudes: ${e.message}"
                )
            }
        }
    }

    /**
     * Obtiene el detalle de una aprobación
     */
    fun getAprobacionDetail(id: Int) {
        viewModelScope.launch {
            try {
                _aprobacionDetailState.value = UiState.Loading

                val response = apiService.getAprobacionById(id)
                
                if (response.isSuccessful && response.body() != null) {
                    val aprobacion = response.body()!!.toDomainModel()
                    _selectedAprobacion.value = aprobacion
                    _aprobacionDetailState.value = UiState.Success(aprobacion)
                } else {
                    _aprobacionDetailState.value = UiState.Error(
                        message = "Error ${response.code()}: ${response.message()}"
                    )
                }

            } catch (e: Exception) {
                _aprobacionDetailState.value = UiState.Error(
                    message = "Error al obtener detalle: ${e.message}"
                )
            }
        }
    }

    /**
     * Aprueba una solicitud
     * Actualizado para manejar INGRESO, EGRESO y REUBICACION correctamente
     */
    fun aprobarSolicitud(id: Int, observaciones: String? = null) {
        viewModelScope.launch {
            try {
                _respuestaState.value = UiState.Loading

                // 1. Obtener detalles de la solicitud legibles (con códigos ya traducidos)
                val detalleResponse = apiService.getAprobacionById(id)
                if (!detalleResponse.isSuccessful || detalleResponse.body() == null) {
                    _respuestaState.value = UiState.Error(message = "Error al obtener detalles")
                    return@launch
                }

                val solicitudResponse = detalleResponse.body()!!
                val solicitud = solicitudResponse.toDomainModel()
                val ubicacionesService = RetrofitClient.ubicacionesService

                // 2. Delegar el procesamiento de stock al backend
                // Anteriormente se intentaba procesar manualmente desde la app, 
                // pero esto genera duplicidad y errores de autorización (403).
                
                // 3. Obtener ID del aprobador y aprobar
                val idAprobador = getCurrentUserId()
                val request = AprobarRequest(observaciones = observaciones, idAprobador = idAprobador)
                val response = apiService.aprobarSolicitud(id, request)
                
                if (response.isSuccessful) {
                    _respuestaState.value = UiState.Success(true)
                    // Refrescar lista inmediatamente
                    getAllAprobaciones()
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = try {
                        val json = com.google.gson.JsonParser.parseString(errorBody).asJsonObject
                        json.get("message")?.asString ?: "Error backend: ${response.code()}"
                    } catch (e: Exception) {
                        "Error backend: ${response.code()}"
                    }
                    _respuestaState.value = UiState.Error(message = errorMessage)
                }

            } catch (e: Exception) {
                _respuestaState.value = UiState.Error(message = "Error al aprobar: ${e.message}")
            }
        }
    }

    // ========== CREACIÓN DE SOLICITUDES ==========

    /**
     * Crea solicitud de INGRESO
     */
    fun solicitarIngreso(codigoBarras: String, cantidad: Int, ubicacionDestino: String, motivo: String) {
        viewModelScope.launch {
            try {
                _createSolicitudState.value = UiState.Loading

                val idDestino = ubicacionService.getIdUbicacionByCodigo(ubicacionDestino)
                if (idDestino == null) {
                    _createSolicitudState.value = UiState.Error(message = "Ubicación destino no válida")
                    return@launch
                }

                val idSolicitante = getCurrentUserId()
                
                // 1. Guardar localmente
                val solicitudLocal = SolicitudMovimientoLocal(
                    tipoMovimiento = "INGRESO",
                    codigoBarras = codigoBarras,
                    cantidad = cantidad,
                    motivo = motivo,
                    idUbicacionOrigen = null,
                    idUbicacionDestino = idDestino,
                    timestamp = System.currentTimeMillis()
                )
                val localId = solicitudMovimientoDao.insertarSolicitud(solicitudLocal)

                try {
                    // 2. Enviar al backend
                    val request = NetworkAprobacionRequest(
                        tipoMovimiento = "INGRESO",
                        codigoBarras = codigoBarras,
                        cantidad = cantidad,
                        motivo = motivo,
                        idUbicacionOrigen = null,
                        idUbicacionDestino = idDestino,
                        idSolicitante = idSolicitante
                    )
                    val response = apiService.createAprobacion(request)
                    
                    if (response.isSuccessful) {
                        solicitudMovimientoDao.deleteById(localId)
                        _createSolicitudState.value = UiState.Success(true)
                    } else {
                        _createSolicitudState.value = UiState.Error("Error backend: ${response.code()}")
                    }
                } catch (e: Exception) {
                    _createSolicitudState.value = UiState.Error("Guardado offline.")
                }
            } catch (e: Exception) {
                _createSolicitudState.value = UiState.Error(message = "Error: ${e.message}")
            }
        }
    }

    /**
     * Crea solicitud de EGRESO
     */
    fun solicitarEgreso(codigoBarras: String, cantidad: Int, ubicacionOrigen: String, motivo: String) {
        viewModelScope.launch {
            try {
                _createSolicitudState.value = UiState.Loading

                val idOrigen = ubicacionService.getIdUbicacionByCodigo(ubicacionOrigen)
                if (idOrigen == null) {
                    _createSolicitudState.value = UiState.Error(message = "Ubicación origen no válida")
                    return@launch
                }

                val idSolicitante = getCurrentUserId()

                val solicitudLocal = SolicitudMovimientoLocal(
                    tipoMovimiento = "EGRESO",
                    codigoBarras = codigoBarras,
                    cantidad = cantidad,
                    motivo = motivo,
                    idUbicacionOrigen = idOrigen,
                    idUbicacionDestino = null,
                    timestamp = System.currentTimeMillis()
                )
                val localId = solicitudMovimientoDao.insertarSolicitud(solicitudLocal)

                try {
                    val request = NetworkAprobacionRequest(
                        tipoMovimiento = "EGRESO",
                        codigoBarras = codigoBarras,
                        cantidad = cantidad,
                        motivo = motivo,
                        idUbicacionOrigen = idOrigen,
                        idUbicacionDestino = null,
                        idSolicitante = idSolicitante
                    )
                    val response = apiService.createAprobacion(request)
                    
                    if (response.isSuccessful) {
                        solicitudMovimientoDao.deleteById(localId)
                        _createSolicitudState.value = UiState.Success(true)
                    } else {
                        _createSolicitudState.value = UiState.Error("Error backend: ${response.code()}")
                    }
                } catch (e: Exception) {
                    _createSolicitudState.value = UiState.Error("Guardado offline.")
                }
            } catch (e: Exception) {
                _createSolicitudState.value = UiState.Error(message = "Error: ${e.message}")
            }
        }
    }

    /**
     * Crea solicitud de REUBICACION
     */
    fun solicitarReubicacion(codigoBarras: String, cantidad: Int, ubicacionOrigen: String, ubicacionDestino: String, motivo: String) {
        viewModelScope.launch {
            try {
                _createSolicitudState.value = UiState.Loading

                val idOrigen = ubicacionService.getIdUbicacionByCodigo(ubicacionOrigen)
                val idDestino = ubicacionService.getIdUbicacionByCodigo(ubicacionDestino)

                if (idOrigen == null || idDestino == null) {
                    _createSolicitudState.value = UiState.Error(message = "Ubicaciones no válidas")
                    return@launch
                }

                val idSolicitante = getCurrentUserId()

                val solicitudLocal = SolicitudMovimientoLocal(
                    tipoMovimiento = "REUBICACION",
                    codigoBarras = codigoBarras,
                    cantidad = cantidad,
                    motivo = motivo,
                    idUbicacionOrigen = idOrigen,
                    idUbicacionDestino = idDestino,
                    timestamp = System.currentTimeMillis()
                )
                val localId = solicitudMovimientoDao.insertarSolicitud(solicitudLocal)

                try {
                    val request = NetworkAprobacionRequest(
                        tipoMovimiento = "REUBICACION",
                        codigoBarras = codigoBarras,
                        cantidad = cantidad,
                        motivo = motivo,
                        idUbicacionOrigen = idOrigen,
                        idUbicacionDestino = idDestino,
                        idSolicitante = idSolicitante
                    )
                    val response = apiService.createAprobacion(request)
                    
                    if (response.isSuccessful) {
                        solicitudMovimientoDao.deleteById(localId)
                        _createSolicitudState.value = UiState.Success(true)
                    } else {
                        _createSolicitudState.value = UiState.Error("Error backend: ${response.code()}")
                    }
                } catch (e: Exception) {
                    _createSolicitudState.value = UiState.Error("Guardado offline.")
                }
            } catch (e: Exception) {
                _createSolicitudState.value = UiState.Error(message = "Error: ${e.message}")
            }
        }
    }

    /**
     * Rechaza una solicitud
     */
    fun rechazarSolicitud(id: Int, observaciones: String) {
        viewModelScope.launch {
            try {
                _respuestaState.value = UiState.Loading

                val request = RechazarRequest(observaciones = observaciones)
                val response = apiService.rechazarSolicitud(id, request)
                
                if (response.isSuccessful) {
                    _respuestaState.value = UiState.Success(true)
                    getAllAprobaciones()
                } else {
                    _respuestaState.value = UiState.Error(message = "Error backend: ${response.code()}")
                }
            } catch (e: Exception) {
                _respuestaState.value = UiState.Error(message = "Error al rechazar: ${e.message}")
            }
        }
    }

    // ========== UTILIDADES ==========

    fun clearAprobaciones() {
        _aprobacionesState.value = UiState.Idle
        _estadoFiltro.value = null
    }

    fun clearSelectedAprobacion() {
        _selectedAprobacion.value = null
        _aprobacionDetailState.value = UiState.Idle
    }

    fun clearCreateState() {
        _createSolicitudState.value = UiState.Idle
    }

    fun clearRespuestaState() {
        _respuestaState.value = UiState.Idle
    }

    fun clearEstadoFilter() {
        _estadoFiltro.value = null
        getAllAprobaciones()
    }
}


