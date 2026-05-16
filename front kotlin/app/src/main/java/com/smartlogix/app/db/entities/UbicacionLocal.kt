package com.smartlogix.app.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad local para cachear ubicaciones
 * Permite acceso offline a la lista de ubicaciones
 * Actualizado para soportar 5 pasillos × 60 posiciones × 3 pisos = 900 ubicaciones
 */
@Entity(tableName = "ubicaciones_cache")
data class UbicacionLocal(
    @PrimaryKey
    val id: Int, // ID unico del backend
    val codigo: String, // Formato: P{pasillo}-{piso}-{numero} (ej: "P1-A-12", "P3-B-45")
    val pasillo: Int, // 1-5
    val piso: String, // "A", "B", "C"
    val numero: Int, // 1-60
    val nivel: Int? = null,
    val esEstante: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)


