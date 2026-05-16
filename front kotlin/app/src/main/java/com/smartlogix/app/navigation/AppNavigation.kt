package com.smartlogix.app.navigation

/**
 * Sistema de navegación centralizado de la aplicación
 * Define todas las rutas disponibles según el rol del usuario
 */
sealed class Screen(val route: String) {
    // ========== PANTALLAS COMUNES ==========

    /**
     * Pantalla de inicio de sesión
     */
    object Login : Screen("login")

    // ========== DASHBOARDS POR ROL ==========

    /**
     * Dashboard para rol ADMIN
     * Funciones: Gestión de usuarios, descarga de resúmenes
     */
    object DashboardAdmin : Screen("dashboard_admin")

    /**
     * Dashboard para rol JEFE
     * Funciones: Alertas, mensajes, aprobaciones, acciones rápidas
     */
    object DashboardJefe : Screen("dashboard_jefe")

    /**
     * Dashboard para rol SUPERVISOR
     * Similar al jefe pero con notificación automática al jefe
     */
    object DashboardSupervisor : Screen("dashboard_supervisor")

    /**
     * Dashboard para rol OPERADOR
     * Funciones: Mensajes del jefe, tareas pendientes
     */
    object DashboardOperador : Screen("dashboard_operador")

    // ========== BÚSQUEDA Y PRODUCTOS ==========

    /**
     * Pantalla de búsqueda de productos
     * Permite búsqueda por cámara o manual (SKU, descripción, ubicación)
     */
    object Busqueda : Screen("busqueda")

    /**
     * Detalle de un producto específico
     * Muestra información completa, ubicaciones, stock, etc.
     */
    data class DetalleProducto(val sku: String) : Screen("detalle_producto/{sku}") {
        fun buildRoute(): String = "detalle_producto/$sku"
    }

    // ========== GESTIÓN DE UBICACIONES ==========

    /**
     * Gestión de ubicaciones
     * JEFE: Asignación directa
     * OPERADOR: Requiere aprobación
     */
    object GestionUbicaciones : Screen("gestion_ubicaciones")

    /**
     * Vista de una ubicación específica
     * Muestra productos almacenados en esa ubicación
     */
    data class DetalleUbicacion(val codigo: String) : Screen("detalle_ubicacion/{codigo}") {
        fun buildRoute(): String = "detalle_ubicacion/$codigo"
    }

    // ========== MOVIMIENTOS ==========

    /**
     * Solicitud de movimiento (ingreso/egreso/reubicación)
     * Para OPERADORES - requiere aprobación
     */
    object SolicitudMovimiento : Screen("solicitud_movimiento")

    /**
     * Registro directo de movimiento
     * Solo para JEFE/SUPERVISOR - sin aprobación
     */
    object RegistroDirecto : Screen("registro_directo")

    // ========== APROBACIONES ==========

    /**
     * Lista de solicitudes pendientes de aprobación
     * Solo para JEFE/SUPERVISOR
     */
    object Aprobaciones : Screen("aprobaciones")

    /**
     * Detalle de una solicitud específica
     * Permite aprobar/rechazar con observaciones
     */
    data class DetalleAprobacion(val id: Int) : Screen("detalle_aprobacion/{id}") {
        fun buildRoute(): String = "detalle_aprobacion/$id"
    }

    /**
     * Historial de mis solicitudes
     * Para OPERADORES ver estado de sus solicitudes
     */
    object MisSolicitudes : Screen("mis_solicitudes")



    // ========== GESTIÓN DE USUARIOS ==========

    /**
     * Lista de usuarios del sistema
     * Solo para ADMIN
     */
    object GestionUsuarios : Screen("gestion_usuarios")

    /**
     * Crear nuevo usuario
     * Solo para ADMIN
     */
    object CrearUsuario : Screen("crear_usuario")

    // ========== PERFIL Y CONFIGURACIÓN ==========

    /**
     * Perfil del usuario actual
     * Ver/editar información personal
     */
    object Perfil : Screen("perfil")

    /**
     * Configuración de la app
     */
    /**
     * Configuración de la app
     */
    object Configuracion : Screen("configuracion")

    // ========== GESTIÓN DE PEDIDOS ==========

    object ListaPedidos : Screen("lista_pedidos?isMyOrders={isMyOrders}") {
        fun buildRoute(isMyOrders: Boolean = false) = "lista_pedidos?isMyOrders=$isMyOrders"
    }

    data class Picking(val id: Int) : Screen("picking/{id}") {
        fun buildRoute(id: Int) = "picking/$id"
    }

    data class Despacho(val id: Int) : Screen("despacho/{id}") {
        fun buildRoute(id: Int) = "despacho/$id"
    }

    data class DetalleEntregado(val id: Long) : Screen("detalle_entregado/{id}") {
        fun buildRoute(id: Long) = "detalle_entregado/$id"
    }

    object Estadisticas : Screen("estadisticas")
    object Entregas : Screen("entregas")
    object RevisarPedidos : Screen("revisar_pedidos")
}

/**
 * Obtiene la ruta del dashboard según el rol del usuario
 */
fun getDashboardForRole(rol: String): Screen {
    return when (rol.uppercase()) {
        "ADMIN" -> Screen.DashboardAdmin
        "JEFE" -> Screen.DashboardJefe
        "SUPERVISOR" -> Screen.DashboardSupervisor
        "OPERADOR" -> Screen.DashboardOperador
        else -> Screen.Login
    }
}

// Ruta simple para Configuración
data object Configuracion : Screen("configuracion")


    /**
     * Vista de una ubicación específica
     * Muestra productos almacenados en esa ubicación
     */
    data class DetalleUbicacion(val id: Int) : Screen("detalle_ubicacion/{id}") {
        fun buildRoute(): String = "detalle_ubicacion/$id"
    }

    /**
     * Conteo de ubicación (recibe argumento `idUbicacion`)
     */
    data class ConteoUbicacion(val id: Int) : Screen("conteo_ubicacion/{id}") {
        fun buildRoute(): String = "conteo_ubicacion/$id"
    }



