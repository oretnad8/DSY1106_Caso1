package com.smartlogix.app.db.daos

import androidx.room.*
import com.smartlogix.app.db.entities.ConteoLocal
import kotlinx.coroutines.flow.Flow

@Dao
interface ConteoDao {

    @Query("SELECT * FROM conteos_pendientes ORDER BY timestamp ASC")
    fun getAllPendientes(): Flow<List<ConteoLocal>>

    @Query("SELECT * FROM conteos_pendientes WHERE idLocal = :idLocal LIMIT 1")
    suspend fun getPendienteById(idLocal: Long): ConteoLocal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(conteo: ConteoLocal): Long

    // Alias para compatibilidad con código existente
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarConteoPendiente(conteo: ConteoLocal): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(conteos: List<ConteoLocal>)

    @Update
    suspend fun update(conteo: ConteoLocal)

    @Delete
    suspend fun delete(conteo: ConteoLocal)

    @Query("DELETE FROM conteos_pendientes WHERE idLocal = :idLocal")
    suspend fun deleteById(idLocal: Long)

    @Query("DELETE FROM conteos_pendientes")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM conteos_pendientes")
    suspend fun countPendientes(): Int
}


