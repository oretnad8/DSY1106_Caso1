package com.smartlogix.app.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.smartlogix.app.models.Rol // <-- CORRECCIÓN AQUÍ

@Entity(tableName = "usuarios_pendientes_creacion") // Tabla para usuarios nuevos pendientes de enviar
data class UsuarioLocal(
    @PrimaryKey(autoGenerate = true)
    val idLocal: Long = 0, // ID único local para este registro pendiente

    // Datos necesarios para crear el usuario (basado en CreateUsuarioRequest)
    val nombre: String,
    val email: String,
    val rol: String, // Guardamos el nombre del Enum Rol
    // Nota: La contraseña no se guarda aquí por seguridad.
    // Se usaría solo al momento de enviarla al backend.

    val timestamp: Long = System.currentTimeMillis() // Para saber cuándo se intentó crear
)

