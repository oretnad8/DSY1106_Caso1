package com.smartlogix.app.db.daos

import androidx.room.*
import com.smartlogix.app.db.entities.SolicitudMovimientoLocal
import kotlinx.coroutines.flow.Flow

@Dao
interface SolicitudMovimientoDao {

    @Query("SELECT * FROM solicitudes_pendientes ORDER BY timestamp ASC")
    fun getAllPendientes(): Flow<List<SolicitudMovimientoLocal>>

    @Query("SELECT * FROM solicitudes_pendientes WHERE idLocal = :idLocal LIMIT 1")
    suspend fun getPendienteById(idLocal: Long): SolicitudMovimientoLocal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(solicitud: SolicitudMovimientoLocal): Long

    // Alias para compatibilidad con código existente
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarSolicitud(solicitud: SolicitudMovimientoLocal): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(solicitudes: List<SolicitudMovimientoLocal>)

    @Update
    suspend fun update(solicitud: SolicitudMovimientoLocal)

    @Delete
    suspend fun delete(solicitud: SolicitudMovimientoLocal)

    @Query("DELETE FROM solicitudes_pendientes WHERE idLocal = :idLocal")
    suspend fun deleteById(idLocal: Long)

    @Query("DELETE FROM solicitudes_pendientes")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM solicitudes_pendientes")
    suspend fun countPendientes(): Int
}


