# 📱 FotomarWMS - Sistema de Gestión de Bodega

## 🎯 Resumen del Proyecto

**FotomarWMS** es una aplicación móvil nativa para la gestión avanzada de bodegas de productos fotográficos. Desarrollada en **Kotlin** utilizando **Jetpack Compose** para la interfaz de usuario y una arquitectura **MVVM (Model-View-ViewModel)** robusta.

El sistema implementa un enfoque **Offline-First** utilizando **Room Database** para la persistencia local y se sincroniza en tiempo real con una arquitectura de backend basada en **Microservicios**.

### Integrantes
- Dante Rojas
- Martin Villegas

---

## 🏗 Arquitectura y Tecnologías

* **Lenguaje:** Kotlin
* **UI Toolkit:** Jetpack Compose (Material Design 3)
* **Patrón de Diseño:** MVVM + Repository Pattern
* **Persistencia Local:** Room Database (SQLite)
* **Red:** Retrofit 2 + OkHttp (Integración REST)
* **Escaneo:** ML Kit (Barcode Scanning)
* **Inyección de Dependencias:** ViewModelFactory personalizado

---

## 📁 Estructura del Proyecto Actualizada
app/src/main/java/com/pneuma/fotomarwms_grupo5/ 
├── MainActivity.kt # Activity única (Single Activity Architecture) 
├── FotomarWMSApplication.kt # Punto de entrada y gestión de dependencias globales 
├── model/ 
│ ├── Producto.kt, Usuario.kt # Modelos de dominio 
│ ├── Aprobacion.kt, etc. 
│ └── UiState.kt # Estados de UI genéricos 
├── navigation/ 
│ └── AppNavigation.kt # Definición de rutas y grafo de navegación 
├── db/ # Persistencia Local (Room) 
├── AppDatabase.kt # Configuración de BD (Versión 7) 
│ ├── daos/ # ProductoDao, UbicacionDao, AprobacionDao, etc. 
│ └── entities/ # Entidades locales (Tablas SQL) 
├── network/ # Capa de Comunicación (API) 
│ ├── RetrofitClient.kt # Cliente HTTP configurado 
│ └── *ApiService.kt # Interfaces para cada microservicio 
├── repository/ # Repositorios (Single Source of Truth) 
│ ├── ProductoRepository.kt # Lógica de sincronización Local <-> Remoto 
│ └── UbicacionRepository.kt 
├── viewmodels/ # Gestión de Estado (StateFlow) 
│ ├── AuthViewModel.kt 
│ ├── ProductoViewModel.kt 
│ ├── UbicacionViewModel.kt 
│ ├── AprobacionViewModel.kt 
│ ├── RegistroDirectoViewModel.kt 
│ └── UsuarioViewModel.kt 
├── ui/ 
│ ├── screen/ # Pantallas (Composables) 
│ │ ├── LoginScreen.kt 
│ │ ├── Dashboard[Admin/Jefe/Operador]Screen.kt 
│ │ ├── BusquedaScreen.kt 
│ │ ├── DetalleProductoScreen.kt 
│ │ ├── GestionUbicacionesScreen.kt 
│ │ ├── DetalleUbicacionScreen.kt 
│ │ ├── AsignarUbicacionScreen.kt 
│ │ ├── AprobacionesScreen.kt 
│ │ ├── DetalleAprobacionScreen.kt 
│ │ ├── SolicitudMovimientoScreen.kt 
│ │ ├── RegistroDirectoScreen.kt 
│ │ ├── MisSolicitudesScreen.kt 
│ │ ├── GestionUsuariosScreen.kt 
│ │ ├── PerfilScreen.kt 
│ │ └── ConfiguracionScreen.kt 
│ └── componentes/ # UI Reutilizable 
│ ├── BarcodeScanner.kt # Escáner de cámara integrado 
│ ├── AsignarUbicacionDialog.kt 
│ └── ... (Buttons, Cards, Inputs)

---

## 🔌 Microservicios Integrados

La aplicación consume una arquitectura distribuida. Actualmente integra los siguientes servicios activos:

| Servicio | Puerto | Descripción |
|----------|--------|-------------|
| **Auth** | `:8081` | Autenticación y JWT |
| **Usuarios** | `:8082` | Gestión de perfiles y roles |
| **Productos** | `:8083` | Catálogo, stock y códigos |
| **Ubicaciones** | `:8084` | Gestión de pasillos y asignaciones |
| **Aprobaciones** | `:8085` | Flujo de control de movimientos |

---

## 🎨 Pantallas y Funcionalidades (14 Pantallas)

### 🔐 Autenticación y Perfil
1.  **LoginScreen:** Acceso seguro con roles (ADMIN, JEFE, SUPERVISOR, OPERADOR).
2.  **PerfilScreen:** Gestión de datos de usuario y cierre de sesión.
3.  **ConfiguraciónScreen:** Ajustes de la aplicación.

### 🏠 Dashboards (Por Rol)
4.  **DashboardOperadorScreen:** Acceso rápido a búsqueda y solicitudes.
5.  **DashboardJefeScreen:** Alertas de stock, resumen de aprobaciones y accesos directos.
6.  **DashboardAdminScreen:** Métricas de sistema y gestión de usuarios.

