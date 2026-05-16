package com.smartlogix.app.network

import com.google.gson.annotations.SerializedName

// ========== AUTH MODELS ==========

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val type: String,
    val id: Int,
    val nombre: String,
    val email: String,
    val rol: String
)

// ========== USUARIO MODELS ==========

data class UsuarioRequest(
    val nombre: String,
    val email: String,
    val password: String,
    val rol: String // "ADMIN", "JEFE", "SUPERVISOR", "OPERADOR"
)

data class UsuarioResponse(
    val id: Int,
    val nombre: String,
    val email: String,
    val rol: String,
    val activo: Boolean
)

// ========== PRODUCTO MODELS ==========

data class ProductoRequest(
    val sku: String,
    val descripcion: String,
    val stock: Int,
    val codigoBarrasIndividual: String?,
    val lpn: String?,
    val lpnDesc: String?,
    val fechaVencimiento: String?
)

data class ProductoResponse(
    val sku: String,
    val descripcion: String,
    val stock: Int,
    val codigoBarrasIndividual: String?,
    val lpn: String?,
    val lpnDesc: String?,
    val fechaVencimiento: String?,
    val vencimientoCercano: Boolean = false,
    val ubicaciones: List<ProductoUbicacionResponse>? = null
)

data class ProductoUbicacionResponse(
    val idUbicacion: Int,
    val codigoUbicacion: String,
    val cantidad: Int
)

// ========== UBICACION MODELS ==========

data class UbicacionResponse(
    // La anotación @SerializedName le dice a Gson cómo mapear el JSON a tu variable.
    // Es la forma más segura de evitar errores de nombres.
    @SerializedName("idUbicacion")
    val idUbicacion: Int,

    @SerializedName("codigoUbicacion")
    val codigoUbicacion: String, // Formato: P{pasillo}-{piso}-{numero}

    @SerializedName("pasillo")
    val pasillo: Int, // 1-5

    @SerializedName("piso")
    val piso: String, // A, B, C

    @SerializedName("numero")
    val numero: Int, // 1-60

    // Es buena práctica definir todos los campos que vienen en el JSON
    @SerializedName("productos")
    val productos: List<ProductoEnUbicacion>?,

    @SerializedName("totalProductos")
    val totalProductos: Int,

    @SerializedName("cantidadTotal")
    val cantidadTotal: Int,

    @SerializedName("nivel")
    val nivel: Int?,

    @SerializedName("esEstante")
    val esEstante: Boolean
)

// NUEVA data class para los productos anidados dentro de una ubicación.
// Esto es necesario para que Gson pueda parsear la lista "productos".
data class ProductoEnUbicacion(
    @SerializedName("sku")
    val sku: String,
    @SerializedName("descripcion")
    val descripcion: String,
    @SerializedName("cantidadEnUbicacion")
    val cantidadEnUbicacion: Int
)

data class AsignarUbicacionRequest(
    @SerializedName("sku")
    val codigoBarras: String,
    val codigoUbicacion: String,
    val cantidad: Int
)

data class EgresoUbicacionRequest(
    @SerializedName("sku")
    val codigoBarras: String,
    val codigoUbicacion: String,
    val cantidad: Int,
    val motivo: String? = null
)

data class ReubicarUbicacionRequest(
    @SerializedName("sku")
    val codigoBarras: String,
    val codigoUbicacionOrigen: String,
    val codigoUbicacionDestino: String,
    val cantidad: Int,
    val motivo: String? = null
)

// ========== APROBACION MODELS ==========

data class AprobacionRequest(
    val tipoMovimiento: String, // "INGRESO", "EGRESO", "REUBICACION"
    @SerializedName("sku")
    val codigoBarras: String,
    val cantidad: Int,
    val motivo: String,
    val idSolicitante: Int? = null,
    val idUbicacionOrigen: Int? = null,
    val idUbicacionDestino: Int? = null
)

data class AprobacionResponse(
    val id: Int,
    val tipoMovimiento: String,
    @SerializedName("sku")
    val codigoBarras: String,
    val cantidad: Int,
    val motivo: String,
    val estado: String, // "PENDIENTE", "APROBADO", "RECHAZADO"
    val idUbicacionOrigen: Int?,
    val idUbicacionDestino: Int?,
    val fechaSolicitud: String,
    val observaciones: String?,
    val solicitante: SolicitanteDTO? = null,
    val aprobador: AprobadorDTO? = null,
    val fechaAprobacion: String? = null
)

data class SolicitanteDTO(
    val id: Int,
    val nombre: String,
    val email: String,
    val rol: String
)

data class AprobadorDTO(
    val id: Int,
    val nombre: String,
    val email: String,
    val rol: String
)

data class AprobarRequest(
    val observaciones: String? = null,
    val idAprobador: Int? = null
)

data class RechazarRequest(
    val observaciones: String?
)



// ========== GENERIC API RESPONSE ==========

data class ApiResponse<T>(
    val success: Boolean,
    val message: String?,
    val data: T?
)

data class ApiError(
    val error: String,
    val message: String,
    val status: Int
)


