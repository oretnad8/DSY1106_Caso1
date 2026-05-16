package com.smartlogix.app.network

import com.smartlogix.app.model.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Servicio API para la gestión de pedidos
 * Base URL: http://dantero.ddns.net:8086/
 */
interface PedidosApiService {

    // 1. Crear Pedido
    @POST("api/pedidos")
    suspend fun crearPedido(@Body request: CrearPedidoRequest): Response<PedidoResponse>

    // 1.1 Listar pedidos en picking (En Curso)
    @GET("api/pedidos/picking")
    suspend fun getPedidosEnPicking(): Response<List<PedidoResponse>>

    // 2. Listar Pendientes
    @GET("api/pedidos/pendientes")
    suspend fun getPedidosPendientes(): Response<List<PedidoResponse>>

    // 2.1 Listar Todos
    @GET("api/pedidos/todos")
    suspend fun getPedidosTodos(): Response<List<PedidoResponse>>

    // 2.2 Listar Entregados
    @GET("api/pedidos/entregados")
    suspend fun getPedidosEntregados(): Response<List<PedidoResponse>>

    // 2.3 Listar Facturados (Para Revisión)
    @GET("api/pedidos/facturados")
    suspend fun getPedidosFacturados(): Response<List<PedidoResponse>>

    // 3. Asignarse Picking (POST /api/pedidos/{id}/asignar-picking)
    @POST("api/pedidos/{id}/asignar-picking")
    suspend fun asignarPicking(@Path("id") id: Int): Response<PedidoResponse> // Retorna pedido actualizado? Asumimos que sí

    // 4. Obtener Hoja de Picking
    @GET("api/pedidos/{id}/hoja-picking")
    suspend fun getHojaPicking(@Path("id") id: Int): Response<HojaPickingResponse>

    // 5. Confirmar Item (POST /api/pedidos/{id}/confirmar-item)
    // Importante: En la URL va el ID del pedido
    @POST("api/pedidos/{id}/confirmar-item")
    suspend fun confirmarItem(
        @Path("id") id: Int,
        @Body request: ConfirmarItemRequest
    ): Response<PedidoResponse> // Asumimos retorna el pedido actualizado con items confirmados

    // 6. Completar Picking (PUT /api/pedidos/{id}/completar-picking)
    @PUT("api/pedidos/{id}/completar-picking")
    suspend fun completarPicking(@Path("id") id: Int): Response<Unit>

    // 7. Registrar Factura (PUT /api/pedidos/{id}/registrar-factura)
    @PUT("api/pedidos/{id}/registrar-factura")
    suspend fun registrarFactura(
        @Path("id") id: Int,
        @Body request: RegistrarFacturaRequest
    ): Response<PedidoResponse>

    // 8. Revisión Factura (PUT /api/pedidos/{id}/revision-factura)
    @PUT("api/pedidos/{id}/revision-factura")
    suspend fun revisionFactura(@Path("id") id: Int): Response<Unit>

    // 9. Asignar Transporte (PUT /api/pedidos/{id}/asignar-transporte)
    @PUT("api/pedidos/{id}/asignar-transporte")
    suspend fun asignarTransporte(@Path("id") id: Int): Response<Unit>

    // 10. Iniciar Transporte (PUT /api/pedidos/{id}/iniciar-transporte)
    @PUT("api/pedidos/{id}/iniciar-transporte")
    suspend fun iniciarTransporte(@Path("id") id: Int): Response<Unit>

    // 11. Finalizar Entrega (POST /api/pedidos/{id}/finalizar-entrega)
    @POST("api/pedidos/{id}/finalizar-entrega")
    suspend fun finalizarEntrega(
        @Path("id") id: Int,
        @Body request: FinalizarEntregaRequest
    ): Response<Unit>

    // 12. Estadísticas (GET /api/pedidos/stats)
    @GET("api/pedidos/stats")
    suspend fun getEstadisticas(): Response<PedidoStats>

    // 13. Detalle completo del pedido (Trazabilidad)
    @GET("api/pedidos/{id}")
    suspend fun getPedidoDetalle(@Path("id") id: Long): Response<PedidoDetalleDTO>
}


