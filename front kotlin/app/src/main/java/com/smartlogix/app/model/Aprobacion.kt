package com.smartlogix.app.models

/**
 * Modelo de datos para Aprobación
 * Representa una solicitud de movimiento (ingreso/egreso/reubicación)
 * que requiere aprobación de un Jefe o Supervisor
 */
data class Aprobacion(
    val id: Int,
    val tipoMovimiento: TipoMovimiento,
    val codigoBarras: String,
    val cantidad: Int,
    val motivo: String,
    val estado: EstadoAprobacion,
    val solicitante: String, // Nombre del usuario que solicitó
    val idSolicitante: Int,
    val aprobador: String?, // Nombre del usuario que aprobó/rechazó
    val idAprobador: Int?,
    val observaciones: String?, // Comentarios del aprobador
    val fechaSolicitud: String, // ISO format
    val fechaRespuesta: String?, // ISO format
    // Campos específicos para REUBICACION
    val idUbicacionOrigen: Int?,
    val idUbicacionDestino: Int?,
    val ubicacionOrigen: String?, // Código ubicación origen (ej: A-12)
    val ubicacionDestino: String? // Código ubicación destino (ej: B-25)
)

/**
 * Enum para tipos de movimiento
 */
enum class TipoMovimiento(val label: String) {
    INGRESO("Ingreso"),   // Nueva entrada de productos
    EGRESO("Egreso"),    // Salida de productos
    REUBICACION("Reubicacion") // Cambio de ubicación
}

/**
 * Enum para estados de aprobación
 */
enum class EstadoAprobacion {
    PENDIENTE,
    APROBADO,
    RECHAZADO
}

/**
 * Request para crear una solicitud de aprobación
 */
data class AprobacionRequest(
    val tipoMovimiento: String, // "INGRESO", "EGRESO", "REUBICACION"
    val codigoBarras: String,
    val cantidad: Int,
    val motivo: String,
    val idSolicitante: Int? = null, // ID del usuario que solicita
    // Solo para REUBICACION:
    val idUbicacionOrigen: Int? = null,
    val idUbicacionDestino: Int? = null
)

/**
 * Request para aprobar/rechazar una solicitud
 */
data class RespuestaAprobacionRequest(
    val observaciones: String? = null
)

/**
 * Request para aprobar una solicitud (con ID del aprobador)
 */
data class AprobarRequestWithAprobador(
    val observaciones: String? = null,
    val idAprobador: Int? = null
)

