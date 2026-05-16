package com.smartlogix.app.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "asignaciones_pendientes") // Nombre de la tabla
data class AsignacionUbicacionLocal(
    @PrimaryKey(autoGenerate = true)
    val idLocal: Long = 0, // ID único local

    // Datos necesarios para la asignación (basado en AsignarUbicacionRequest)
    val codigoBarras: String,
    val codigoUbicacion: String, // Usamos el código (ej: "A-12") ya que es más directo
    val cantidad: Int,

    val timestamp: Long = System.currentTimeMillis() // Hora del intento de asignación
)

