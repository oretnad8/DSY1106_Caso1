package com.smartlogix.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartlogix.app.models.*
import com.smartlogix.app.network.ProductoRequest
import com.smartlogix.app.repository.ProductoRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para gestión de productos
 * Maneja búsqueda, detalle, creación y actualización de productos
 * Usa ProductoRepository con patrón local-first
 */
/**
 * Eventos de navegación unidireccionales
 */
sealed class ProductoNavEvent {
    data class NavigateToDetail(val sku: String) : ProductoNavEvent()
}

class ProductoViewModel(
    private val productoRepository: ProductoRepository
) : ViewModel() {

    // ========== ESTADOS DE PRODUCTOS ==========

    private val _searchState = MutableStateFlow<UiState<List<Producto>>>(UiState.Idle)
    val searchState: StateFlow<UiState<List<Producto>>> = _searchState.asStateFlow()

    private val _selectedProducto = MutableStateFlow<Producto?>(null)
    val selectedProducto: StateFlow<Producto?> = _selectedProducto.asStateFlow()

    private val _productoDetailState = MutableStateFlow<UiState<Producto>>(UiState.Idle)
    val productoDetailState: StateFlow<UiState<Producto>> = _productoDetailState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _createState = MutableStateFlow<UiState<Producto>>(UiState.Idle)
    val createState: StateFlow<UiState<Producto>> = _createState.asStateFlow()

    private val _updateState = MutableStateFlow<UiState<Producto>>(UiState.Idle)
    val updateState: StateFlow<UiState<Producto>> = _updateState.asStateFlow()

    private val _deleteState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val deleteState: StateFlow<UiState<Unit>> = _deleteState.asStateFlow()

    // ========== EVENTOS DE NAVEGACIÓN ==========
    private val _navigationEvent = MutableSharedFlow<ProductoNavEvent>()
    val navigationEvent: SharedFlow<ProductoNavEvent> = _navigationEvent.asSharedFlow()

    // ========== BÚSQUEDA DE PRODUCTOS ==========

    /**
     * Busca productos por texto
     * Conecta con: GET /api/productos/search?q={query}
     * Busca por: SKU, descripción, código de barras, LPN, ubicación
     * @param query Texto de búsqueda (opcional, si está vacío devuelve todos)
     */
    fun searchProductos(query: String = "") {
        viewModelScope.launch {
            try {
                val trimmedQuery = query.trim()
                _searchState.value = UiState.Loading
                _searchQuery.value = trimmedQuery

                val result = productoRepository.searchProductos(trimmedQuery.ifBlank { null })
                
                _searchState.value = if (result.isSuccess) {
                    UiState.Success(result.getOrNull()!!)
                } else {
                    UiState.Error(
                        message = result.exceptionOrNull()?.message ?: "Error al buscar productos"
                    )
                }

            } catch (e: Exception) {
                _searchState.value = UiState.Error(
                    message = "Error al buscar productos: ${e.message}"
                )
            }
        }
    }

    /**
     * Busca un producto específico por código de barras o LPN.
     * Si encuentra un resultado único, obtiene el detalle completo del producto.
     * Si encuentra múltiples resultados o ninguno, actualiza el estado de búsqueda con la lista.
     * Usado cuando se escanea con la cámara.
     * @param codigo Código de barras o LPN escaneado.
     */
    fun searchByBarcode(codigo: String) {
        viewModelScope.launch {
            try {
                val trimmedCodigo = codigo.trim()
                // Inicia estados a Loading/Idle para evitar estados inconsistentes en la UI
                _searchState.value = UiState.Loading
                _productoDetailState.value = UiState.Idle
                _searchQuery.value = trimmedCodigo

                val searchResult = productoRepository.searchProductos(trimmedCodigo)

                if (searchResult.isSuccess) {
                    val products = searchResult.getOrNull()!!
                    if (products.size == 1) {
                        // Resultado único: obtenemos el detalle completo.
                        _productoDetailState.value = UiState.Loading
                        val sku = products.first().sku
                        val detailResult = productoRepository.getProductoBySku(sku)

                        if (detailResult.isSuccess) {
                            val producto = detailResult.getOrNull()!!
                            _selectedProducto.value = producto
                            _productoDetailState.value = UiState.Success(producto)
                            
                            // Emitimos el evento de navegación en lugar de solo cambiar el estado
                            _navigationEvent.emit(ProductoNavEvent.NavigateToDetail(sku))
                            
                            // Limpiamos el estado de búsqueda de lista.
                            _searchState.value = UiState.Idle
                        } else {
                            // Error al obtener el detalle, lo reflejamos en el estado de detalle.
                            _productoDetailState.value = UiState.Error(
                                message = detailResult.exceptionOrNull()?.message ?: "Error al obtener detalle del producto"
                            )
                            _searchState.value = UiState.Idle
                        }
                    } else {
                        // Múltiples o ningún resultado: mostramos la lista.
                        _searchState.value = UiState.Success(products)
                        _productoDetailState.value = UiState.Idle
                    }
                } else {
                    // Error en la búsqueda inicial.
                    _searchState.value = UiState.Error(
                        message = searchResult.exceptionOrNull()?.message ?: "Error al buscar productos"
                    )
                    _productoDetailState.value = UiState.Idle
                }
            } catch (e: Exception) {
                // Error inesperado durante el proceso.
                _searchState.value = UiState.Error(message = "Error en la búsqueda por código: ${e.message}")
                _productoDetailState.value = UiState.Idle
            }
        }
    }

    /**
     * Obtiene el detalle completo de un producto por SKU
     * Conecta con: GET /api/productos/{sku}
     * @param sku SKU del producto
     */
    fun getProductoDetail(sku: String) {
        viewModelScope.launch {
            try {
                val trimmedSku = sku.trim()
                _productoDetailState.value = UiState.Loading

                val result = productoRepository.getProductoBySku(trimmedSku)

                if (result.isSuccess) {
                    val producto = result.getOrNull()!!
                    _selectedProducto.value = producto
                    _productoDetailState.value = UiState.Success(producto)
                } else {
                    _productoDetailState.value = UiState.Error(
                        message = result.exceptionOrNull()?.message ?: "Error al obtener producto"
                    )
                }

            } catch (e: Exception) {
                _productoDetailState.value = UiState.Error(
                    message = "Error al obtener producto: ${e.message}"
                )
            }
        }
    }

    // ========== FUNCIÓN NUEVA AÑADIDA ==========

    /**
     * Fuerza la recarga del detalle del producto actualmente seleccionado.
     * Es útil para llamar desde fuera del ViewModel (ej. desde un UbicacionViewModel)
     * después de una acción que modifica el producto indirectamente (como asignar una ubicación).
     */
    fun refreshProductoDetail() {
        // Obtenemos el SKU del producto que ya está cargado
        val currentSku = _selectedProducto.value?.sku
        if (currentSku != null) {
            // Reutilizamos la lógica que ya teníamos
            getProductoDetail(currentSku)
        }
    }

    // ========== GESTIÓN DE PRODUCTOS (ADMIN/JEFE) ==========

    /**
     * Crea un nuevo producto
     * Conecta con: POST /api/productos
     * Solo para ADMIN, JEFE, SUPERVISOR
     * Usa patrón local-first: guarda local → envía backend → elimina si OK
     * @param request Datos del nuevo producto
     */
    fun createProducto(request: ProductoRequest) {
        viewModelScope.launch {
            try {
                _createState.value = UiState.Loading

                val result = productoRepository.createProducto(request)
                
                if (result.isSuccess) {
                    _createState.value = UiState.Success(result.getOrNull()!!)
                    // Recargar lista de productos después de crear
                    searchProductos(_searchQuery.value)
                } else {
                    // Puede estar guardado localmente aunque falle el backend
                    _createState.value = UiState.Error(
                        message = result.exceptionOrNull()?.message ?: "Error al crear producto"
                    )
                }

            } catch (e: Exception) {
                _createState.value = UiState.Error(
                    message = "Error al crear producto: ${e.message}"
                )
            }
        }
    }

    /**
     * Actualiza un producto existente
     * Conecta con: PUT /api/productos/{sku}
     * Solo para ADMIN, JEFE, SUPERVISOR
     * Usa patrón local-first: guarda local → envía backend → elimina si OK
     * @param sku SKU del producto a actualizar
     * @param request Datos actualizados
     */
    fun updateProducto(sku: String, request: ProductoRequest) {
        viewModelScope.launch {
            try {
                _updateState.value = UiState.Loading

                val result = productoRepository.updateProducto(sku, request)
                
                if (result.isSuccess) {
                    _updateState.value = UiState.Success(result.getOrNull()!!)
                    // Recargar detalle del producto
                    getProductoDetail(sku)
                } else {
                    // Puede estar guardado localmente aunque falle el backend
                    _updateState.value = UiState.Error(
                        message = result.exceptionOrNull()?.message ?: "Error al actualizar producto"
                    )
                }

            } catch (e: Exception) {
                _updateState.value = UiState.Error(
                    message = "Error al actualizar producto: ${e.message}"
                )
            }
        }
    }

    /**
     * Elimina un producto
     * Conecta con: DELETE /api/productos/{sku}
     * Solo para ADMIN, JEFE
     * @param sku SKU del producto a eliminar
     */
    fun deleteProducto(sku: String) {
        viewModelScope.launch {
            try {
                _deleteState.value = UiState.Loading

                val result = productoRepository.deleteProducto(sku)
                
                if (result.isSuccess) {
                    _deleteState.value = UiState.Success(Unit)
                    // Recargar lista después de eliminar
                    searchProductos(_searchQuery.value)
                } else {
                    _deleteState.value = UiState.Error(
                        message = result.exceptionOrNull()?.message ?: "Error al eliminar producto"
                    )
                }

            } catch (e: Exception) {
                _deleteState.value = UiState.Error(
                    message = "Error al eliminar producto: ${e.message}"
                )
            }
        }
    }

    // ========== SINCRONIZACIÓN ==========

    /**
     * Sincroniza todos los productos pendientes con el backend
     * Útil para sincronizar después de recuperar conexión
     */
    fun syncPendientes() {
        viewModelScope.launch {
            try {
                val result = productoRepository.syncPendientes()
                
                if (result.isSuccess) {
                    val syncCount = result.getOrNull() ?: 0
                    // Opcional: Mostrar mensaje de éxito con cantidad sincronizada
                    // Recargar lista si hay cambios
                    if (syncCount > 0) {
                        searchProductos(_searchQuery.value)
                    }
                }
            } catch (e: Exception) {
                // Error silencioso, se reintentará después
            }
        }
    }

    // ========== UTILIDADES ==========

    /**
     * Limpia el estado de búsqueda
     */
    fun clearSearch() {
        _searchState.value = UiState.Idle
        _searchQuery.value = ""
    }

    /**
     * Limpia el producto seleccionado
     */
    fun clearSelectedProducto() {
        _selectedProducto.value = null
        _productoDetailState.value = UiState.Idle
    }

    /**
     * Limpia el estado de creación
     */
    fun clearCreateState() {
        _createState.value = UiState.Idle
    }

    /**
     * Limpia el estado de actualización
     */
    fun clearUpdateState() {
        _updateState.value = UiState.Idle
    }

    /**
     * Limpia el estado de eliminación
     */
    fun clearDeleteState() {
        _deleteState.value = UiState.Idle
    }

    /**
     * Verifica si un producto está próximo a vencer (menos de 2 meses)
     * @param fechaVencimiento Fecha en formato ISO (yyyy-MM-dd)
     * @return true si quedan menos de 60 días
     */
    fun isVencimientoCercano(fechaVencimiento: String?): Boolean {
        if (fechaVencimiento == null) return false

        try {
            // Implementación simple de comparación de fechas
            val parts = fechaVencimiento.split("-")
            if (parts.size != 3) return false
            
            val year = parts[0].toIntOrNull() ?: return false
            val month = parts[1].toIntOrNull() ?: return false
            val day = parts[2].toIntOrNull() ?: return false
            
            // Obtener fecha actual
            val currentDate = java.util.Calendar.getInstance()
            val currentYear = currentDate.get(java.util.Calendar.YEAR)
            val currentMonth = currentDate.get(java.util.Calendar.MONTH) + 1
            val currentDay = currentDate.get(java.util.Calendar.DAY_OF_MONTH)
            
            // Calcular diferencia aproximada en días
            val yearDiff = (year - currentYear) * 365
            val monthDiff = (month - currentMonth) * 30
            val dayDiff = day - currentDay
            
            val totalDays = yearDiff + monthDiff + dayDiff
            
            return totalDays in 1..60
        } catch (e: Exception) {
            return false
        }
    }
}


