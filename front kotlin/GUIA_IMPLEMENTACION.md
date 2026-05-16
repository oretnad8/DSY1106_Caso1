# Guía de Implementación - FotomarWMS

## Resumen de Cambios Implementados

### 1. Capa de Persistencia (Room Database)

#### Nuevas Entidades Creadas
- **ProductoLocal**: Para cachear productos y operaciones pendientes (CREATE/UPDATE)
- **UbicacionLocal**: Para cachear ubicaciones del backend
- **AprobacionLocal**: Para solicitudes de aprobación pendientes

#### Entidades Actualizadas
- **SolicitudMovimientoLocal**: Mantiene estructura original
- **ConteoLocal**: Mantiene estructura original
- **MensajeLocal**: Mantiene estructura original
- **AsignacionUbicacionLocal**: Mantiene estructura original
- **UsuarioLocal**: Mantiene estructura original

#### DAOs Actualizados (Nombres Consistentes)
Todos los DAOs ahora tienen métodos con nombres consistentes:
- `getAllPendientes()`: Obtener todos los registros pendientes
- `getPendienteById(id)`: Obtener un registro por ID
- `insert(entity)`: Insertar un registro
- `insertAll(list)`: Insertar múltiples registros
- `update(entity)`: Actualizar un registro
- `delete(entity)`: Eliminar un registro
- `deleteById(id)`: Eliminar por ID
- `deleteAll()`: Eliminar todos
- `countPendientes()`: Contar registros pendientes

#### AppDatabase Actualizado
- Versión incrementada a **6**
- Incluye todas las entidades nuevas
- Todos los DAOs registrados

### 2. Capa de Red (Retrofit)

#### Dependencias Agregadas
```kotlin
// Retrofit + Networking
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
implementation("com.google.code.gson:gson:2.10.1")
```

#### Servicios API Implementados
1. **AuthApiService** (Puerto 8081)
   - Login, Logout, Validate Token

2. **UsuariosApiService** (Puerto 8082)
   - CRUD completo de usuarios
   - Toggle activo/inactivo

3. **ProductosApiService** (Puerto 8083)
   - Búsqueda de productos
   - CRUD completo de productos
   - Obtener por SKU

4. **UbicacionesApiService** (Puerto 8084)
   - Listar ubicaciones (con filtro por piso)
   - Obtener por código
   - Asignar producto a ubicación

5. **AprobacionesApiService** (Puerto 8085)
   - CRUD de solicitudes
   - Aprobar/Rechazar solicitudes
   - Ver mis solicitudes

6. **MensajesApiService** (Puerto 8086)
   - Listar mensajes (con filtros)
   - Enviar mensajes
   - Marcar como leído
   - Toggle importante

7. **InventarioApiService** (Puerto 8087)
   - Ver progreso
   - Registrar conteo
   - Ver diferencias
   - Finalizar inventario

#### RetrofitClient
- URLs base configuradas para todos los microservicios
- Interceptor de autenticación (Bearer token)
- Logging interceptor para debug
- Timeouts configurados (30 segundos)

### 3. Repositorios (Patrón Local-First)

#### ProductoRepository
**Patrón implementado:**
1. Guardar en Room primero
2. Intentar enviar al backend
3. Si recibe código 200, eliminar de Room
4. Si falla, mantener en Room para sincronización posterior

**Métodos:**
- `searchProductos(query)`: Buscar productos en backend
- `getProductoBySku(sku)`: Obtener producto específico
- `createProducto(request)`: Crear con patrón local-first
- `updateProducto(sku, request)`: Actualizar con patrón local-first
- `deleteProducto(sku)`: Eliminar del backend
- `getPendientes()`: Flow de productos pendientes
- `syncPendientes()`: Sincronizar todos los pendientes

#### UbicacionRepository
**Funcionalidades:**
- Cache local de ubicaciones
- Asignación de productos con patrón local-first
- Filtrado por piso (A, B, C)

