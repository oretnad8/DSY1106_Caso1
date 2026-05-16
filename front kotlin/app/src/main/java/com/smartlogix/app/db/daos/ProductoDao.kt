package com.smartlogix.app.db.daos

import androidx.room.*
import com.smartlogix.app.db.entities.ProductoLocal
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductoDao {
    
    @Query("SELECT * FROM productos_pendientes ORDER BY timestamp ASC")
    fun getAllPendientes(): Flow<List<ProductoLocal>>
    
    @Query("SELECT * FROM productos_pendientes WHERE sku = :sku LIMIT 1")
    suspend fun getPendienteBySku(sku: String): ProductoLocal?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(producto: ProductoLocal)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(productos: List<ProductoLocal>)
    
    @Update
    suspend fun update(producto: ProductoLocal)
    
    @Delete
    suspend fun delete(producto: ProductoLocal)
    
    @Query("DELETE FROM productos_pendientes WHERE sku = :sku")
    suspend fun deleteBySku(sku: String)
    
    @Query("DELETE FROM productos_pendientes")
    suspend fun deleteAll()
    
    @Query("SELECT COUNT(*) FROM productos_pendientes")
    suspend fun countPendientes(): Int
}


