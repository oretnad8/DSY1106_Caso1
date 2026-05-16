package com.smartlogix.app.models

/**
 * Modelo de datos para Ubicación en bodega
 * Representa una posición física donde se almacenan productos
 * Estructura actualizada: 5 pasillos × 60 posiciones × 3 pisos = 900 ubicaciones totales
 * Formato de código: P{pasillo}-{piso}-{numero} (ej: P1-A-15, P3-B-42, P5-C-01)
 */
data class Ubicacion(
    val idUbicacion: Int,
    val codigoUbicacion: String, // Formato: P{pasillo}-{piso}-{numero} (ej: P1-A-12, P3-B-45)
    val pasillo: Int, // 1 a 5
    val piso: Char, // 'A', 'B', o 'C'
    val numero: Int, // 1 a 60
    val nivel: Int? = null, // Nivel para estantes (P3)
    val esEstante: Boolean = false, // Indica si es un estante multinivel
    val productos: List<ProductoEnUbicacion>? = null // Productos en esta ubicación
) {
    /**
     * Retorna un nombre amigable para mostrar en la interfaz.
     * Ejemplo: "P3-A/EST-13,1" -> "Pasillo 3 - 13"
     */
    val formattedDisplayName: String
        get() = if (esEstante) "Pasillo $pasillo - $numero" else codigoUbicacion
}

/**
 * Producto almacenado en una ubicación específica
 */
data class ProductoEnUbicacion(
    val sku: String,
    val descripcion: String,
    val cantidad: Int
)

/**
 * Request para asignar un producto a una ubicación
 * Usado tanto por jefe (directo) como por operador (requiere aprobación)
 */
data class AsignarUbicacionRequest(
    val sku: String,
    val codigoUbicacion: String, // Formato: P{pasillo}-{piso}-{numero}
    val cantidad: Int
)

/**
 * Enum para los pisos de la bodega
 */
enum class Piso(val codigo: Char, val label: String) {
    A('A', "Piso A"),
    B('B', "Piso B"),
    C('C', "Piso C");

    companion object {
        fun fromChar(c: Char): Piso? = values().find { it.codigo == c }
    }
}

/**
 * Enum para los pasillos de la bodega
 */
enum class Pasillo(val numero: Int, val label: String) {
    P1(1, "Pasillo 1"),
    P2(2, "Pasillo 2"),
    P3(3, "Pasillo 3"),
    P4(4, "Pasillo 4"),
    P5(5, "Pasillo 5");

    companion object {
        fun fromNumero(n: Int): Pasillo? = values().find { it.numero == n }
    }
}

/**
 * Utilidad para formatear códigos de ubicación
 */
object UbicacionFormatter {
    /**
     * Formatea un código de ubicación al formato estándar P{pasillo}-{piso}-{numero}
     * Ejemplo: formatCodigo(1, 'A', 5) -> "P1-A-05"
     */
    fun formatCodigo(pasillo: Int, piso: Char, numero: Int): String {
        return "P$pasillo-$piso-${numero.toString().padStart(2, '0')}"
    }