**Métodos:**
- `getUbicaciones(piso, forceRefresh)`: Con cache local
- `getUbicacionByCodigo(codigo)`: Obtener específica
- `asignarProducto(sku, codigo, cantidad)`: Con patrón local-first
- `getAsignacionesPendientes()`: Flow de asignaciones pendientes
- `syncAsignacionesPendientes()`: Sincronizar pendientes

### 4. Pantallas Nuevas/Mejoradas

#### DetalleProductoScreenNew
**Características:**
- Modo de visualización y edición
- Botones de escáner para código de barras y LPN
- Campos editables con visualización del código escaneado
- Botón para navegar a asignación de ubicaciones
- Lista de ubicaciones actuales del producto

**Estados:**
- `isEditMode`: Controla modo edición
- `showBarcodeScannerForIndividual`: Muestra escáner para código individual
- `showBarcodeScannerForLPN`: Muestra escáner para LPN
- Campos editables: `editCodigoBarras`, `editLpn`, `editLpnDesc`, `editDescripcion`, `editStock`

#### AsignarUbicacionScreen
**Características:**
- Selector de piso (A, B, C) con chips
- Input de número de ubicación (1-60) con validación
- Input de cantidad
- Vista previa del código generado (formato: A-12, B-05, etc.)
- Lista de asignaciones pendientes
- Posibilidad de asignar a múltiples ubicaciones
- Confirmación de todas las asignaciones

#### AsignarUbicacionDialog
**Versión compacta para usar como diálogo:**
- Misma funcionalidad que la pantalla completa
- Formato de diálogo modal
- Ideal para asignación rápida desde otras pantallas

### 5. Modelos de Datos

#### ApiModels.kt
Incluye todos los modelos de request y response:
- LoginRequest/Response
- UsuarioRequest/Response
- ProductoRequest/Response con ProductoUbicacionResponse
- UbicacionResponse
- AsignarUbicacionRequest
- AprobacionRequest/Response
- AprobarRequest/RechazarRequest
- MensajeRequest/Response
- ResumenMensajesResponse
- ConteoRequest
- ProgresoInventarioResponse
- DiferenciaInventarioResponse
- ApiResponse<T> genérico
- ApiError

## Integración con el Proyecto Existente

### Pasos para Integrar

#### 1. Actualizar ViewModel de Productos
El `ProductoViewModel` actual debe actualizarse para usar el `ProductoRepository`:

```kotlin
class ProductoViewModel(
    private val productoRepository: ProductoRepository
) : ViewModel() {
    
    fun searchProductos(query: String = "") {
        viewModelScope.launch {
            _searchState.value = UiState.Loading
            val result = productoRepository.searchProductos(query)
            _searchState.value = if (result.isSuccess) {
                UiState.Success(result.getOrNull()!!)
            } else {
                UiState.Error(result.exceptionOrNull()?.message ?: "Error desconocido")
            }
        }
    }
    
    fun updateProducto(sku: String, request: ProductoRequest) {
        viewModelScope.launch {
            _productoDetailState.value = UiState.Loading
            val result = productoRepository.updateProducto(sku, request)
            _productoDetailState.value = if (result.isSuccess) {
                UiState.Success(result.getOrNull()!!)
            } else {
                UiState.Error(result.exceptionOrNull()?.message ?: "Error al actualizar")
            }
        }
    }
}
```

#### 2. Crear ViewModel de Ubicaciones
```kotlin
class UbicacionViewModel(
    private val ubicacionRepository: UbicacionRepository
) : ViewModel() {
    
    private val _ubicacionesState = MutableStateFlow<UiState<List<Ubicacion>>>(UiState.Idle)
    val ubicacionesState: StateFlow<UiState<List<Ubicacion>>> = _ubicacionesState.asStateFlow()
    
    fun getUbicaciones(piso: String? = null) {
        viewModelScope.launch {
            _ubicacionesState.value = UiState.Loading
            val result = ubicacionRepository.getUbicaciones(piso)
            _ubicacionesState.value = if (result.isSuccess) {
                UiState.Success(result.getOrNull()!!)
            } else {
                UiState.Error(result.exceptionOrNull()?.message ?: "Error")
            }
        }
    }
    
    fun asignarProducto(sku: String, codigoUbicacion: String, cantidad: Int) {
        viewModelScope.launch {
            val result = ubicacionRepository.asignarProducto(sku, codigoUbicacion, cantidad)
            // Manejar resultado
        }
    }
}
```

