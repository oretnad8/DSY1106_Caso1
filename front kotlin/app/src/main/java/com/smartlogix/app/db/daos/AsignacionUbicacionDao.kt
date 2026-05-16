package com.smartlogix.app.db.daos

import androidx.room.*
import com.smartlogix.app.db.entities.AsignacionUbicacionLocal
import kotlinx.coroutines.flow.Flow

@Dao
interface AsignacionUbicacionDao {

    @Query("SELECT * FROM asignaciones_pendientes ORDER BY timestamp ASC")
    fun getAllPendientes(): Flow<List<AsignacionUbicacionLocal>>

    @Query("SELECT * FROM asignaciones_pendientes WHERE idLocal = :idLocal LIMIT 1")
    suspend fun getPendienteById(idLocal: Long): AsignacionUbicacionLocal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(asignacion: AsignacionUbicacionLocal): Long

    // Alias para compatibilidad con código existente
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarAsignacionPendiente(asignacion: AsignacionUbicacionLocal): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(asignaciones: List<AsignacionUbicacionLocal>)

    @Update
    suspend fun update(asignacion: AsignacionUbicacionLocal)

    @Delete
    suspend fun delete(asignacion: AsignacionUbicacionLocal)

    @Query("DELETE FROM asignaciones_pendientes WHERE idLocal = :idLocal")
    suspend fun deleteById(idLocal: Long)

    @Query("DELETE FROM asignaciones_pendientes")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM asignaciones_pendientes")
    suspend fun countPendientes(): Int
}


