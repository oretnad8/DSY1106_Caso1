package com.smartlogix.app.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad local para productos pendientes de sincronización
 * Guarda productos creados/actualizados offline hasta que se envíen al backend
 */
@Entity(tableName = "productos_pendientes")
data class ProductoLocal(
    @PrimaryKey
    val sku: String,
    val descripcion: String,
    val stock: Int,
    val codigoBarrasIndividual: String?,
    val lpn: String?,
    val lpnDesc: String?,
    val fechaVencimiento: String?, // Formato ISO: "2025-12-31"
    val operacion: String, // "CREATE", "UPDATE", "DELETE"
    val timestamp: Long = System.currentTimeMillis()
)