#### 3. Actualizar Navigation
Agregar las nuevas rutas en `AppNavigation.kt`:

```kotlin
// Ruta para asignar ubicación
composable(
    route = "asignar_ubicacion/{sku}",
    arguments = listOf(navArgument("sku") { type = NavType.StringType })
) { backStackEntry ->
    val sku = backStackEntry.arguments?.getString("sku") ?: ""
    AsignarUbicacionScreen(
        sku = sku,
        onNavigateBack = { navController.popBackStack() },
        onAsignar = { sku, codigo, cantidad ->
            ubicacionViewModel.asignarProducto(sku, codigo, cantidad)
        }
    )
}

// Usar DetalleProductoScreenNew en lugar de DetalleProductoScreen
composable(
    route = "detalle_producto/{sku}",
    arguments = listOf(navArgument("sku") { type = NavType.StringType })
) { backStackEntry ->
    val sku = backStackEntry.arguments?.getString("sku") ?: ""
    DetalleProductoScreenNew(
        sku = sku,
        productoViewModel = productoViewModel,
        onNavigateBack = { navController.popBackStack() },
        onNavigateToUbicacion = { codigo ->
            navController.navigate("detalle_ubicacion/$codigo")
        },
        onNavigateToAsignarUbicacion = { sku ->
            navController.navigate("asignar_ubicacion/$sku")
        }
    )
}
```

#### 4. Inicializar Repositorios en MainActivity
```kotlin
class MainActivity : ComponentActivity() {
    
    private lateinit var database: AppDatabase
    private lateinit var productoRepository: ProductoRepository
    private lateinit var ubicacionRepository: UbicacionRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicializar base de datos
        database = AppDatabase.getDatabase(applicationContext)
        
        // Inicializar repositorios
        productoRepository = ProductoRepository(
            productoDao = database.productoDao(),
            apiService = RetrofitClient.productosService
        )
        
        ubicacionRepository = UbicacionRepository(
            ubicacionDao = database.ubicacionDao(),
            asignacionDao = database.asignacionUbicacionDao(),
            apiService = RetrofitClient.ubicacionesService
        )
        
        setContent {
            FotomarWMSTheme {
                AppNavigation(
                    productoRepository = productoRepository,
                    ubicacionRepository = ubicacionRepository
                )
            }
        }
    }
}
```

#### 5. Gestión de Token de Autenticación
Después del login exitoso, guardar el token:

```kotlin
// En AuthViewModel o LoginScreen
viewModelScope.launch {
    val response = RetrofitClient.authService.login(
        LoginRequest(email, password)
    )
    
    if (response.isSuccessful && response.body() != null) {
        val loginResponse = response.body()!!
        
        // Guardar token en RetrofitClient
        RetrofitClient.setAuthToken(loginResponse.token)
        
        // También guardar en SharedPreferences para persistencia
        val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("token", loginResponse.token)
            .putString("rol", loginResponse.rol)
            .putInt("userId", loginResponse.id)
            .apply()
        
        // Navegar al dashboard
        onLoginSuccess(loginResponse.rol)
    }
}
```

#### 6. Sincronización Automática
Implementar un WorkManager para sincronizar datos pendientes:

```kotlin
class SyncWorker(
    context: Context,
    params: WorkerParameters,
    private val productoRepository: ProductoRepository,
    private val ubicacionRepository: UbicacionRepository
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            // Sincronizar productos pendientes
            productoRepository.syncPendientes()
            
            // Sincronizar asignaciones pendientes
            ubicacionRepository.syncAsignacionesPendientes()
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

// Programar sincronización periódica
val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
    15, TimeUnit.MINUTES
).setConstraints(
    Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()
).build()

WorkManager.getInstance(context).enqueueUniquePeriodicWork(
    "sync_work",
    ExistingPeriodicWorkPolicy.KEEP,
    syncRequest
)
```

