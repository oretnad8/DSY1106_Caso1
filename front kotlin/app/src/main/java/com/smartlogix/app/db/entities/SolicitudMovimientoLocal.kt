package com.smartlogix.app.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "solicitudes_pendientes") // Nombre de la tabla
data class SolicitudMovimientoLocal(
    @PrimaryKey(autoGenerate = true)
    val idLocal: Long = 0, // ID único en esta tabla local
    val tipoMovimiento: String, // "INGRESO", "EGRESO", "REUBICACION"
    val codigoBarras: String,
    val cantidad: Int,
    val motivo: String,
    val idUbicacionOrigen: Int?, // Null si no aplica
    val idUbicacionDestino: Int?, // Null si no aplica
    val timestamp: Long = System.currentTimeMillis() // Para saber cuándo se guardó
)

