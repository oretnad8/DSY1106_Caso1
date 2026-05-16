package com.smartlogix.app.db.daos

import androidx.room.*
import com.smartlogix.app.db.entities.UbicacionLocal
import kotlinx.coroutines.flow.Flow

@Dao
interface UbicacionDao {
    
    @Query("SELECT * FROM ubicaciones_cache ORDER BY codigo ASC")
    fun getAll(): Flow<List<UbicacionLocal>>
    
    @Query("SELECT * FROM ubicaciones_cache WHERE piso = :piso ORDER BY pasillo ASC, numero ASC")
    fun getByPiso(piso: String): Flow<List<UbicacionLocal>>
    
    @Query("SELECT * FROM ubicaciones_cache WHERE pasillo = :pasillo ORDER BY piso ASC, numero ASC")
    fun getByPasillo(pasillo: Int): Flow<List<UbicacionLocal>>
    
    @Query("SELECT * FROM ubicaciones_cache WHERE pasillo = :pasillo AND piso = :piso ORDER BY numero ASC")
    fun getByPasilloYPiso(pasillo: Int, piso: String): Flow<List<UbicacionLocal>>
    
    @Query("SELECT * FROM ubicaciones_cache WHERE codigo = :codigo LIMIT 1")
    suspend fun getByCodigo(codigo: String): UbicacionLocal?

    @Query("SELECT * FROM ubicaciones_cache WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): UbicacionLocal?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ubicacion: UbicacionLocal)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(ubicaciones: List<UbicacionLocal>)
    
    @Update
    suspend fun update(ubicacion: UbicacionLocal)
    
    @Delete
    suspend fun delete(ubicacion: UbicacionLocal)
    
    @Query("DELETE FROM ubicaciones_cache")
    suspend fun deleteAll()
    
    @Query("SELECT COUNT(*) FROM ubicaciones_cache")
    suspend fun count(): Int
}


