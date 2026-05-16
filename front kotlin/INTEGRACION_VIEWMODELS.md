# Guía de Integración de ViewModels con Repositorios

## Cambios Realizados

### 1. ViewModels Actualizados

#### ProductoViewModel
- ✅ Eliminados todos los mocks
- ✅ Usa `ProductoRepository` con microservicios reales
- ✅ Implementa patrón local-first
- ✅ Requiere inyección de `ProductoRepository` en constructor

#### UbicacionViewModel
- ✅ Eliminados todos los mocks
- ✅ Usa `UbicacionRepository` con microservicios reales
- ✅ Implementa patrón local-first
- ✅ Requiere inyección de `UbicacionRepository` en constructor

### 2. Infraestructura Creada

#### FotomarWMSApplication
- Application class para inicializar repositorios
- Gestión de token de autenticación
- Acceso global a repositorios

#### ViewModelFactory
- Factory para crear ViewModels con dependencias
- Soporta ProductoViewModel y UbicacionViewModel

---

## Cómo Integrar

### Paso 1: Actualizar AndroidManifest.xml

Agregar la Application class en `AndroidManifest.xml`:

```xml
<application
    android:name=".FotomarWMSApplication"
    android:allowBackup="true"
    android:icon="@mipmap/ic_launcher"
    android:label="@string/app_name"
    android:theme="@style/Theme.FotomarWMS">
    
    <activity
        android:name=".MainActivity"
        android:exported="true">
        <intent-filter>
            <action android:name="android.intent.action.MAIN" />
            <category android:name="android.intent.category.LAUNCHER" />
        </intent-filter>
    </activity>
</application>
```

### Paso 2: Actualizar MainActivity

Obtener la Application y crear ViewModels con factory:

```kotlin
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Obtener Application
        val app = application as FotomarWMSApplication
        
        setContent {
            FotomarWMSTheme {
                // Crear ViewModels con factory
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
                
                AppNavigation(
                    productoViewModel = productoViewModel,
                    ubicacionViewModel = ubicacionViewModel
                )
            }
        }
    }
}
```

### Paso 3: Actualizar Pantallas que Usan ViewModels

#### Ejemplo: BuscarProductoScreen

**Antes:**
```kotlin
@Composable
fun BuscarProductoScreen() {
    val productoViewModel: ProductoViewModel = viewModel()
    // ...
}
```

**Después:**
```kotlin
@Composable
fun BuscarProductoScreen(
    productoViewModel: ProductoViewModel  // Pasar como parámetro
) {
    // ...
}
```

#### Ejemplo: DetalleProductoScreen

**Antes:**
```kotlin
@Composable
fun DetalleProductoScreen(sku: String) {
    val productoViewModel: ProductoViewModel = viewModel()
    // ...
}
```

**Después:**
```kotlin
@Composable
fun DetalleProductoScreen(
    sku: String,
    productoViewModel: ProductoViewModel  // Pasar como parámetro
) {
    // ...
}
```

### Paso 4: Actualizar Navegación

Pasar los ViewModels a través de la navegación:

```kotlin
@Composable
fun AppNavigation(
    productoViewModel: ProductoViewModel,
    ubicacionViewModel: UbicacionViewModel
) {
    val navController = rememberNavController()
    
    NavHost(navController = navController, startDestination = "home") {
        
        composable("buscar_producto") {
            BuscarProductoScreen(
                productoViewModel = productoViewModel,
                onNavigateToDetail = { sku ->
                    navController.navigate("detalle_producto/$sku")
                }
            )
        }
        
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
        
        composable("ubicaciones") {
            UbicacionesScreen(
                ubicacionViewModel = ubicacionViewModel,
                onNavigateToDetail = { codigo ->
                    navController.navigate("detalle_ubicacion/$codigo")
                }
            )
        }
    }
}
```

### Paso 5: Gestión de Autenticación

Después del login exitoso, guardar el token:

```kotlin
@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as FotomarWMSApplication
    
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    fun login() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.authService.login(
                    LoginRequest(email, password)
                )
                
                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!
                    
                    // Guardar token en Application
                    app.saveAuthToken(
                        token = loginResponse.token,
                        rol = loginResponse.rol,
                        userId = loginResponse.id
                    )
                    
                    // Navegar al dashboard
                    onLoginSuccess(loginResponse.rol)
                }
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }
    
    // UI del login...
}
```

### Paso 6: Verificar Autenticación al Iniciar

En MainActivity, verificar si el usuario ya está autenticado:

```kotlin
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val app = application as FotomarWMSApplication
        
        setContent {
            FotomarWMSTheme {
                val startDestination = if (app.isAuthenticated()) {
                    "home"
                } else {
                    "login"
                }
                
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
                
                AppNavigation(
                    startDestination = startDestination,
                    productoViewModel = productoViewModel,
                    ubicacionViewModel = ubicacionViewModel
                )
            }
        }
    }
}
```

---

## Funcionalidades Nuevas

### Sincronización de Datos Pendientes

Los ViewModels ahora tienen métodos para sincronizar datos pendientes:

```kotlin
// En ProductoViewModel
productoViewModel.syncPendientes()

// En UbicacionViewModel
ubicacionViewModel.syncAsignacionesPendientes()
```

Puedes llamar estos métodos:
- Al recuperar conexión a internet
- Al abrir la app
- Periódicamente con WorkManager

### Estados de Operaciones

Los ViewModels ahora exponen estados para todas las operaciones:

