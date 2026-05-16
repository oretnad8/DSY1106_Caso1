package com.smartlogix.app.network

import retrofit2.Response
import retrofit2.http.*

/**
 * Servicio API para productos (Puerto 8083)
 */
interface ProductosApiService {

    /**
     * GET /api/productos/search
     * Buscar productos (query opcional)
     */
    @GET("api/productos/search")
    suspend fun searchProductos(@Query("q") query: String? = null): Response<List<ProductoResponse>>

    /**
     * GET /api/productos/{sku}
     * Obtener producto por SKU
     */
    @GET("api/productos/{sku}")
    suspend fun getProductoBySku(@Path("sku") sku: String): Response<ProductoResponse>

    /**
     * POST /api/productos
     * Crear producto
     * Roles: ADMIN, JEFE, SUPERVISOR
     */
    @POST("api/productos")
    suspend fun createProducto(@Body request: ProductoRequest): Response<ProductoResponse>

    /**
     * PUT /api/productos/{sku}
     * Actualizar producto
     * Roles: ADMIN, JEFE, SUPERVISOR
     */
    @PUT("api/productos/{sku}")
    suspend fun updateProducto(
        @Path("sku") sku: String,
        @Body request: ProductoRequest
    ): Response<ProductoResponse>

    /**
     * DELETE /api/productos/{sku}
     * Eliminar producto
     * Roles: ADMIN, JEFE
     */
    @DELETE("api/productos/{sku}")
    suspend fun deleteProducto(@Path("sku") sku: String): Response<Unit>
}


