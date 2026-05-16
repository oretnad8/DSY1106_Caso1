package com.smartlogix.app.repository

import android.util.Log
import androidx.compose.foundation.layout.size
import com.smartlogix.app.db.daos.AsignacionUbicacionDao
import com.smartlogix.app.db.daos.UbicacionDao
import com.smartlogix.app.db.entities.AsignacionUbicacionLocal
import com.smartlogix.app.db.entities.UbicacionLocal
import com.smartlogix.app.models.Ubicacion
import com.smartlogix.app.network.AsignarUbicacionRequest
import com.smartlogix.app.network.UbicacionesApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Repositorio para ubicaciones con patrón local-first
 * Actualizado para soportar 5 pasillos × 60 posiciones × 3 pisos = 900 ubicaciones
 */
class UbicacionRepository(
    private val ubicacionDao: UbicacionDao,
    private val asignacionDao: AsignacionUbicacionDao,
    private val apiService: UbicacionesApiService
) {

    companion object {
        private const val TAG = "UbicacionRepository"
    }

    // ========== CONSULTA DE UBICACIONES ==========

    /**
     * Obtiene todas las ubicaciones del backend y las cachea localmente
     * @param piso Filtro opcional por piso (A, B, C)
     * @param pasillo Filtro opcional por pasillo (1-5)
     * @param forceRefresh Fuerza la actualización desde el backend
     */
    suspend fun getUbicaciones(
        piso: String? = null,
        pasillo: Int? = null,
        esEstante: Boolean? = null,
        nivel: Int? = null,
        forceRefresh: Boolean = false
    ): Result<List<Ubicacion>> {
        return try {
            // Si no es refresh forzado, intentar obtener del cache
            if (!forceRefresh) {
                // ... (Room logic doesn't support these filters yet, so we use the network if they are provided)
                if (esEstante != null || nivel != null) {
                    return getUbicaciones(piso, pasillo, esEstante, nivel, forceRefresh = true)
                }
                
                val cached = when {
                    piso != null && pasillo != null -> ubicacionDao.getByPasilloYPiso(pasillo, piso)
                    piso != null -> ubicacionDao.getByPiso(piso)
                    pasillo != null -> ubicacionDao.getByPasillo(pasillo)
                    else -> ubicacionDao.getAll()
                }

                val cachedData = cached.first()

                if (cachedData.isNotEmpty()) {
                    Log.d(TAG, "Cargando ${cachedData.size} ubicaciones desde el caché.")
                    // Forzar la actualización desde la red para obtener los detalles del producto
                    return getUbicaciones(piso, pasillo, forceRefresh = true)
                }
            }

            // Obtener del backend (Este código solo se ejecuta si el caché está vacío o se fuerza el refresco)
            val response = apiService.getUbicaciones(piso, pasillo, esEstante, nivel)
            if (response.isSuccessful && response.body() != null) {
                val ubicaciones = response.body()!!.map { apiUbicacion ->
                    Ubicacion(
                        idUbicacion = apiUbicacion.idUbicacion,
                        codigoUbicacion = apiUbicacion.codigoUbicacion,
                        pasillo = apiUbicacion.pasillo,
                        piso = apiUbicacion.piso.firstOrNull() ?: 'A',
                        numero = apiUbicacion.numero,
                        nivel = apiUbicacion.nivel,
                        esEstante = apiUbicacion.esEstante,
                        productos = apiUbicacion.productos?.map {
                            com.smartlogix.app.models.ProductoEnUbicacion(
                                sku = it.sku,
                                descripcion = it.descripcion,
                                cantidad = it.cantidadEnUbicacion
                            )
                        }
                    )
                }

                // Actualizar cache
                val ubicacionesLocal = ubicaciones.map {
                    UbicacionLocal(
                        id = it.idUbicacion,
                        codigo = it.codigoUbicacion,
                        pasillo = it.pasillo,
                        piso = it.piso.toString(),
                        numero = it.numero,
                        nivel = it.nivel,
                        esEstante = it.esEstante
                    )
                }
                ubicacionDao.insertAll(ubicacionesLocal)

                Log.d(TAG, "Ubicaciones actualizadas desde backend: ${ubicaciones.size}")
                Result.success(ubicaciones)
            } else {
                Result.failure(Exception("Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener ubicaciones", e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene una ubicación específica por código
     */
    suspend fun getUbicacionByCodigo(codigo: String): Result<Ubicacion> {
        return try {
            val response = apiService.getUbicacionByCodigo(codigo)
            if (response.isSuccessful && response.body() != null) {
                val ub = response.body()!!
                val ubicacion = mapResponseToModel(ub)
                Result.success(ubicacion)
            } else {
                Result.failure(Exception("Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener ubicación $codigo", e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene una ubicación específica por ID
     */
    suspend fun getUbicacionById(id: Int): Result<Ubicacion> {
        return try {
            val response = apiService.getUbicacionById(id)
            if (response.isSuccessful && response.body() != null) {
                val ub = response.body()!!
                val ubicacion = mapResponseToModel(ub)
                Result.success(ubicacion)
            } else {
                Result.failure(Exception("Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener ubicación ID $id", e)
            Result.failure(e)
        }
    }

    private fun mapResponseToModel(ub: com.smartlogix.app.network.UbicacionResponse): Ubicacion {
        return Ubicacion(
            idUbicacion = ub.idUbicacion,
            codigoUbicacion = ub.codigoUbicacion,
            pasillo = ub.pasillo,
            piso = ub.piso.firstOrNull() ?: 'A',
            numero = ub.numero,
            nivel = ub.nivel,
            esEstante = ub.esEstante,
            productos = ub.productos?.map {
                com.smartlogix.app.models.ProductoEnUbicacion(
                    sku = it.sku,
                    descripcion = it.descripcion,
                    cantidad = it.cantidadEnUbicacion
                )
            }
        )
    }

    // ========== ASIGNACIÓN DE PRODUCTOS (LOCAL-FIRST) ==========

    /**
     * Asigna un producto a una ubicación con patrón local-first
     */
    suspend fun asignarProducto(codigoBarras: String, codigoUbicacion: String, cantidad: Int): Result<Unit> {
        return try {
            // 1. Guardar en Room primero
            val asignacionLocal = AsignacionUbicacionLocal(
                codigoBarras = codigoBarras,
                codigoUbicacion = codigoUbicacion,
                cantidad = cantidad
            )
            val idLocal = asignacionDao.insert(asignacionLocal)
            Log.d(TAG, "Asignación guardada localmente: $codigoBarras -> $codigoUbicacion")

            // 2. Intentar enviar al backend
            try {
                val request = AsignarUbicacionRequest(
                    codigoBarras = codigoBarras,
                    codigoUbicacion = codigoUbicacion,
                    cantidad = cantidad
                )
                val response = apiService.asignarProducto(request)
                
                if (response.isSuccessful) {
                    // 3. Si OK, eliminar de Room
                    asignacionDao.deleteById(idLocal)
                    Log.d(TAG, "Asignación completada en backend: $codigoBarras -> $codigoUbicacion")
                    Result.success(Unit)
                } else {
                    Log.w(TAG, "Asignación guardada solo localmente: ${response.code()}")
                    Result.failure(Exception("Guardado localmente. Sincronizará después."))
                }
            } catch (e: Exception) {
                Log.w(TAG, "Asignación guardada solo localmente (sin conexión)", e)
                Result.failure(Exception("Guardado localmente. Sincronizará cuando haya conexión."))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al asignar producto", e)
            Result.failure(e)
        }
    }

    // ========== SINCRONIZACIÓN ==========

    /**
     * Sincroniza todas las asignaciones pendientes
     */
    suspend fun syncAsignacionesPendientes(): Result<Int> {
        return try {
            val pendientes = asignacionDao.getAllPendientes()
            var syncCount = 0
            
            pendientes.collect { lista ->
                lista.forEach { asignacionLocal ->
                    try {
                        val request = AsignarUbicacionRequest(
                            codigoBarras = asignacionLocal.codigoBarras,
                            codigoUbicacion = asignacionLocal.codigoUbicacion,
                            cantidad = asignacionLocal.cantidad
                        )

                        val response = apiService.asignarProducto(request)
                        if (response.isSuccessful) {
                            asignacionDao.deleteById(asignacionLocal.idLocal)
                            syncCount++
                            Log.d(TAG, "Asignación sincronizada: ${asignacionLocal.codigoBarras}")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error al sincronizar asignación ${asignacionLocal.idLocal}", e)
                    }
                }
            }
            
            Result.success(syncCount)
        } catch (e: Exception) {
            Log.e(TAG, "Error en sincronización de asignaciones", e)
            Result.failure(e)
        }
    }

}


