package com.smartlogix.app.network

import retrofit2.Response
import retrofit2.http.*

/**
 * Servicio API para aprobaciones (Puerto 8085)
 */
interface AprobacionesApiService {

    /**
     * GET /api/aprobaciones
     * Listar aprobaciones
     * Query opcional: estado=PENDIENTE|APROBADO|RECHAZADO
     */
    @GET("api/aprobaciones")
    suspend fun getAprobaciones(@Query("estado") estado: String? = null): Response<List<AprobacionResponse>>

    /**
     * GET /api/aprobaciones/{id}
     * Obtener aprobación por ID
     */
    @GET("api/aprobaciones/{id}")
    suspend fun getAprobacionById(@Path("id") id: Int): Response<AprobacionResponse>

    /**
     * POST /api/aprobaciones
     * Crear solicitud de aprobación
     * Roles: OPERADOR, SUPERVISOR, JEFE
     */
    @POST("api/aprobaciones")
    suspend fun createAprobacion(@Body request: AprobacionRequest): Response<AprobacionResponse>

    /**
     * PUT /api/aprobaciones/{id}/aprobar
     * Aprobar solicitud
     * Roles: JEFE, SUPERVISOR
     */
    @PUT("api/aprobaciones/{id}/aprobar")
    suspend fun aprobarSolicitud(
        @Path("id") id: Int,
        @Body request: AprobarRequest
    ): Response<AprobacionResponse>

    /**
     * PUT /api/aprobaciones/{id}/rechazar
     * Rechazar solicitud
     * Roles: JEFE, SUPERVISOR
     */
    @PUT("api/aprobaciones/{id}/rechazar")
    suspend fun rechazarSolicitud(
        @Path("id") id: Int,
        @Body request: RechazarRequest
    ): Response<AprobacionResponse>

    /**
     * GET /api/aprobaciones/mis-solicitudes
     * Ver mis solicitudes
     * Roles: OPERADOR, SUPERVISOR, JEFE
     */
    @GET("api/aprobaciones/mis-solicitudes")
    suspend fun getMisSolicitudes(): Response<List<AprobacionResponse>>
}