```kotlin
// ProductoViewModel
val searchState: StateFlow<UiState<List<Producto>>>
val productoDetailState: StateFlow<UiState<Producto>>
val createState: StateFlow<UiState<Producto>>
val updateState: StateFlow<UiState<Producto>>
val deleteState: StateFlow<UiState<Unit>>

// UbicacionViewModel
val ubicacionesState: StateFlow<UiState<List<Ubicacion>>>
val ubicacionDetailState: StateFlow<UiState<Ubicacion>>
val asignacionState: StateFlow<UiState<Boolean>>
```

### Métodos de Limpieza

Limpiar estados cuando sea necesario:

```kotlin
productoViewModel.clearSearch()
productoViewModel.clearSelectedProducto()
productoViewModel.clearCreateState()
productoViewModel.clearUpdateState()
productoViewModel.clearDeleteState()

ubicacionViewModel.clearUbicaciones()
ubicacionViewModel.clearSelectedUbicacion()
ubicacionViewModel.clearAsignacionState()
```

---

## Ejemplo Completo de Uso

### Buscar y Mostrar Productos

```kotlin
@Composable
fun BuscarProductoScreen(
    productoViewModel: ProductoViewModel,
    onNavigateToDetail: (String) -> Unit
) {
    val searchState by productoViewModel.searchState.collectAsStateWithLifecycle()
    val searchQuery by productoViewModel.searchQuery.collectAsStateWithLifecycle()
    
    var query by remember { mutableStateOf("") }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Barra de búsqueda
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Buscar por SKU, descripción...") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Button(
            onClick = { productoViewModel.searchProductos(query) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Buscar")
        }
        
        // Resultados
        when (val state = searchState) {
            is UiState.Loading -> {
                CircularProgressIndicator()
            }
            is UiState.Success -> {
                LazyColumn {
                    items(state.data) { producto ->
                        ProductoCard(
                            producto = producto,
                            onClick = { onNavigateToDetail(producto.sku) }
                        )
                    }
                }
            }
            is UiState.Error -> {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error
                )
            }
            is UiState.Idle -> {
                Text("Ingresa un término de búsqueda")
            }
        }
    }
}
```

### Asignar Producto a Ubicación

```kotlin
@Composable
fun AsignarUbicacionDialog(
    sku: String,
    ubicacionViewModel: UbicacionViewModel,
    onDismiss: () -> Unit
) {
    val asignacionState by ubicacionViewModel.asignacionState.collectAsStateWithLifecycle()
    
    var piso by remember { mutableStateOf("A") }
    var numero by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    
    // Observar estado de asignación
    LaunchedEffect(asignacionState) {
        if (asignacionState is UiState.Success) {
            // Éxito - cerrar diálogo
            onDismiss()
            ubicacionViewModel.clearAsignacionState()
        }
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Asignar a Ubicación", style = MaterialTheme.typography.titleLarge)
                
                // Inputs...
                
                Button(
                    onClick = {
                        val codigo = "$piso-${numero.padStart(2, '0')}"
                        val cant = cantidad.toIntOrNull() ?: 0
                        ubicacionViewModel.asignarProducto(sku, codigo, cant)
                    },
                    enabled = asignacionState !is UiState.Loading
                ) {
                    if (asignacionState is UiState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        Text("Asignar")
                    }
                }
                
                if (asignacionState is UiState.Error) {
                    Text(
                        text = (asignacionState as UiState.Error).message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
```

---

## Resumen de Archivos

### Creados
- `FotomarWMSApplication.kt` - Application class
- `ViewModelFactory.kt` - Factory para ViewModels
- `ProductoViewModel.kt` - Actualizado sin mocks
- `UbicacionViewModel.kt` - Actualizado sin mocks

### Modificar
- `AndroidManifest.xml` - Agregar `android:name=".FotomarWMSApplication"`
- `MainActivity.kt` - Inicializar ViewModels con factory
- Todas las pantallas que usan ViewModels - Pasar como parámetros

---

## Checklist de Integración

- [ ] Actualizar AndroidManifest.xml con FotomarWMSApplication
- [ ] Actualizar MainActivity para crear ViewModels con factory
- [ ] Actualizar AppNavigation para pasar ViewModels
- [ ] Actualizar todas las pantallas para recibir ViewModels como parámetros
- [ ] Implementar gestión de login con saveAuthToken()
- [ ] Verificar autenticación al iniciar con isAuthenticated()
- [ ] Probar búsqueda de productos
- [ ] Probar detalle de producto
- [ ] Probar asignación de ubicaciones
- [ ] Implementar sincronización periódica (opcional)

---

## Notas Importantes

1. **Los mocks han sido completamente eliminados** - La app ahora usa los microservicios reales
2. **Patrón local-first activo** - Los datos se guardan localmente primero
3. **Requiere conexión a internet** - Para sincronizar con el backend
4. **Token persistente** - Se guarda en SharedPreferences
5. **ViewModels requieren inyección** - No se pueden crear con `viewModel()` sin factory

---

## Troubleshooting

### Error: "ProductoRepository requerido para ProductoViewModel"
**Solución:** Usar ViewModelFactory con el repositorio correcto

### Error: "No se pueden obtener productos"
**Solución:** Verificar que el token esté configurado y que haya conexión a internet

### Los datos no se sincronizan
**Solución:** Llamar manualmente a `syncPendientes()` o implementar WorkManager

### La app crashea al crear ViewModel
**Solución:** Asegurarse de que FotomarWMSApplication esté configurada en AndroidManifest.xml
