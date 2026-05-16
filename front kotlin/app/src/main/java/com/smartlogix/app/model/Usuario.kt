package com.smartlogix.app.models

/**
 * Modelo de datos para Usuario del sistema
 * Representa la información básica de un usuario autenticado
 */
data class Usuario(
    val id: Int,
    val nombre: String,
    val email: String,
    val rol: Rol,
    val activo: Boolean = true
)

/**
 * Enum para los roles del sistema
 * - ADMIN: Puede crear usuarios y gestionar configuración
 * - JEFE: Aprobaciones sin restricciones, registro directo de movimientos
 * - SUPERVISOR: Aprobaciones sin restricciones con notificación a jefe
 * - OPERADOR: Solo solicitudes que requieren aprobación
 */
enum class Rol {
    ADMIN,
    JEFE,
    SUPERVISOR,
    OPERADOR
}

/**
 * Request para crear/actualizar usuario
 */
data class UsuarioRequest(
    val nombre: String,
    val email: String,
    val password: String,
    val rol: String // "ADMIN", "JEFE", "SUPERVISOR", "OPERADOR"
)

/**
 * Response del login con token JWT
 */
data class LoginResponse(
    val token: String,
    val type: String, // "Bearer"
    val id: Int,
    val nombre: String,
    val email: String,
    val rol: String
)

/**
 * Request para login
 */
data class LoginRequest(
    val email: String,
    val password: String
)

data class CreateUsuarioRequest(
    val nombre: String,
    val email: String,
    val rol: Rol, // Usamos el Enum 'Rol' que ya tienes
    val contrasena: String // La contraseña que escribe el usuario
)

