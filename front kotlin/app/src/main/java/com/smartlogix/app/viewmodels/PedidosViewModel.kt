package com.smartlogix.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.smartlogix.app.model.*
import com.smartlogix.app.models.UiState
import com.smartlogix.app.repository.PedidosRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class PedidosViewModel(
    private val repository: PedidosRepository
) : ViewModel() {

    // Lista de pedidos
    private val _pedidosState = MutableStateFlow<UiState<List<PedidoResponse>>>(UiState.Idle)
    val pedidosState: StateFlow<UiState<List<PedidoResponse>>> = _pedidosState.asStateFlow()

    private val _pedidoDetalleState = MutableStateFlow<UiState<PedidoResponse>>(UiState.Idle)
    val pedidoDetalleState: StateFlow<UiState<PedidoResponse>> = _pedidoDetalleState.asStateFlow()

    // Detalle COMPLETÍSIMO (Trazabilidad, Historial, Responsables)
    private val _pedidoDetalleCompletoState = MutableStateFlow<UiState<PedidoDetalleDTO>>(UiState.Idle)
    val pedidoDetalleCompletoState: StateFlow<UiState<PedidoDetalleDTO>> = _pedidoDetalleCompletoState.asStateFlow()

    // Estado para acciones de picking (confirmar item, asignar, etc)
    private val _actionState = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val actionState: StateFlow<UiState<Boolean>> = _actionState.asStateFlow()

    // Estadísticas
    private val _statsState = MutableStateFlow<UiState<PedidoStats>>(UiState.Idle)
    val statsState: StateFlow<UiState<PedidoStats>> = _statsState.asStateFlow()

    fun getPedidosPendientes() {
        viewModelScope.launch {
            _pedidosState.value = UiState.Loading
            repository.getPedidosPendientes()
                .onSuccess {
                    _pedidosState.value = UiState.Success(it)
                }
                .onFailure {
                    _pedidosState.value = UiState.Error(it.message ?: "Error desconocido")
                }
        }
    }

    fun getPedidosEnPicking() {
        viewModelScope.launch {
            _pedidosState.value = UiState.Loading
            repository.getPedidosEnPicking()
                .onSuccess {
                    _pedidosState.value = UiState.Success(it)
                }
                .onFailure {
                    _pedidosState.value = UiState.Error(it.message ?: "Error desconocido")
                }
        }
    }

    fun getPedidosEntregados() {
        viewModelScope.launch {
            _pedidosState.value = UiState.Loading
            repository.getPedidosEntregados()
                .onSuccess {
                    _pedidosState.value = UiState.Success(it)
                }
                .onFailure {
                    _pedidosState.value = UiState.Error(it.message ?: "Error desconocido")
                }
        }
    }

    fun getPedidosFacturados() {
        viewModelScope.launch {
            _pedidosState.value = UiState.Loading
            // El usuario ya no quiere ver los entregados aquí, solo los pendientes de revisión (FACTURADO)
            repository.getPedidosTodos()
                .onSuccess { todos ->
                    val filtrados = todos.filter { 
                        it.estado == EstadoPedido.FACTURADO 
                    }
                    _pedidosState.value = UiState.Success(filtrados)
                }
                .onFailure { _pedidosState.value = UiState.Error(it.message ?: "Error al obtener lista de revisión") }
        }
    }

    fun revisarPedido(id: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            repository.revisionFactura(id)
                .onSuccess { _: Unit ->
                    _actionState.value = UiState.Success(true)
                    getPedidosFacturados() // Refresh list for Jefe
                    onSuccess()
                }
                .onFailure { t: Throwable ->
                    _actionState.value = UiState.Error(t.message ?: "Error al revisar pedido")
                }
        }
    }

    fun getPedidosTodos() {
        viewModelScope.launch {
            _pedidosState.value = UiState.Loading
            repository.getPedidosTodos()
                .onSuccess {
                    _pedidosState.value = UiState.Success(it)
                }
                .onFailure {
                    _pedidosState.value = UiState.Error(it.message ?: "Error desconocido")
                }
        }
    }

    fun getPedidoDetalle(id: Int) {
        viewModelScope.launch {
            _pedidoDetalleState.value = UiState.Loading
            
            // El backend no tiene GET /api/pedidos/{id}, así que buscamos en la lista global 
            // para obtener los metadatos completos (operadorId, nombre, etc.)
            repository.getPedidosTodos()
                .onSuccess { todos ->
                    val pedidoMetadata = todos.find { it.id == id }
                    
                    // Luego pedimos la hoja de picking para tener los productos y ubicaciones
                    repository.getHojaPicking(id)
                        .onSuccess { pickingDetail ->
                            // Combinamos: Metadatos de la lista (dueño, estado real) + Items de la hoja
                            val merged = (pedidoMetadata ?: pickingDetail).copy(
                                detalles = pickingDetail.detalles,
                                operadorId = pedidoMetadata?.operadorId ?: pickingDetail.operadorId,
                                operadorNombre = pedidoMetadata?.operadorNombre ?: pickingDetail.operadorNombre
                            )
                            _pedidoDetalleState.value = UiState.Success(merged)
                        }
                        .onFailure { pickingError ->
                            // Si falla la hoja pero tenemos la metadata, mostramos lo que hay
                            if (pedidoMetadata != null) {
                                _pedidoDetalleState.value = UiState.Success(pedidoMetadata)
                            } else {
                                _pedidoDetalleState.value = UiState.Error(pickingError.message ?: "Error al obtener detalle")
                            }
                        }
                }
                .onFailure { listError ->
                    // Si falla el listado, intentamos al menos la hoja como último recurso
                    repository.getHojaPicking(id)
                        .onSuccess { _pedidoDetalleState.value = UiState.Success(it) }
                        .onFailure { _pedidoDetalleState.value = UiState.Error(listError.message ?: "Error de conexión") }
                }
        }
    }

    /**
     * Obtiene el detalle completo (trazabilidad) del pedido
     */
    fun getPedidoDetalleCompleto(id: Long) {
        viewModelScope.launch {
            _pedidoDetalleCompletoState.value = UiState.Loading
            repository.getPedidoDetalleCompleto(id)
                .onSuccess {
                    _pedidoDetalleCompletoState.value = UiState.Success(it)
                }
                .onFailure {
                    _pedidoDetalleCompletoState.value = UiState.Error(it.message ?: "Error al obtener trazabilidad")
                }
        }
    }

    fun asignarPicking(id: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            repository.asignarPicking(id)
                .onSuccess {
                    _actionState.value = UiState.Success(true)
                    getPedidoDetalle(id)
                    onSuccess()
                }
                .onFailure {
                    _actionState.value = UiState.Error(it.message ?: "Error al asignar picking")
                }
        }
    }

    fun confirmarItem(id: Int, sku: String, cantidad: Int, codigoUbicacion: String) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            repository.confirmarItem(id, sku, cantidad, codigoUbicacion)
                .onSuccess {
                    _actionState.value = UiState.Success(true)
                    // Recargamos el detalle completo para asegurar que el progreso y los items estén actualizados
                    getPedidoDetalle(id)
                }
                .onFailure {
                    _actionState.value = UiState.Error(it.message ?: "Error al confirmar item")
                }
        }
    }

    fun completarPicking(id: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            repository.completarPicking(id)
                .onSuccess {
                    _actionState.value = UiState.Success(true)
                    getPedidoDetalle(id)
                    onSuccess()
                }
                .onFailure {
                    _actionState.value = UiState.Error(it.message ?: "Error al completar picking")
                }
        }
    }


    fun asignarTransporte(id: Int, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            repository.asignarTransporte(id)
                .onSuccess {
                    _actionState.value = UiState.Success(true)
                    getPedidoDetalle(id)
                    onSuccess()
                }
                .onFailure {
                    _actionState.value = UiState.Error(it.message ?: "Error al asignar transporte")
                }
        }
    }

    fun iniciarTransporte(id: Int) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            repository.iniciarTransporte(id)
                .onSuccess {
                    _actionState.value = UiState.Success(true)
                    getPedidoDetalle(id)
                }
                .onFailure {
                    _actionState.value = UiState.Error(it.message ?: "Error al iniciar transporte")
                }
        }
    }

    fun finalizarEntrega(id: Int, fotoBase64: String) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            repository.finalizarEntrega(id, fotoBase64)
                .onSuccess {
                    _actionState.value = UiState.Success(true)
                    getPedidoDetalle(id)
                }
                .onFailure {
                    _actionState.value = UiState.Error(it.message ?: "Error al finalizar entrega")
                }
        }
    }

    fun finalizarEntrega(id: Int, foto: File) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            try {
                // Comprimir imagen antes de enviar
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = false
                    inSampleSize = 2 // Reducir a la mitad el tamaño si es necesario (opcional)
                }
                val bitmap = BitmapFactory.decodeFile(foto.absolutePath, options)
                val outputStream = java.io.ByteArrayOutputStream()
                
                // Comprimir a JPEG con 80% de calidad
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                val compressedBytes = outputStream.toByteArray()
                
                val base64 = android.util.Base64.encodeToString(compressedBytes, android.util.Base64.NO_WRAP)
                
                repository.finalizarEntrega(id, base64)
                    .onSuccess {
                        _actionState.value = UiState.Success(true)
                        getPedidoDetalle(id)
                    }
                    .onFailure {
                        _actionState.value = UiState.Error(it.message ?: "Error al finalizar entrega")
                    }
            } catch (e: Exception) {
                _actionState.value = UiState.Error("Error al procesar imagen: ${e.message}")
            }
        }
    }

    fun getStats() {
        viewModelScope.launch {
            _statsState.value = UiState.Loading
            repository.getEstadisticas()
                .onSuccess {
                    _statsState.value = UiState.Success(it)
                }
                .onFailure {
                    _statsState.value = UiState.Error(it.message ?: "Error al obtener estadísticas")
                }
        }
    }

    fun resetActionState() {
        _actionState.value = UiState.Idle
    }
}


