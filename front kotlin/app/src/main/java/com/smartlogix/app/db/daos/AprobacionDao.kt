package com.smartlogix.app.db.daos

import androidx.room.*
import com.smartlogix.app.db.entities.AprobacionLocal
import kotlinx.coroutines.flow.Flow

@Dao
interface AprobacionDao {
    
    @Query("SELECT * FROM aprobaciones_pendientes ORDER BY timestamp ASC")
    fun getAllPendientes(): Flow<List<AprobacionLocal>>
    
    @Query("SELECT * FROM aprobaciones_pendientes WHERE idLocal = :idLocal LIMIT 1")
    suspend fun getPendienteById(idLocal: Long): AprobacionLocal?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(aprobacion: AprobacionLocal): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(aprobaciones: List<AprobacionLocal>)
    
    @Update
    suspend fun update(aprobacion: AprobacionLocal)
    
    @Delete
    suspend fun delete(aprobacion: AprobacionLocal)
    
    @Query("DELETE FROM aprobaciones_pendientes WHERE idLocal = :idLocal")
    suspend fun deleteById(idLocal: Long)
    
    @Query("DELETE FROM aprobaciones_pendientes")
    suspend fun deleteAll()
    
    @Query("SELECT COUNT(*) FROM aprobaciones_pendientes")
    suspend fun countPendientes(): Int
}


