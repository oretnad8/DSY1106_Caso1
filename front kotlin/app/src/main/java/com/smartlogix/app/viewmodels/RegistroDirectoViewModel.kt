package com.smartlogix.app.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartlogix.app.models.*
import com.smartlogix.app.network.RetrofitClient
import com.smartlogix.app.network.AsignarUbicacionRequest
import com.smartlogix.app.network.EgresoUbicacionRequest
import com.smartlogix.app.network.ReubicarUbicacionRequest
import com.smartlogix.app.services.UbicacionService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para Registro Directo de Movimientos
 * Maneja operaciones que se ejecutan inmediatamente sin necesidad de aprobación
 * Solo para JEFE y SUPERVISOR
 */
class RegistroDirectoViewModel(application: Application) : AndroidViewModel(application) {

    private val ubicacionesService = RetrofitClient.ubicacionesService
    private val ubicacionService = UbicacionService(application)

    // ========== ESTADOS ==========

    private val _registroState = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val registroState: StateFlow<UiState<Boolean>> = _registroState.asStateFlow()

    // ========== OPERACIONES DIRECTAS ==========

    /**
     * Registra un INGRESO directo
     * Asigna el producto a la ubicación destino inmediatamente
     */
    fun registrarIngreso(codigoBarras: String, cantidad: Int, ubicacionDestino: String, motivo: String) {
        viewModelScope.launch {
            try {
                _registroState.value = UiState.Loading

                // Convertir código de ubicación a ID
                val idDestino = ubicacionService.getIdUbicacionByCodigo(ubicacionDestino)

                if (idDestino == null) {
                    _registroState.value = UiState.Error(
                        message = "No se pudo obtener el ID de la ubicacion. Verifica que el codigo sea valido."
                    )
                    return@launch
                }

                // Asignar producto a ubicación
                val asignarRequest = AsignarUbicacionRequest(
                    codigoBarras = codigoBarras,
                    codigoUbicacion = ubicacionDestino,
                    cantidad = cantidad
                )
                val asignarResponse = ubicacionesService.asignarProducto(asignarRequest)

                if (asignarResponse.isSuccessful) {
                    _registroState.value = UiState.Success(true)
                } else {
                    _registroState.value = UiState.Error(
                        message = "Error al registrar ingreso: ${asignarResponse.code()}"
                    )
                }

            } catch (e: Exception) {
                _registroState.value = UiState.Error(
                    message = "Error al registrar ingreso: ${e.message}"
                )
            }
        }
    }

    /**
     * Registra un EGRESO directo
     * Reduce el stock del producto en la ubicación origen
     */
    fun registrarEgreso(codigoBarras: String, cantidad: Int, ubicacionOrigen: String, motivo: String) {
        viewModelScope.launch {
            try {
                _registroState.value = UiState.Loading

                // Llamar al endpoint de egreso
                val egresoRequest = EgresoUbicacionRequest(
                    codigoBarras = codigoBarras,
                    codigoUbicacion = ubicacionOrigen,
                    cantidad = cantidad,
                    motivo = motivo
                )
                val egresoResponse = ubicacionesService.egresoProducto(egresoRequest)

                if (egresoResponse.isSuccessful) {
                    _registroState.value = UiState.Success(true)
                } else {
                    _registroState.value = UiState.Error(
                        message = "Error al registrar egreso: ${egresoResponse.code()}"
                    )
                }

            } catch (e: Exception) {
                _registroState.value = UiState.Error(
                    message = "Error al registrar egreso: ${e.message}"
                )
            }
        }
    }

    /**
     * Registra una REUBICACION directa
     * Mueve el producto de una ubicación a otra
     */
    fun registrarReubicacion(codigoBarras: String, cantidad: Int, ubicacionOrigen: String, ubicacionDestino: String, motivo: String) {
        viewModelScope.launch {
            try {
                _registroState.value = UiState.Loading

                // Llamar al endpoint de reubicación
                val reubicarRequest = ReubicarUbicacionRequest(
                    codigoBarras = codigoBarras,
                    codigoUbicacionOrigen = ubicacionOrigen,
                    codigoUbicacionDestino = ubicacionDestino,
                    cantidad = cantidad,
                    motivo = motivo
                )
                val reubicarResponse = ubicacionesService.reubicarProducto(reubicarRequest)

                if (reubicarResponse.isSuccessful) {
                    _registroState.value = UiState.Success(true)
                } else {
                    _registroState.value = UiState.Error(
                        message = "Error al registrar reubicacion: ${reubicarResponse.code()}"
                    )
                }

            } catch (e: Exception) {
                _registroState.value = UiState.Error(
                    message = "Error al registrar reubicacion: ${e.message}"
                )
            }
        }
    }

    // ========== UTILIDADES ==========

    fun clearState() {
        _registroState.value = UiState.Idle
    }
}