### 📦 Gestión de Inventario
7.  **BusquedaScreen:** Escáner de código de barras (Cámara) y búsqueda manual (SKU/Nombre).
8.  **DetalleProductoScreen:**
    * Información completa y stock.
    * **Edición:** Modificación de códigos de barras/LPN.
    * **Ubicaciones:** Visualización y navegación a ubicaciones físicas.
9.  **GestionUbicacionesScreen:** Mapa visual de bodega (Pisos A, B, C) con estados de ocupación.
10. **DetalleUbicacionScreen:** Listado de productos contenidos en una posición específica.
11. **AsignarUbicacionScreen:** Interfaz dedicada para vincular productos a posiciones.

### 📝 Movimientos y Control
12. **SolicitudMovimientoScreen:** (Operadores) Formulario para pedir ingresos, egresos o reubicaciones.
13. **RegistroDirectoScreen:** (Jefes/Supervisores) Ejecución inmediata de movimientos sin aprobación previa.
14. **AprobacionesScreen:** (Jefes/Supervisores) Bandeja de entrada para autorizar o rechazar solicitudes.
    * Incluye **DetalleAprobacionScreen** para revisión exhaustiva.
15. **MisSolicitudesScreen:** (Operadores) Historial y estado de las solicitudes propias.
16. **GestionUsuariosScreen:** (Admin) ABM completo de usuarios del sistema.

---

## 🔑 Roles y Permisos

### ADMIN
* Gestión total de usuarios (Crear, Editar, Desactivar).
* Visualización de métricas globales.
* *Sin acceso a operaciones de bodega.*

### JEFE DE BODEGA
* **Registro Directo:** Movimientos de stock inmediatos.
* **Aprobador:** Autoridad final para solicitudes de operadores.
* Gestión total de ubicaciones y productos.

### SUPERVISOR
* Funciones similares al Jefe.
* Capacidad de aprobar solicitudes y realizar registros directos.

### OPERADOR
* **Solicitante:** Debe pedir autorización para mover stock.
* Consulta de productos y ubicaciones.
* Visualización de estado de sus propias solicitudes.

---

## 💾 Base de Datos Local (Room)

La app utiliza una base de datos local robusta (versión 7) para garantizar el funcionamiento offline y la velocidad de respuesta.

**Entidades Principales:**
* `ProductoLocal`: Caché del catálogo y stock.
* `UbicacionLocal` & `AsignacionUbicacionLocal`: Estado físico de la bodega.
* `SolicitudMovimientoLocal`: Cola de peticiones de movimientos.
* `AprobacionLocal`: Estado de las autorizaciones.
* `UsuarioLocal`: Datos de sesión y usuarios cacheados.

---

## 🚀 Flujo de Trabajo Recomendado

1.  **Ingreso de Mercadería:**
    * *Jefe:* Usa **Registro Directo** -> Ingreso. Asigna ubicación escaneando el producto.
    * *Operador:* Usa **Solicitar Movimiento** -> Ingreso. El Jefe aprueba desde **Aprobaciones**.

2.  **Consulta:**
    * Usar **Búsqueda** para escanear un código de barras.
    * Ver en **Detalle Producto** en qué pasillo/ubicación está.

3.  **Movimiento Interno (Reubicación):**
    * Escanear producto.
    * Solicitar "Reubicación" indicando origen y destino.
    * Al aprobarse, el stock se mueve virtualmente.

## 🚀 Instrucciones de Ejecución
**Requisitos Previos**
Android Studio Ladybug (o superior).
*JDK 11 configurado.
*Dispositivo físico o emulador con Android 13 (API 33) o superior.

**Pasos para Ejecutar**
*Clonar el repositorio que contiene el código fuente de la app móvil y los microservicios.
*Configurar Red: Asegurarse de que el dispositivo tenga acceso a las URLs de los microservicios.
*Compilación: Abrir el proyecto en Android Studio, sincronizar Gradle y ejecutar el módulo :app.
*Sincronización: Al iniciar sesión por primera vez, la app descargará el catálogo inicial para funcionamiento offline.

**📦 Entregables y Binarios**
*APK Firmado: Ubicado en el directorio app/debug/app-debug.apk dentro del archivo comprimido adjunto.
*Llave de Firma (.jks): El archivo keywms se encuentra dentro del paquete comprimido junto al código fuente.

**Código Fuente:**
*App Móvil: Directorio /app.
*Microservicios: Código fuente del backend incluido en las carpetas de servicios correspondientes.

**📈 Evidencia de Trabajo Colaborativo**
El desarrollo se gestionó mediante Git, manteniendo un historial de commits que refleja la autoría de cada integrante:

**Dante Rojas:** Implementación de arquitectura MVVM, , lógica de repositorios y integración de CameraX/ML Kit para escaneo y gestión de navegación. sincronización local-first.

**Martin Villegas:** Diseño de interfaces en Jetpack Compose, integración de Room Database, Implementación de lógica de negocio en endpoints de egreso/reubicación y automatización de flujo de aprobaciones.