package com.smartlogix.app.models

/**
 * Modelo de datos para Producto
 * Representa un producto fotográfico en la bodega
 */
data class Producto(
    val sku: String, // Formato: 2 letras + 5 números (ej: AP30001)
    val descripcion: String,
    val stock: Int,
    val codigoBarrasIndividual: String?, // Código de barras del producto individual
    val lpn: String?, // Código de caja (License Plate Number)
    val lpnDesc: String?, // Descripción del LPN
    val fechaVencimiento: String?, // Formato ISO: "2025-12-31"
    val vencimientoCercano: Boolean = false, // True si quedan menos de 2 meses
    val ubicaciones: List<ProductoUbicacion>? = null // Ubicaciones donde está el producto
)

/**
 * Relación entre Producto y Ubicación con cantidad
 */
data class ProductoUbicacion(
    val idUbicacion: Int,
    val codigoUbicacion: String, // ej: A-12
    val cantidad: Int
)

/**
 * Request para crear/actualizar producto
 */
data class ProductoRequest(
    val sku: String,
    val descripcion: String,
    val stock: Int,
    val codigoBarrasIndividual: String? = null,
    val lpn: String? = null,
    val lpnDesc: String? = null,
    val fechaVencimiento: String? = null
)

/**
 * Resultado de búsqueda de productos
 * Puede buscar por SKU, descripción, código de barras, LPN o ubicación
 */
data class ProductoBusqueda(
    val productos: List<Producto>,
    val totalResultados: Int
)

