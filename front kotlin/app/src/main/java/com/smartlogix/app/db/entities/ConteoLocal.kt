package com.smartlogix.app.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conteos_pendientes") // Nombre de la tabla en SQLite
data class ConteoLocal(
    @PrimaryKey(autoGenerate = true)
    val idLocal: Long = 0, // ID único autoincremental para esta tabla local

    // Datos que necesitamos guardar para enviar luego al backend
    val sku: String,
    val idUbicacion: Int,
    val cantidadFisica: Int,

    // Para saber cuándo se registró y poder reintentar en orden si es necesario
    val timestamp: Long = System.currentTimeMillis()
)

