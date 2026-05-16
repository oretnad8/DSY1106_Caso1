package com.smartlogix.app.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.smartlogix.app.repository.ProductoRepository
import com.smartlogix.app.repository.UbicacionRepository

/**
 * Factory para crear ViewModels con inyección de dependencias
 * Permite pasar repositorios a los ViewModels
 */
class ViewModelFactory(
    private val application: Application,
    private val productoRepository: ProductoRepository? = null,
    private val ubicacionRepository: UbicacionRepository? = null,
    private val pedidosRepository: com.smartlogix.app.repository.PedidosRepository? = null
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ProductoViewModel::class.java) -> {
                requireNotNull(productoRepository) { "ProductoRepository requerido" }
                ProductoViewModel(productoRepository) as T
            }
            modelClass.isAssignableFrom(UbicacionViewModel::class.java) -> {
                requireNotNull(ubicacionRepository) { "UbicacionRepository requerido" }
                UbicacionViewModel(application, ubicacionRepository) as T
            }
            modelClass.isAssignableFrom(PedidosViewModel::class.java) -> {
                requireNotNull(pedidosRepository) { "PedidosRepository requerido" }
                PedidosViewModel(pedidosRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}


