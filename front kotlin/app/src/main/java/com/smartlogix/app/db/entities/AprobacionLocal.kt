package com.smartlogix.app.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad local para solicitudes de aprobación pendientes
 * Guarda solicitudes creadas offline hasta que se envíen al backend
 */
@Entity(tableName = "aprobaciones_pendientes")
data class AprobacionLocal(
    @PrimaryKey(autoGenerate = true)
    val idLocal: Long = 0,
    val tipoMovimiento: String, // "INGRESO", "EGRESO", "REUBICACION"
    val sku: String,
    val cantidad: Int,
    val motivo: String,
    val idUbicacionOrigen: Int?, // Solo para REUBICACION
    val idUbicacionDestino: Int?, // Solo para REUBICACION
    val timestamp: Long = System.currentTimeMillis()
)