## Flujo de Uso

### Editar Producto con Escáner

1. Usuario navega a DetalleProducto
2. Presiona botón "Editar Producto"
3. Campos se vuelven editables
4. Usuario presiona icono de cámara junto a "Código Individual"
5. Se abre el escáner de códigos de barras
6. Usuario escanea el código
7. El código aparece en el campo de texto
8. Usuario puede editar manualmente si es necesario
9. Repite para LPN si es necesario
10. Presiona "Guardar"
11. Los cambios se guardan localmente primero
12. Se envían al backend
13. Si OK (200), se eliminan de la BD local
14. Si falla, quedan pendientes para sincronización

### Asignar Producto a Ubicaciones

1. Usuario está en DetalleProducto
2. Presiona botón "Asignar" en la sección de ubicaciones
3. Se abre AsignarUbicacionScreen
4. Selecciona piso (A, B o C)
5. Ingresa número (1-60)
6. Ingresa cantidad
7. Ve vista previa del código (ej: A-12)
8. Presiona "Agregar a la Lista"
9. Puede agregar múltiples ubicaciones
10. Presiona "Confirmar Asignaciones"
11. Cada asignación se guarda localmente primero
12. Se envía al backend
13. Si OK (200), se elimina de la BD local
14. Si falla, queda pendiente para sincronización

## Archivos Modificados/Creados

### Modificados
- `app/build.gradle.kts` - Dependencias de Retrofit
- `AppDatabase.kt` - Versión 6, nuevas entidades
- Todos los DAOs - Nombres consistentes

### Creados - Capa de Datos
- `db/entities/ProductoLocal.kt`
- `db/entities/UbicacionLocal.kt`
- `db/entities/AprobacionLocal.kt`
- `db/daos/ProductoDao.kt`
- `db/daos/UbicacionDao.kt`
- `db/daos/AprobacionDao.kt`

### Creados - Capa de Red
- `network/ApiModels.kt`
- `network/RetrofitClient.kt`
- `network/AuthApiService.kt`
- `network/UsuariosApiService.kt`
- `network/ProductosApiService.kt`
- `network/UbicacionesApiService.kt`
- `network/AprobacionesApiService.kt`
- `network/MensajesApiService.kt`
- `network/InventarioApiService.kt`

### Creados - Repositorios
- `repository/ProductoRepository.kt`
- `repository/UbicacionRepository.kt`

### Creados - UI
- `ui/screen/DetalleProductoScreenNew.kt`
- `ui/screen/AsignarUbicacionScreen.kt`
- `ui/screen/componentes/AsignarUbicacionDialog.kt`

## Próximos Pasos Recomendados

1. **Implementar repositorios restantes**:
   - AprobacionRepository
   - MensajeRepository
   - InventarioRepository
   - UsuarioRepository

2. **Actualizar ViewModels existentes** para usar repositorios

3. **Implementar sincronización automática** con WorkManager

4. **Agregar manejo de errores robusto**:
   - Snackbars para errores de red
   - Diálogos de confirmación
   - Indicadores de sincronización pendiente

5. **Implementar caché inteligente**:
   - TTL (Time To Live) para datos cacheados
   - Refresh automático en segundo plano

6. **Agregar tests unitarios**:
   - Tests de repositorios
   - Tests de ViewModels
   - Tests de sincronización

7. **Optimizar rendimiento**:
   - Paginación en listas largas
   - Lazy loading de imágenes
   - Debounce en búsquedas

## Notas Importantes

- **Versión de Base de Datos**: Incrementada a 6, se perderán datos existentes por `fallbackToDestructiveMigration()`
- **URLs Base**: Configuradas para `http://fotomarwms.ddns.net:8081-8087`
- **Formato de Ubicación**: Siempre `{PISO}-{NUMERO}` (ej: A-12, B-05, C-60)
- **Sincronización**: Los datos se guardan localmente primero, luego se sincronizan
- **Token**: Se debe guardar después del login y restaurar al iniciar la app
