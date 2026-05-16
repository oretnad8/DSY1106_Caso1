package com.smartlogix.app.models

/**
 * Clase sellada para representar los diferentes estados de la UI
 * Permite manejar estados de carga, éxito y error de forma estructurada
 */
sealed class UiState<out T> {
    /**
     * Estado inicial o en reposo
     */
    object Idle : UiState<Nothing>()

    /**
     * Estado de carga - se está procesando una operación
     */
    object Loading : UiState<Nothing>()

    /**
     * Estado de éxito - la operación se completó correctamente
     * @param data Datos resultantes de la operación
     */
    data class Success<T>(val data: T) : UiState<T>()

    /**
     * Estado de error - la operación falló
     * @param message Mensaje de error para mostrar al usuario
     * @param exception Excepción original (opcional)
     */
    data class Error(
        val message: String,
        val exception: Throwable? = null
    ) : UiState<Nothing>()
}

/**
 * Clase genérica para respuestas de API
 * Encapsula el resultado de llamadas al backend
 */
sealed class ApiResponse<out T> {
    /**
     * Respuesta exitosa de la API
     * @param data Datos retornados por la API
     */
    data class Success<T>(val data: T) : ApiResponse<T>()

    /**
     * Error en la respuesta de la API
     * @param message Mensaje de error
     * @param code Código HTTP de error (opcional)
     */
    data class Error(
        val message: String,
        val code: Int? = null
    ) : ApiResponse<Nothing>()

    /**
     * Error de red o conexión
     * @param exception Excepción de red
     */
    data class NetworkError(
        val exception: Throwable
    ) : ApiResponse<Nothing>()
}

/**
 * Clase para manejar el estado de autenticación
 */
sealed class AuthState {
    object NotAuthenticated : AuthState()
    object Loading : AuthState()
    data class Authenticated(val usuario: Usuario, val token: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

