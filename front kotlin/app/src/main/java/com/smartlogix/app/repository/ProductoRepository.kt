package com.smartlogix.app.repository

import android.util.Log
import com.smartlogix.app.db.daos.ProductoDao
import com.smartlogix.app.db.entities.ProductoLocal
import com.smartlogix.app.models.Producto
import com.smartlogix.app.models.ProductoUbicacion
import com.smartlogix.app.network.ProductosApiService
import com.smartlogix.app.network.ProductoResponse
import com.smartlogix.app.network.ProductoRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repositorio para productos con patrón local-first
 * 1. Guarda en Room primero
 * 2. Envía al backend
 * 3. Si recibe 200, elimina de Room
 */
class ProductoRepository(
    private val productoDao: ProductoDao,
    private val apiService: ProductosApiService
) {

    companion object {
        private const val TAG = "ProductoRepository"
    }

    // ========== BÚSQUEDA Y CONSULTA ==========

    /**
     * Busca productos en el backend
     */
    suspend fun searchProductos(query: String? = null): Result<List<Producto>> {
        return try {
            val response = apiService.searchProductos(query)
            if (response.isSuccessful && response.body() != null) {
                val productos = response.body()!!.map { it.toDomainModel() }
                Result.success(productos)
            } else {
                Result.failure(Exception("Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al buscar productos", e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene un producto por SKU del backend
     */
    suspend fun getProductoBySku(sku: String): Result<Producto> {
        return try {
            val response = apiService.getProductoBySku(sku)
            if (response.isSuccessful && response.body() != null) {
                val producto = response.body()!!.toDomainModel()
                Result.success(producto)
            } else {
                Result.failure(Exception("Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener producto $sku", e)
            Result.failure(e)
        }
    }

    // ========== CREAR PRODUCTO (LOCAL-FIRST) ==========

    /**
     * Crea un producto con patrón local-first
     * 1. Guarda en Room
     * 2. Intenta enviar al backend
     * 3. Si OK (200), elimina de Room
     */
    suspend fun createProducto(request: ProductoRequest): Result<Producto> {
        return try {
            // 1. Guardar en Room primero
            val productoLocal = ProductoLocal(
                sku = request.sku,
                descripcion = request.descripcion,
                stock = request.stock,
                codigoBarrasIndividual = request.codigoBarrasIndividual,
                lpn = request.lpn,
                lpnDesc = request.lpnDesc,
                fechaVencimiento = request.fechaVencimiento,
                operacion = "CREATE"
            )
            productoDao.insert(productoLocal)
            Log.d(TAG, "Producto guardado localmente: ${request.sku}")

            // 2. Intentar enviar al backend
            try {
                val response = apiService.createProducto(request)
                if (response.isSuccessful && response.body() != null) {
                    // 3. Si OK, eliminar de Room
                    productoDao.deleteBySku(request.sku)
                    Log.d(TAG, "Producto creado en backend y eliminado de local: ${request.sku}")
                    
                    val producto = response.body()!!.toDomainModel()
                    Result.success(producto)
                } else {
                    // Backend falló, pero está guardado localmente
                    Log.w(TAG, "Producto guardado solo localmente: ${response.code()}")
                    Result.failure(Exception("Guardado localmente. Sincronizará después."))
                }
            } catch (e: Exception) {
                // Error de red, pero está guardado localmente
                Log.w(TAG, "Producto guardado solo localmente (sin conexión)", e)
                Result.failure(Exception("Guardado localmente. Sincronizará cuando haya conexión."))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al crear producto", e)
            Result.failure(e)
        }
    }

    // ========== ACTUALIZAR PRODUCTO (LOCAL-FIRST) ==========

    /**
     * Actualiza un producto con patrón local-first
     */
    suspend fun updateProducto(sku: String, request: ProductoRequest): Result<Producto> {
        return try {
            // 1. Guardar en Room primero
            val productoLocal = ProductoLocal(
                sku = sku,
                descripcion = request.descripcion,
                stock = request.stock,
                codigoBarrasIndividual = request.codigoBarrasIndividual,
                lpn = request.lpn,
                lpnDesc = request.lpnDesc,
                fechaVencimiento = request.fechaVencimiento,
                operacion = "UPDATE"
            )
            productoDao.insert(productoLocal)
            Log.d(TAG, "Actualización guardada localmente: $sku")

            // 2. Intentar enviar al backend
            try {
                val response = apiService.updateProducto(sku, request)
                if (response.isSuccessful && response.body() != null) {
                    // 3. Si OK, eliminar de Room
                    productoDao.deleteBySku(sku)
                    Log.d(TAG, "Producto actualizado en backend: $sku")
                    
                    val producto = response.body()!!.toDomainModel()
                    Result.success(producto)
                } else {
                    Log.w(TAG, "Actualización guardada solo localmente: ${response.code()}")
                    Result.failure(Exception("Guardado localmente. Sincronizará después."))
                }
            } catch (e: Exception) {
                Log.w(TAG, "Actualización guardada solo localmente (sin conexión)", e)
                Result.failure(Exception("Guardado localmente. Sincronizará cuando haya conexión."))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar producto", e)
            Result.failure(e)
        }
    }

    // ========== ELIMINAR PRODUCTO ==========

    /**
     * Elimina un producto (solo backend, no requiere local-first)
     */
    suspend fun deleteProducto(sku: String): Result<Unit> {
        return try {
            val response = apiService.deleteProducto(sku)
            if (response.isSuccessful) {
                Log.d(TAG, "Producto eliminado: $sku")
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar producto", e)
            Result.failure(e)
        }
    }

    // ========== SINCRONIZACIÓN ==========

    /**
     * Obtiene todos los productos pendientes de sincronización
     */
    fun getPendientes(): Flow<List<ProductoLocal>> {
        return productoDao.getAllPendientes()
    }

    /**
     * Sincroniza todos los productos pendientes
     */
    suspend fun syncPendientes(): Result<Int> {
        return try {
            val pendientes = productoDao.getAllPendientes()
            var syncCount = 0
            
            pendientes.collect { lista ->
                lista.forEach { productoLocal ->
                    try {
                        val request = ProductoRequest(
                            sku = productoLocal.sku,
                            descripcion = productoLocal.descripcion,
                            stock = productoLocal.stock,
                            codigoBarrasIndividual = productoLocal.codigoBarrasIndividual,
                            lpn = productoLocal.lpn,
                            lpnDesc = productoLocal.lpnDesc,
                            fechaVencimiento = productoLocal.fechaVencimiento
                        )

                        val response = when (productoLocal.operacion) {
                            "CREATE" -> apiService.createProducto(request)
                            "UPDATE" -> apiService.updateProducto(productoLocal.sku, request)
                            else -> null
                        }

                        if (response?.isSuccessful == true) {
                            productoDao.deleteBySku(productoLocal.sku)
                            syncCount++
                            Log.d(TAG, "Sincronizado: ${productoLocal.sku}")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error al sincronizar ${productoLocal.sku}", e)
                    }
                }
            }
            
            Result.success(syncCount)
        } catch (e: Exception) {
            Log.e(TAG, "Error en sincronización", e)
            Result.failure(e)
        }
    }

    // ========== CONVERSIÓN ==========

    /**
     * Convierte ProductoResponse a modelo de dominio
     */
    private fun ProductoResponse.toDomainModel(): Producto {
        return Producto(
            sku = this.sku,
            descripcion = this.descripcion,
            stock = this.stock,
            codigoBarrasIndividual = this.codigoBarrasIndividual,
            lpn = this.lpn,
            lpnDesc = this.lpnDesc,
            fechaVencimiento = this.fechaVencimiento,
            vencimientoCercano = this.vencimientoCercano,
            ubicaciones = this.ubicaciones?.map { ubicacion ->
                ProductoUbicacion(
                    idUbicacion = ubicacion.idUbicacion,
                    codigoUbicacion = ubicacion.codigoUbicacion,
                    cantidad = ubicacion.cantidad
                )
            }
        )
    }
}


