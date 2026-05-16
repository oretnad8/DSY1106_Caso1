package com.smartlogix.app.model

import com.google.gson.annotations.SerializedName

/**
 * Estados posibles de un pedido según el flujo definido por el usuario
 */
enum class EstadoPedido {
    PENDIENTE,
    PICKING_ASIGNADO,
    EN_PICKING,
    PICKING_COMPLETADO,
    FACTURADO,
    REVISION_FACTURA,
    TRANSPORTE_ASIGNADO,
    EN_TRANSPORTE,
    ENTREGADO
}

/**
 * Respuesta principal de un Pedido desde la API
 */
data class PedidoResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("cliente") val cliente: String,
    @SerializedName("vendedorId") val vendedorId: Int? = null,
    @SerializedName("vendedorNombre") val vendedorNombre: String? = null,
    @SerializedName(value = "estado", alternate = ["estadoActual"]) val estado: EstadoPedido? = null,
    @SerializedName("fechaCreacion") val fechaCreacion: String,
    @SerializedName("numeroFactura") val numeroFactura: String? = null,
    @SerializedName(value = "urlFactura", alternate = ["urlFacturaDigital"]) val urlFactura: String? = null,
    @SerializedName("fotoEntrega") val fotoEntrega: String? = null,
    @SerializedName("operadorId") val operadorId: Int? = null,
    @SerializedName("operadorNombre") val operadorNombre: String? = null,
    @SerializedName("fechaEntrega") val fechaEntrega: String? = null,
    @SerializedName("totalItems") val totalItems: Int? = null,
    @SerializedName("detalles") val detalles: List<DetallePedido>? = null,
    @SerializedName("historial") val historial: List<HistorialEstado>? = null
)

/**
 * Respuesta específica de Hoja de Picking
 */
data class HojaPickingResponse(
    @SerializedName("pedidoId") val id: Int,
    @SerializedName("cliente") val cliente: String,
    @SerializedName("fechaCreacion") val fechaCreacion: String,
    @SerializedName("estado") val estado: EstadoPedido,
    @SerializedName("operadorId") val operadorId: Int? = null,
    @SerializedName("operadorNombre") val operadorNombre: String? = null,
    @SerializedName("items") val items: List<DetallePedido>
) {
    // Función de extensión para convertir a PedidoResponse si es necesario unificar
    fun toPedidoResponse() = PedidoResponse(
        id = id,
        cliente = cliente,
        estado = estado,
        fechaCreacion = fechaCreacion,
        operadorId = operadorId,
        operadorNombre = operadorNombre,
        detalles = items
    )
}

/**
 * Detalle de un item dentro del pedido
 */
data class DetallePedido(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("sku") val sku: String,
    @SerializedName("descripcion") val descripcion: String? = null,
    @SerializedName("cantidadSolicitada") val cantidadSolicitada: Int,
    @SerializedName("cantidadPickeada") val cantidadPickeada: Int = 0,
    @SerializedName("ubicacionSugerida") val ubicacionSugerida: String? = null
)

/**
 * Historial de cambios de estado
 */
data class HistorialEstado(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("estado") val estado: String,
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("usuarioId") val usuarioId: Int,
    @SerializedName("usuarioNombre") val usuarioNombre: String
)

/**
 * Estadísticas del Dashboard (Estructura actualizada)
 */
data class OperadorStats(
    @SerializedName("nombre") val nombre: String,
    @SerializedName("totalAsignados") val totalAsignados: Int,
    @SerializedName("entregados") val entregados: Int,
    @SerializedName("eficiencia") val eficiencia: Double
)

data class PedidoStats(
    @SerializedName("pedidosTotales") val pedidosTotales: Int,
    @SerializedName("eficienciaGlobal") val eficienciaGlobal: Double,
    @SerializedName("promedioPickingMinutos") val promedioPickingMinutos: Double,
    @SerializedName("operadoresActivos") val operadoresActivos: Int,
    @SerializedName("eficienciaPorOperador") val eficienciaPorOperador: Map<String, OperadorStats>,
    @SerializedName("distribucionEstados") val distribucionEstados: Map<String, Int>
)

/**
 * Request Body para Crear Pedido
 */
data class CrearPedidoRequest(
    @SerializedName("cliente") val cliente: String,
    @SerializedName("vendedorId") val vendedorId: Int,
    @SerializedName("detalles") val detalles: List<DetallePedido>
)

/**
 * Request Body para Confirmar Item
 */
data class ConfirmarItemRequest(
    @SerializedName("sku") val sku: String,
    @SerializedName("cantidad") val cantidad: Int,
    @SerializedName("codigoUbicacion") val codigoUbicacion: String
)

/**
 * Request Body para Registrar Factura
 */
data class RegistrarFacturaRequest(
    @SerializedName("urlFactura") val urlFactura: String
)

/**
 * Request Body para Finalizar Entrega
 */
data class FinalizarEntregaRequest(
    @SerializedName("fotoEvidencia") val fotoEvidencia: String
)

/**
 * DTOs para el detalle completo de un pedido (Vista de Entregados / Auditoría)
 */
data class ResponsableDTO(
    @SerializedName("id") val id: Int?,
    @SerializedName("nombre") val nombre: String?
)

data class ItemDetalleDTO(
    @SerializedName("sku") val sku: String,
    @SerializedName("descripcion") val descripcion: String,
    @SerializedName("cantidadSolicitada") val cantidadSolicitada: Int,
    @SerializedName("cantidadPickeada") val cantidadPickeada: Int,
    @SerializedName("ubicacionSugerida") val ubicacionSugerida: String?
)

data class HistorialEstadoDTO(
    @SerializedName("estado") val estado: String,
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("usuarioId") val usuarioId: Int,
    @SerializedName("usuarioNombre") val usuarioNombre: String
)

data class PedidoDetalleDTO(
    @SerializedName("id") val id: Long,
    @SerializedName("cliente") val cliente: String,
    @SerializedName("fechaCreacion") val fechaCreacion: String,
    @SerializedName("fechaEntrega") val fechaEntrega: String?,
    @SerializedName("estadoActual") val estadoActual: String,
    @SerializedName("numeroFactura") val numeroFactura: String?,
    @SerializedName("fotoEntrega") val fotoEntrega: String?,
    @SerializedName("vendedor") val vendedor: ResponsableDTO?,
    @SerializedName("operadorPicking") val operadorPicking: ResponsableDTO?,
    @SerializedName("operadorTransporte") val operadorTransporte: ResponsableDTO?,
    @SerializedName("detalles") val detalles: List<ItemDetalleDTO>,
    @SerializedName("historial") val historial: List<HistorialEstadoDTO>
)


