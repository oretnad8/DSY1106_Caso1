package com.smartlogix.app.services

import android.app.Application
import com.smartlogix.app.db.AppDatabase
import com.smartlogix.app.db.entities.UbicacionLocal
import com.smartlogix.app.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Servicio para gestionar ubicaciones
 * Proporciona funciones para convertir códigos de ubicación a IDs
 */
class UbicacionService(private val application: Application) {

    private val ubicacionDao = AppDatabase.getDatabase(application).ubicacionDao()
    private val ubicacionesService = RetrofitClient.ubicacionesService

    /**
     * Obtiene el ID de una ubicación basado en su código
     * Primero intenta obtener de la base de datos local
     * Si no está disponible, obtiene del backend
     */
    suspend fun getIdUbicacionByCodigo(codigo: String): Int? {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Intentar local
                val local = ubicacionDao.getByCodigo(codigo)
                if (local != null) return@withContext local.id

                // 2. Intentar backend
                val response = ubicacionesService.getUbicacionByCodigo(codigo)
                if (response.isSuccessful && response.body() != null) {
                    val ubicacionResponse = response.body()!!

                    // Guardar en base de datos local
                    val ubicacionLocal = UbicacionLocal(
                        id = ubicacionResponse.idUbicacion,
                        codigo = ubicacionResponse.codigoUbicacion,
                        pasillo = ubicacionResponse.pasillo,
                        piso = ubicacionResponse.piso,
                        numero = ubicacionResponse.numero
                    )
                    ubicacionDao.insert(ubicacionLocal)

                    return@withContext ubicacionResponse.idUbicacion
                }

                null
            } catch (e: Exception) {
                android.util.Log.e("UbicacionService", "Error getting ID for $codigo", e)
                null
            }
        }
    }

    /**
     * Obtiene el código de una ubicación basado en su ID
     * Si no está en caché local, carga todas las ubicaciones del backend
     */
    suspend fun getCodigoById(id: Int?): String? {
        if (id == null) return null
        
        return withContext(Dispatchers.IO) {
            try {
                // 1. Intentar local
                val local = ubicacionDao.getById(id)
                if (local != null) return@withContext local.codigo

                // 2. Si no esta, cargar TODAS del backend para poblar cache (eficiencia)
                val response = ubicacionesService.getUbicaciones()
                if (response.isSuccessful && response.body() != null) {
                    val allUbicaciones = response.body()!!.map { 
                        UbicacionLocal(
                            id = it.idUbicacion,
                            codigo = it.codigoUbicacion,
                            pasillo = it.pasillo,
                            piso = it.piso,
                            numero = it.numero
                        )
                    }
                    ubicacionDao.insertAll(allUbicaciones)
                    
                    // Re-intentar busqueda
                    return@withContext ubicacionDao.getById(id)?.codigo
                }

                null
            } catch (e: Exception) {
                android.util.Log.e("UbicacionService", "Error getting Codigo for $id", e)
                null
            }
        }
    }
}


