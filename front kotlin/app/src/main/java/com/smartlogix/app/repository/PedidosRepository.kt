package com.smartlogix.app.repository

import com.smartlogix.app.model.*
import com.smartlogix.app.network.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class PedidosRepository {
    private val api = RetrofitClient.pedidosService


    suspend fun getPedidosEnPicking(): Result<List<PedidoResponse>> {
        return try {
            val response = api.getPedidosEnPicking()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Error al obtener pedidos en picking: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPedidosTodos(): Result<List<PedidoResponse>> {
        return try {
            val response = api.getPedidosTodos()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Error al obtener todos los pedidos: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPedidosPendientes(): Result<List<PedidoResponse>> {
        return try {
            val response = api.getPedidosPendientes()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Error al obtener pendientes: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPedidosEntregados(): Result<List<PedidoResponse>> {
        return try {
            val response = api.getPedidosEntregados()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Error al obtener entregados: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPedidosFacturados(): Result<List<PedidoResponse>> {
        return try {
            val response = api.getPedidosFacturados()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Error al obtener pedidos facturados: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun revisionFactura(id: Int): Result<Unit> {
        return try {
            val response = api.revisionFactura(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al revisar factura: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPedidoDetalleCompleto(id: Long): Result<PedidoDetalleDTO> {
        return try {
            val response = api.getPedidoDetalle(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al obtener detalle completo: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun asignarPicking(pedidoId: Int): Result<PedidoResponse> {
        return try {
            val response = api.asignarPicking(pedidoId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al asignar picking: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Nota: El usuario no mencionó "iniciar picking" explícitamente en la lista nueva, 
    // pero sí "Asignarse Picking" (que cambia a PICKING_ASIGNADO) y "Confirmar Item".
    // Si no hay un endpoint intermedio para pasar a EN_PICKING, se asume que 
    // el primer "Confirmar Item" lo cambia, O que "Asignarse Picking" es suficiente inicio.
    // Mantendré getHojaPicking para obtener datos frescos.
    suspend fun getHojaPicking(pedidoId: Int): Result<PedidoResponse> {
         return try {
            val response = api.getHojaPicking(pedidoId)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                // Convertimos HojaPickingResponse a PedidoResponse para compatibilidad con UI
                Result.success(body.toPedidoResponse())
            } else {
                Result.failure(Exception("Error al obtener hoja de picking: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun confirmarItem(pedidoId: Int, sku: String, cantidad: Int, codigoUbicacion: String): Result<PedidoResponse> {
        return try {
            val request = ConfirmarItemRequest(sku, cantidad, codigoUbicacion)
            val response = api.confirmarItem(pedidoId, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al confirmar item: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun completarPicking(pedidoId: Int): Result<Unit> {
        return try {
            val response = api.completarPicking(pedidoId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al completar picking: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    
    suspend fun asignarTransporte(pedidoId: Int): Result<Unit> {
        return try {
            val response = api.asignarTransporte(pedidoId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al asignar transporte: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun iniciarTransporte(pedidoId: Int): Result<Unit> {
        return try {
            val response = api.iniciarTransporte(pedidoId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al iniciar transporte: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registrarFactura(pedidoId: Int, urlFactura: String): Result<PedidoResponse> {
        return try {
            val request = RegistrarFacturaRequest(urlFactura)
            val response = api.registrarFactura(pedidoId, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al registrar factura: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun finalizarEntrega(pedidoId: Int, fotoEvidenciaBase64: String): Result<Unit> {
        return try {
            val request = FinalizarEntregaRequest(fotoEvidenciaBase64)
            val response = api.finalizarEntrega(pedidoId, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al finalizar entrega: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun crearPedido(request: CrearPedidoRequest): Result<PedidoResponse> {
        return try {
            val response = api.crearPedido(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al crear pedido: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getEstadisticas(): Result<PedidoStats> {
        return try {
            val response = api.getEstadisticas()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al obtener estadísticas: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


