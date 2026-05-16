package com.smartlogix.app.network

import retrofit2.Response
import retrofit2.http.*

/**
 * Servicio API para autenticación (Puerto 8081)
 */
interface AuthApiService {

    /**
     * POST /api/auth/login
     * Iniciar sesión
     */
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    /**
     * POST /api/auth/logout
     * Cerrar sesión
     */
    @POST("api/auth/logout")
    suspend fun logout(): Response<Unit>

    /**
     * GET /api/auth/validate
     * Validar token JWT
     */
    @GET("api/auth/validate")
    suspend fun validateToken(): Response<Unit>
}