    /**
     * Normaliza los datos de escaneo al formato de la base de datos
     * Entrada: "Pasillo 1/Rack A1" -> Salida: "P1-A-01" (Standard)
     * Entrada: "Pasillo 3/EST A31,2" -> Salida: "P3-A/EST-31,2" (Especial)
     */
    fun normalizeScanToCode(scannedData: String): String {
        val input = scannedData.trim()
        
        // Caso Especial: Pasillo 3 con estantes (EST) y nivel (coma)
        // Ejemplo: "Pasillo 3/EST A31,2" -> "P3-A/EST-31,2"
        if (input.contains("EST", ignoreCase = true) && input.contains(",")) {
            val regex = Regex("""Pasillo\s+3/EST\s+([ABC])(\d+),(\d+)""", RegexOption.IGNORE_CASE)
            val match = regex.find(input)
            if (match != null) {
                val piso = match.groupValues[1].uppercase()
                val numero = match.groupValues[2]
                val nivel = match.groupValues[3]
                return "P3-$piso/EST-${numero},${nivel}"
            }
        }
        
        // Caso Estándar: "Pasillo X/Rack YZ" -> "PX-Y-ZZ"
        // Ejemplo: "Pasillo 1/Rack A1" -> "P1-A-01"
        val standardRegex = Regex("""Pasillo\s+(\d)/Rack\s+([ABC])(\d+)""", RegexOption.IGNORE_CASE)
        val standardMatch = standardRegex.find(input)
        if (standardMatch != null) {
            val pasillo = standardMatch.groupValues[1]
            val piso = standardMatch.groupValues[2].uppercase()
            val numero = standardMatch.groupValues[3].padStart(2, '0')
            return "P$pasillo-$piso-$numero"
        }
        
        return input // Retornar original si no coincide con patrones conocidos
    }

    /**
     * Parsea un código escaneado (formato P1/A1 o formatos nuevos) al formato estándar
     */
    fun parseScannedCode(scannedCode: String): String? {
        val normalized = normalizeScanToCode(scannedCode)
        
        // Si ya está normalizado o es un código directo
        if (normalized.startsWith("P")) return normalized

        // Mantener compatibilidad con el regex anterior si falla la normalización
        val regex = Regex("""P(\d)/([ABC])(\d+)""")
        val match = regex.matchEntire(scannedCode.trim().uppercase())
        
        return match?.let {
            val pasillo = it.groupValues[1].toInt()
            val piso = it.groupValues[2][0]
            val numero = it.groupValues[3].toInt()
            
            // Validar rangos
            if (pasillo in 1..5 && piso in listOf('A', 'B', 'C') && numero in 1..60) {
                formatCodigo(pasillo, piso, numero)
            } else {
                null
            }
        }
    }

    /**
     * Extrae los componentes de un código de ubicación
     * Retorna Triple(pasillo, piso, numero) o null si el formato es inválido
     */
    fun parseCodigo(codigo: String): Triple<Int, Char, Int>? {
        val regex = Regex("""P(\d)-([ABC])-(\d+)""")
        val match = regex.matchEntire(codigo.trim().uppercase())
        
        return match?.let {
            val pasillo = it.groupValues[1].toInt()
            val piso = it.groupValues[2][0]
            val numero = it.groupValues[3].toInt()
            Triple(pasillo, piso, numero)
        }
    }

    /**
     * Retorna una representación legible de una ubicación.
     * Soporta formato estándar P1-A-12 y especial P3-A/EST-31,2
     */
    fun getFriendlyLocationName(codigo: String?): String {
        if (codigo.isNullOrEmpty()) return "Ubicación sin asignar"
        
        // Caso Especial: Pasillo 3 con estantes (P3-A/EST-31,2)
        if (codigo.contains("/EST-")) {
            val regex = Regex("""P3-([ABC])/EST-(\d+),(\d+)""")
            val match = regex.find(codigo)
            if (match != null) {
                val estante = match.groupValues[1]
                val numero = match.groupValues[2]
                val nivel = match.groupValues[3]
                return "Pasillo 3 - Estante $estante$numero - Nivel $nivel"
            }
        }
        
        // Caso Estándar: P1-A-12
        val triple = parseCodigo(codigo)
        if (triple != null) {
            return "Pasillo ${triple.first} - Piso ${triple.second} - Posición ${triple.third}"
        }
        
        return codigo // Retornar original si no se reconoce
    }

    /**
     * Formatea el estado del pedido a un nombre legible
     */
    fun formatEstadoPedido(estado: com.smartlogix.app.model.EstadoPedido?): String {
        if (estado == null) return "Desconocido"
        return estado.name.lowercase().replace("_", " ").split(" ")
            .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
    }
}


