package com.smartlogix.app.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartlogix.app.db.AppDatabase
import com.smartlogix.app.models.*
import com.smartlogix.app.repository.UbicacionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para gestión de ubicaciones en bodega
 * Maneja ubicaciones, asignación de productos y consultas por piso y pasillo
 * Usa UbicacionRepository con patrón local-first
 * Actualizado para soportar 5 pasillos × 60 posiciones × 3 pisos = 900 ubicaciones
 */
class UbicacionViewModel(
    application: Application,
    private val ubicacionRepository: UbicacionRepository
) : AndroidViewModel(application) {

    // ========== ESTADOS DE UBICACIONES ==========

    private val _ubicacionesState = MutableStateFlow<UiState<List<Ubicacion>>>(UiState.Idle)
    val ubicacionesState: StateFlow<UiState<List<Ubicacion>>> = _ubicacionesState.asStateFlow()

    private val _selectedUbicacion = MutableStateFlow<Ubicacion?>(null)
    val selectedUbicacion: StateFlow<Ubicacion?> = _selectedUbicacion.asStateFlow()

    private val _ubicacionDetailState = MutableStateFlow<UiState<Ubicacion>>(UiState.Idle)
    val ubicacionDetailState: StateFlow<UiState<Ubicacion>> = _ubicacionDetailState.asStateFlow()

    private val _asignacionState = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val asignacionState: StateFlow<UiState<Boolean>> = _asignacionState.asStateFlow()

    private val _pisoSeleccionado = MutableStateFlow<Piso?>(null)
    val pisoSeleccionado: StateFlow<Piso?> = _pisoSeleccionado.asStateFlow()

    private val _pasilloSeleccionado = MutableStateFlow<Pasillo?>(null)
    val pasilloSeleccionado: StateFlow<Pasillo?> = _pasilloSeleccionado.asStateFlow()

    private val _esEstanteSolo = MutableStateFlow(false)
    val esEstanteSolo: StateFlow<Boolean> = _esEstanteSolo.asStateFlow()

    // ========== CONSULTA DE UBICACIONES ==========

    /**
     * Obtiene todas las ubicaciones de la bodega
     * Conecta con: GET /api/ubicaciones
     */
    fun getAllUbicaciones(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                _ubicacionesState.value = UiState.Loading

                val result = ubicacionRepository.getUbicaciones(
                    piso = null, 
                    pasillo = null, 
                    esEstante = if (_esEstanteSolo.value) true else null,
                    forceRefresh = forceRefresh
                )
                
                _ubicacionesState.value = if (result.isSuccess) {
                    UiState.Success(result.getOrNull() ?: emptyList())
                } else {
                    UiState.Error(
                        message = result.exceptionOrNull()?.message ?: "Error al obtener ubicaciones"
                    )
                }

            } catch (e: Throwable) {
                _ubicacionesState.value = UiState.Error(
                    message = "Error al obtener ubicaciones: ${e.message}"
                )
            }
        }
    }

    /**
     * Alterna el filtro de solo estantes
     */
    fun toggleSoloEstantes() {
        _esEstanteSolo.value = !_esEstanteSolo.value
        refreshUbicaciones()
    }

    private fun refreshUbicaciones() {
        when {
            _pasilloSeleccionado.value != null && _pisoSeleccionado.value != null -> {
                getUbicacionesByPasilloYPiso(_pasilloSeleccionado.value!!, _pisoSeleccionado.value!!)
            }
            _pasilloSeleccionado.value != null -> {
                getUbicacionesByPasillo(_pasilloSeleccionado.value!!)
            }
            _pisoSeleccionado.value != null -> {
                getUbicacionesByPiso(_pisoSeleccionado.value!!)
            }
            else -> {
                getAllUbicaciones()
            }
        }
    }

    /**
     * Obtiene ubicaciones filtradas por piso
     * Conecta con: GET /api/ubicaciones?piso={A|B|C}
     * @param piso Piso a filtrar (A, B o C)
     */
    fun getUbicacionesByPiso(piso: Piso, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                _ubicacionesState.value = UiState.Loading
                _pisoSeleccionado.value = piso

                val result = ubicacionRepository.getUbicaciones(
                    piso = piso.codigo.toString(),
                    pasillo = _pasilloSeleccionado.value?.numero,
                    esEstante = if (_esEstanteSolo.value) true else null,
                    forceRefresh = forceRefresh
                )
                
                _ubicacionesState.value = if (result.isSuccess) {
                    UiState.Success(result.getOrNull() ?: emptyList())
                } else {
                    UiState.Error(
                        message = result.exceptionOrNull()?.message ?: "Error al obtener ubicaciones"
                    )
                }

            } catch (e: Throwable) {
                _ubicacionesState.value = UiState.Error(
                    message = "Error al obtener ubicaciones del piso ${piso.codigo}: ${e.message}"
                )
            }
        }
    }

    /**
     * Obtiene ubicaciones filtradas por pasillo
     * Conecta con: GET /api/ubicaciones?pasillo={1|2|3|4|5}
     * @param pasillo Pasillo a filtrar (1-5)
     */
    fun getUbicacionesByPasillo(pasillo: Pasillo, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                _ubicacionesState.value = UiState.Loading
                _pasilloSeleccionado.value = pasillo

                val result = ubicacionRepository.getUbicaciones(
                    piso = _pisoSeleccionado.value?.codigo?.toString(),
                    pasillo = pasillo.numero,
                    esEstante = if (_esEstanteSolo.value) true else null,
                    forceRefresh = forceRefresh
                )
                
                _ubicacionesState.value = if (result.isSuccess) {
                    UiState.Success(result.getOrNull() ?: emptyList())
                } else {
                    UiState.Error(
                        message = result.exceptionOrNull()?.message ?: "Error al obtener ubicaciones"
                    )
                }

            } catch (e: Throwable) {
                _ubicacionesState.value = UiState.Error(
                    message = "Error al obtener ubicaciones del pasillo ${pasillo.numero}: ${e.message}"
                )
            }
        }
    }

    /**
     * Obtiene ubicaciones filtradas por pasillo y piso
     * Conecta con: GET /api/ubicaciones?pasillo={1-5}&piso={A|B|C}
     */
    fun getUbicacionesByPasilloYPiso(pasillo: Pasillo, piso: Piso, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                _ubicacionesState.value = UiState.Loading
                _pasilloSeleccionado.value = pasillo
                _pisoSeleccionado.value = piso

                val result = ubicacionRepository.getUbicaciones(
                    piso = piso.codigo.toString(),
                    pasillo = pasillo.numero,
                    esEstante = if (_esEstanteSolo.value) true else null,
                    forceRefresh = forceRefresh
                )
                
                _ubicacionesState.value = if (result.isSuccess) {
                    UiState.Success(result.getOrNull() ?: emptyList())
                } else {
                    UiState.Error(
                        message = result.exceptionOrNull()?.message ?: "Error al obtener ubicaciones"
                    )
                }

            } catch (e: Throwable) {
                _ubicacionesState.value = UiState.Error(
                    message = "Error al obtener ubicaciones del pasillo ${pasillo.numero} piso ${piso.codigo}: ${e.message}"
                )
            }
        }
    }

    /**
     * Obtiene el detalle de una ubicación específica por ID
     * Conecta con: GET /api/ubicaciones/id/{id}
     * @param id ID numérico de la ubicación
     */
    fun getUbicacionDetailById(id: Int) {
        viewModelScope.launch {
            try {
                _ubicacionDetailState.value = UiState.Loading

                val result = ubicacionRepository.getUbicacionById(id)
                
                if (result.isSuccess) {
                    val ubicacion = result.getOrNull()!!
                    _selectedUbicacion.value = ubicacion
                    _ubicacionDetailState.value = UiState.Success(ubicacion)
                } else {
                    _ubicacionDetailState.value = UiState.Error(
                        message = result.exceptionOrNull()?.message ?: "Error al obtener ubicación"
                    )
                }

            } catch (e: Exception) {
                _ubicacionDetailState.value = UiState.Error(
                    message = "Error al obtener ubicación: ${e.message}"
                )
            }
        }
    }

    /**
     * Obtiene el detalle de una ubicación específica por código
     * Conecta con: GET /api/ubicaciones/{codigo}
     * @param codigo Código de la ubicación (formato: P1-A-12, P3-B-45)
     */
    fun getUbicacionDetail(codigo: String) {
        viewModelScope.launch {
            try {
                _ubicacionDetailState.value = UiState.Loading

                val result = ubicacionRepository.getUbicacionByCodigo(codigo)
                
                if (result.isSuccess) {
                    val ubicacion = result.getOrNull()!!
                    _selectedUbicacion.value = ubicacion
                    _ubicacionDetailState.value = UiState.Success(ubicacion)
                } else {
                    _ubicacionDetailState.value = UiState.Error(
                        message = result.exceptionOrNull()?.message ?: "Error al obtener ubicación"
                    )
                }

            } catch (e: Exception) {
                _ubicacionDetailState.value = UiState.Error(
                    message = "Error al obtener ubicación: ${e.message}"
                )
            }
        }
    }

    // ========== ASIGNACIÓN DE PRODUCTOS ==========

    /**
     * Asigna un producto a una ubicación
     * Conecta con: POST /api/ubicaciones/asignar
     * Usa patrón local-first: guarda local → envía backend → elimina si OK
     * @param sku SKU del producto
     * @param codigoUbicacion Código de la ubicación (formato: P1-A-12)
     * @param cantidad Cantidad a asignar
     */
    fun asignarProducto(
        productoViewModel: ProductoViewModel,
        codigoBarras: String,
        codigoUbicacion: String,
        cantidad: Int
    ){
        viewModelScope.launch {
            try {
                _asignacionState.value = UiState.Loading

                val result = ubicacionRepository.asignarProducto(codigoBarras, codigoUbicacion, cantidad)
                
                if (result.isSuccess) {
                    _asignacionState.value = UiState.Success(true)
                    // Notificamos al otro ViewModel que debe actualizarse.
                    productoViewModel.refreshProductoDetail()
                    // Recargar detalle de la ubicación si está seleccionada
                    if (_selectedUbicacion.value?.codigoUbicacion == codigoUbicacion) {
                        getUbicacionDetail(codigoUbicacion)
                    }
                } else {
                    // Puede estar guardado localmente aunque falle el backend
                    _asignacionState.value = UiState.Error(
                        message = result.exceptionOrNull()?.message ?: "Error al asignar producto"
                    )
                }

            } catch (e: Exception) {
                _asignacionState.value = UiState.Error(
                    message = "Error al asignar producto: ${e.message}"
                )
            }
        }
    }

    /**
     * Asigna un producto a múltiples ubicaciones
     * @param sku SKU del producto
     * @param asignaciones Lista de pares (codigoUbicacion, cantidad)
     */
    fun asignarProductoMultiple(codigoBarras: String, asignaciones: List<Pair<String, Int>>) {
        viewModelScope.launch {
            try {
                _asignacionState.value = UiState.Loading

                var successCount = 0
                var errorCount = 0

                asignaciones.forEach { (codigo, cantidad) ->
                    val result = ubicacionRepository.asignarProducto(codigoBarras, codigo, cantidad)
                    if (result.isSuccess) {
                        successCount++
                    } else {
                        errorCount++
                    }
                }

                _asignacionState.value = if (errorCount == 0) {
                    UiState.Success(true)
                } else if (successCount > 0) {
                    UiState.Error("$successCount asignaciones exitosas, $errorCount fallidas")
                } else {
                    UiState.Error("Error al asignar producto a las ubicaciones")
                }

            } catch (e: Exception) {
                _asignacionState.value = UiState.Error(
                    message = "Error al asignar producto: ${e.message}"
                )
            }
        }
    }

    // ========== SINCRONIZACIÓN ==========

    /**
     * Sincroniza todas las asignaciones pendientes con el backend
     * Útil para sincronizar después de recuperar conexión
     */
    fun syncAsignacionesPendientes() {
        viewModelScope.launch {
            try {
                val result = ubicacionRepository.syncAsignacionesPendientes()
                
                if (result.isSuccess) {
                    val syncCount = result.getOrNull() ?: 0
                    // Recargar ubicaciones si hay cambios
                    if (syncCount > 0) {
                        when {
                            _pasilloSeleccionado.value != null && _pisoSeleccionado.value != null -> {
                                getUbicacionesByPasilloYPiso(_pasilloSeleccionado.value!!, _pisoSeleccionado.value!!)
                            }
                            _pasilloSeleccionado.value != null -> {
                                getUbicacionesByPasillo(_pasilloSeleccionado.value!!)
                            }
                            _pisoSeleccionado.value != null -> {
                                getUbicacionesByPiso(_pisoSeleccionado.value!!)
                            }
                            else -> {
                                getAllUbicaciones()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Error silencioso, se reintentará después
            }
        }
    }

    // ========== UTILIDADES ==========

    /**
     * Limpia el estado de ubicaciones
     */
    fun clearUbicaciones() {
        _ubicacionesState.value = UiState.Idle
        _pisoSeleccionado.value = null
        _pasilloSeleccionado.value = null
    }

    /**
     * Limpia la ubicación seleccionada
     */
    fun clearSelectedUbicacion() {
        _selectedUbicacion.value = null
        _ubicacionDetailState.value = UiState.Idle
    }

    /**
     * Limpia el estado de asignación
     */
    fun clearAsignacionState() {
        _asignacionState.value = UiState.Idle
    }

    /**
     * Limpia el filtro de piso y muestra todas las ubicaciones
     */
    fun clearPisoFilter() {
        _pisoSeleccionado.value = null
        // Mantener filtro de pasillo si existe
        if (_pasilloSeleccionado.value != null) {
            getUbicacionesByPasillo(_pasilloSeleccionado.value!!)
        } else {
            getAllUbicaciones()
        }
    }

    /**
     * Limpia el filtro de pasillo y muestra todas las ubicaciones
     */
    fun clearPasilloFilter() {
        _pasilloSeleccionado.value = null
        // Mantener filtro de piso si existe
        if (_pisoSeleccionado.value != null) {
            getUbicacionesByPiso(_pisoSeleccionado.value!!)
        } else {
            getAllUbicaciones()
        }
    }

    /**
     * Limpia todos los filtros y muestra todas las ubicaciones
     */
    fun clearAllFilters() {
        _pisoSeleccionado.value = null
        _pasilloSeleccionado.value = null
        getAllUbicaciones()
    }

    /**
     * Valida el formato de un código de ubicación
     * @param codigo Código a validar (formato: P1-A-12, P3-B-45, P5-C-60)
     * @return true si el formato es válido
     */
    fun isCodigoValido(codigo: String): Boolean {
        val regex = Regex("""^P[1-5]-[ABC]-([0-5]?[0-9]|60)$""")
        return regex.matches(codigo)
    }

    /**
     * Genera un código de ubicación a partir de pasillo, piso y número
     * @param pasillo Pasillo (1-5)
     * @param piso Piso (A, B o C)
     * @param numero Número (1-60)
     * @return Código en formato P1-A-12, P3-B-05, etc.
     */
    fun generarCodigo(pasillo: Int, piso: Char, numero: Int): String {
        return UbicacionFormatter.formatCodigo(pasillo, piso, numero)
    }
}


