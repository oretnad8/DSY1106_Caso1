package com.smartlogix.app.network

import retrofit2.Response
import retrofit2.http.*

/**
 * Servicio API para usuarios (Puerto 8082)
 */
interface UsuariosApiService {

    /**
     * GET /api/usuarios
     * Listar todos los usuarios
     * Roles: ADMIN
     */
    @GET("api/usuarios")
    suspend fun getUsuarios(): Response<List<UsuarioResponse>>

    /**
     * GET /api/usuarios/{id}
     * Obtener usuario por ID
     * Roles: ADMIN
     */
    @GET("api/usuarios/{id}")
    suspend fun getUsuarioById(@Path("id") id: Int): Response<UsuarioResponse>

    /**
     * POST /api/usuarios
     * Crear nuevo usuario
     * Roles: ADMIN
     */
    @POST("api/usuarios")
    suspend fun createUsuario(@Body request: UsuarioRequest): Response<UsuarioResponse>

    /**
     * PUT /api/usuarios/{id}
     * Actualizar usuario
     * Roles: ADMIN
     */
    @PUT("api/usuarios/{id}")
    suspend fun updateUsuario(
        @Path("id") id: Int,
        @Body request: UsuarioRequest
    ): Response<UsuarioResponse>

    /**
     * DELETE /api/usuarios/{id}
     * Eliminar usuario
     * Roles: ADMIN
     */
    @DELETE("api/usuarios/{id}")
    suspend fun deleteUsuario(@Path("id") id: Int): Response<Unit>

    /**
     * PUT /api/usuarios/{id}/toggle-activo
     * Activar/Desactivar usuario
     * Roles: ADMIN
     */
    @PUT("api/usuarios/{id}/toggle-activo")
    suspend fun toggleActivo(@Path("id") id: Int): Response<UsuarioResponse>
}


