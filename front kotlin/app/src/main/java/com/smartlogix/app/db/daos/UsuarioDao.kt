package com.smartlogix.app.db.daos

import androidx.room.*
import com.smartlogix.app.db.entities.UsuarioLocal
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    @Query("SELECT * FROM usuarios_pendientes_creacion ORDER BY timestamp ASC")
    fun getAllPendientes(): Flow<List<UsuarioLocal>>

    @Query("SELECT * FROM usuarios_pendientes_creacion WHERE idLocal = :idLocal LIMIT 1")
    suspend fun getPendienteById(idLocal: Long): UsuarioLocal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(usuario: UsuarioLocal): Long

    // Alias para compatibilidad con código existente
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarUsuarioPendiente(usuario: UsuarioLocal): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(usuarios: List<UsuarioLocal>)

    @Update
    suspend fun update(usuario: UsuarioLocal)

    @Delete
    suspend fun delete(usuario: UsuarioLocal)

    @Query("DELETE FROM usuarios_pendientes_creacion WHERE idLocal = :idLocal")
    suspend fun deleteById(idLocal: Long)

    @Query("DELETE FROM usuarios_pendientes_creacion")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM usuarios_pendientes_creacion")
    suspend fun countPendientes(): Int
}


