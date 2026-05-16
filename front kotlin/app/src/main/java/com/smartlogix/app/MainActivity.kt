@file:OptIn(ExperimentalAnimationApi::class)

package com.smartlogix.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.smartlogix.app.navigation.Screen
import com.smartlogix.app.SmartLogixApplication
import com.smartlogix.app.ui.screen.*
import com.smartlogix.app.ui.theme.TestTheme
import com.smartlogix.app.viewmodels.*

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * MainActivity - Actividad principal de la aplicación SmartLogix
 *
 * Gestiona:
 * - Navegación entre todas las pantallas
 * - Instancias de ViewModels compartidos
 * - Tema de la aplicación
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TestTheme(darkTheme = true) { // Default to Dark Navy for WMS style
                SmartLogixApp()
            }
        }
    }
}


@Composable
fun SmartLogixApp() {
    // Obtener Application
    val app = LocalContext.current.applicationContext as SmartLogixApplication
    
    // NavController para navegación
    val navController = rememberNavController()

    // ViewModels compartidos entre pantallas
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    // ViewModels que requieren repositorios (usar factory)
    val productoViewModel: ProductoViewModel = viewModel(
        factory = ViewModelFactory(
            application = app,
            productoRepository = app.productoRepository
        )
    )
    
    val ubicacionViewModel: UbicacionViewModel = viewModel(
        factory = ViewModelFactory(
            application = app,
            ubicacionRepository = app.ubicacionRepository
        )
    )
    
    // ViewModels que solo requieren Application
    val aprobacionViewModel: AprobacionViewModel = viewModel()
    val usuarioViewModel: UsuarioViewModel = viewModel()
    val registroDirectoViewModel: RegistroDirectoViewModel = viewModel()
    
    val pedidosViewModel: PedidosViewModel = viewModel(
        factory = ViewModelFactory(
            application = app,
            pedidosRepository = app.pedidosRepository
        )
    )

    val pendingOrdersCount by app.pollingManager.pendingOrdersCount.collectAsStateWithLifecycle()


    LaunchedEffect(authState) {
        if (authState is com.smartlogix.app.models.AuthState.NotAuthenticated) {
            navController.navigate(Screen.Login.route) {
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }

    // Root container with Navy background to prevent white flashes
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.fillMaxSize(),
            //duracion de transisciones globales aplicadas a todas las pantallas
            //Transicion para entrar
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it / 3 },  // entra desde la derecha
                    animationSpec = tween(900, easing = FastOutSlowInEasing)
                ) + fadeIn(tween(700))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it / 3 }, // sale a la izquierda
                    animationSpec = tween(750, easing = LinearOutSlowInEasing)
                ) + fadeOut(tween(400))
            },
            //Transicion para volver atras
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it / 3 }, // entra desde la izquierda (al volver)
                    animationSpec = tween(900)
                ) + fadeIn(tween(700))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it / 3 },
                    animationSpec = tween(750)
                ) + fadeOut(tween(400))
            }
        ) {
            // ========== LOGIN ==========
            composable(
                route = Screen.Login.route
            ){
                LoginScreen(
                    authViewModel = authViewModel,
                    onNavigateToHome = { route ->
                        navController.navigate(route) {
                            // Limpiar el stack al hacer login
                            popUpTo(Screen.Login.route) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }

            // ========== DASHBOARDS ==========

            // Dashboard Admin
            composable(
                route = Screen.DashboardAdmin.route
            ) {
                DashboardAdminScreen(
                    authViewModel = authViewModel,
                    usuarioViewModel = usuarioViewModel,
                    onNavigate = { route ->
                        navController.navigate(route)
                    }
                )
            }

            // Dashboard Jefe
            composable(route = Screen.DashboardJefe.route) {
                DashboardJefeScreen(
                    authViewModel = authViewModel,
                    aprobacionViewModel = aprobacionViewModel,
                    onNavigate = { route ->
                        navController.navigate(route)
                    }
                )
            }

            // Dashboard Supervisor (usa el mismo que Jefe)
            composable(route = Screen.DashboardSupervisor.route) {
                DashboardJefeScreen(
                    authViewModel = authViewModel,
                    aprobacionViewModel = aprobacionViewModel,
                    onNavigate = { route ->
                        navController.navigate(route)
                    }
                )
            }

            // Dashboard Operador
            composable(route = Screen.DashboardOperador.route) {
                DashboardOperadorScreen(
                    authViewModel = authViewModel,
                    onNavigate = { route ->
                        navController.navigate(route)
                    }
                )
            }

            // ========== BÚSQUEDA Y PRODUCTOS ==========

            // Búsqueda de productos
            composable(route = Screen.Busqueda.route) {
                BusquedaScreen(
                    productoViewModel = productoViewModel,
                    onNavigateToDetail = { sku ->
                        navController.navigate("detalle_producto/$sku")
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Detalle de producto
            composable(
                route = "detalle_producto/{sku}",
                arguments = listOf(
                    navArgument("sku") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val sku = backStackEntry.arguments?.getString("sku") ?: ""
                DetalleProductoScreen(
                    sku = sku,
                    productoViewModel = productoViewModel,
                    ubicacionViewModel = ubicacionViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToUbicacion = { idUbicacion ->
                        navController.navigate("detalle_ubicacion/$idUbicacion")
                    },
                    onNavigateToAsignarUbicacion = { sku ->
                        navController.navigate("asignar_ubicacion/$sku")
                    }
                )
            }

            // ========== UBICACIONES ==========

            // Gestión de ubicaciones
            composable(route = Screen.GestionUbicaciones.route) {
                GestionUbicacionesScreen(
                    ubicacionViewModel = ubicacionViewModel,
                    onNavigateToDetail = { idUbicacion ->
                        navController.navigate("detalle_ubicacion/$idUbicacion")
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = "detalle_ubicacion/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("id") ?: return@composable
                DetalleUbicacionScreen(
                    id = id,
                    ubicacionViewModel = ubicacionViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToProducto = { sku ->
                        navController.navigate("detalle_producto/$sku")
                    },
                    onNavigateToConteo = { idUbicacion ->
                        navController.navigate("conteo_ubicacion/$idUbicacion")
                    }
                )
            }

            // ========== MOVIMIENTOS ==========

            // Solicitud de movimiento (Operadores)
            composable(route = Screen.SolicitudMovimiento.route) {
                SolicitudMovimientoScreen(
                    aprobacionViewModel = aprobacionViewModel,
                    productoViewModel = productoViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Registro directo (Jefe/Supervisor)
            composable(route = Screen.RegistroDirecto.route) {
                RegistroDirectoScreen(
                    registroDirectoViewModel = registroDirectoViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // ========== APROBACIONES ==========

            // Lista de aprobaciones
            composable(route = Screen.Aprobaciones.route) {
                AprobacionesScreen(
                    authViewModel = authViewModel,
                    aprobacionViewModel = aprobacionViewModel,
                    onNavigateToDetail = { id ->
                        navController.navigate("detalle_aprobacion/$id")
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Detalle de aprobación
            composable(
                route = "detalle_aprobacion/{id}",
                arguments = listOf(
                    navArgument("id") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("id") ?: 0
                DetalleAprobacionScreen(
                    aprobacionId = id,
                    aprobacionViewModel = aprobacionViewModel,
                    authViewModel = authViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Mis solicitudes (Operadores)
            composable(route = Screen.MisSolicitudes.route) {
                // Reutiliza AprobacionesScreen pero carga solo las del usuario
                AprobacionesScreen(
                    authViewModel = authViewModel,
                    aprobacionViewModel = aprobacionViewModel,
                    onNavigateToDetail = { id ->
                        navController.navigate("detalle_aprobacion/$id")
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )

                // Cargar solo mis solicitudes
                LaunchedEffect(Unit) {
                    aprobacionViewModel.getMisSolicitudes()
                }
            }



            // ========== GESTIÓN DE USUARIOS (ADMIN) ==========

            // Lista de usuarios
            composable(route = Screen.GestionUsuarios.route) {
                GestionUsuariosScreen(
                    usuarioViewModel = usuarioViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Crear usuario
            composable(route = Screen.CrearUsuario.route) {
                // La creación se hace desde GestionUsuariosScreen con diálogo
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }

            // ========== PERFIL Y CONFIGURACIÓN ==========

            // Perfil del usuario
            composable(route = Screen.Perfil.route) {
                PerfilScreen(
                    authViewModel = authViewModel,
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            // Limpiar todo el stack
                            popUpTo(0) {
                                inclusive = true
                            }
                        }
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Configuración
            composable(Screen.Configuracion.route) {
                ConfiguracionScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // ========== GESTIÓN DE PEDIDOS ==========
            

            // Lista de Pedidos
            composable(
                route = Screen.ListaPedidos.route,
                arguments = listOf(
                    navArgument("isMyOrders") { 
                        type = NavType.BoolType
                        defaultValue = false 
                    }
                )
            ) { backStackEntry ->
                val isMyOrders = backStackEntry.arguments?.getBoolean("isMyOrders") ?: false
                com.smartlogix.app.ui.pedidos.ListaPedidosScreen(
                    viewModel = pedidosViewModel,
                    isMyOrders = isMyOrders,
                    onNavigateToDetail = { pedido ->
                        val userRole = app.getCurrentUserRole()
                        val isJefe = userRole == "JEFE" || userRole == "SUPERVISOR" || userRole == "ADMIN"
                        
                        val destino = when {
                            pedido.estado == com.smartlogix.app.model.EstadoPedido.ENTREGADO -> {
                                "detalle_entregado/${pedido.id}"
                            }
                            pedido.estado != null && pedido.estado >= com.smartlogix.app.model.EstadoPedido.PICKING_COMPLETADO -> {
                                if (isJefe) {
                                    "despacho/${pedido.id}"
                                } else if (pedido.estado >= com.smartlogix.app.model.EstadoPedido.REVISION_FACTURA) {
                                    "despacho/${pedido.id}"
                                } else null
                            }
                            else -> "picking/${pedido.id}"
                        }
                        
                        destino?.let { navController.navigate(it) }
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Picking (y Despacho según estado, aunque aquí definimos ruta de picking)
            composable(
                route = "picking/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("id") ?: 0
                com.smartlogix.app.ui.pedidos.PickingScreen(
                    pedidoId = id,
                    pedidosViewModel = pedidosViewModel,
                    productoViewModel = productoViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onPickingFinished = {
                         val userRole = app.getCurrentUserRole()
                         if (userRole == "JEFE" || userRole == "SUPERVISOR" || userRole == "ADMIN") {
                             navController.navigate("despacho/$id") {
                                popUpTo("picking/$id") { inclusive = true }
                             }
                         } else {
                             // Operador vuelve a la lista tras terminar
                             navController.popBackStack()
                         }
                    }
                )
            }

            // Despacho
            composable(
                route = "despacho/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("id") ?: 0
                com.smartlogix.app.ui.pedidos.DespachoScreen(
                    pedidoId = id,
                    pedidosViewModel = pedidosViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onDeliveryFinished = {
                        navController.popBackStack() // Volver a lista o dashboard
                    }
                )
            }

            // Estadísticas
            composable(Screen.Estadisticas.route) {
                com.smartlogix.app.ui.pedidos.StatsDashboardScreen(
                    viewModel = pedidosViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEntregas = { navController.navigate(Screen.Entregas.route) }
                )
            }

            // Historial de Entregas
            composable(Screen.Entregas.route) {
                com.smartlogix.app.ui.pedidos.ListaPedidosScreen(
                    viewModel = pedidosViewModel,
                    showDelivered = true,
                    onNavigateToDetail = { pedido ->
                        navController.navigate("detalle_entregado/${pedido.id}")
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Detalle de Pedido Entregado
            composable(
                route = "detalle_entregado/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong("id") ?: 0L
                com.smartlogix.app.ui.pedidos.DetalleEntregadoScreen(
                    pedidoId = id,
                    viewModel = pedidosViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Revisión de Pedidos (Facturados)
            composable(Screen.RevisarPedidos.route) {
                com.smartlogix.app.ui.pedidos.ListaPedidosScreen(
                    viewModel = pedidosViewModel,
                    isRevisionMode = true,
                    onNavigateToDetail = { /* No detail needed for quick revision since it's only FACTURADO */ },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

