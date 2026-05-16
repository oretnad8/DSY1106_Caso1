# Cambios Implementados en FotomarWMS

## Resumen

Se implementaron mejoras en el sistema FotomarWMS para que los movimientos de inventario (ingresos, egresos, reubicaciones) afecten correctamente el stock de productos, con control de permisos por rol y mejor visualización de solicitudes.

---

## Cambios en el Backend

### 1. Servicio de Ubicaciones (ubicacionesservice)

#### Nuevos DTOs creados:
- `EgresoProductoRequest.java` - Request para registrar egresos
- `EgresoProductoResponse.java` - Response de egresos
- `ReubicarProductoRequest.java` - Request para reubicaciones
- `ReubicarProductoResponse.java` - Response de reubicaciones

#### Modificaciones en `UbicacionService.java`:
- **Método `asignarProducto()`**: Ahora incrementa el stock del producto al asignar (INGRESO)
- **Nuevo método `egresoProducto()`**: Reduce stock del producto y cantidad en ubicación
- **Nuevo método `reubicarProducto()`**: Mueve producto de ubicación origen a destino

#### Modificaciones en `UbicacionController.java`:
- **Nuevo endpoint POST `/api/ubicaciones/egreso`**: Registra egresos (solo JEFE y SUPERVISOR)
- **Nuevo endpoint POST `/api/ubicaciones/reubicar`**: Registra reubicaciones (solo JEFE y SUPERVISOR)

### 2. Servicio de Aprobaciones (aprobacionesservice)

#### Nuevos archivos creados:
- `UbicacionesClient.java` - Cliente HTTP para comunicación con ubicaciones-service
- `RestTemplateConfig.java` - Configuración de RestTemplate
- `UbicacionMapping.java` - Entidad para mapeo de IDs a códigos de ubicación
- `UbicacionMappingRepository.java` - Repositorio para mapeo de ubicaciones

#### Modificaciones en `AprobacionService.java`:
- **Método `aprobarSolicitud()`**: Ahora ejecuta automáticamente el movimiento al aprobar
- **Nuevo método `ejecutarMovimiento()`**: Ejecuta el movimiento según tipo (INGRESO/EGRESO/REUBICACION)
- **Nuevo método `obtenerCodigoUbicacion()`**: Convierte ID de ubicación a código

#### Flujo de aprobación actualizado:
1. Operador crea solicitud → Estado PENDIENTE
2. Jefe/Supervisor aprueba → Estado APROBADO + **Ejecuta movimiento automáticamente**
3. El movimiento actualiza:
   - Stock del producto
   - Cantidades en ubicaciones
   - Elimina asignaciones con cantidad 0

---

## Cambios en el Frontend (Android)

### 1. Modelos y API

#### Modificaciones en `ApiModels.kt`:
- **Nuevos DTOs**: `EgresoUbicacionRequest`, `ReubicarUbicacionRequest`
- **Actualización de `AprobacionResponse`**: Ahora incluye `solicitante`, `aprobador`, `fechaAprobacion`
- **Nuevos DTOs**: `SolicitanteDTO`, `AprobadorDTO`

#### Modificaciones en `UbicacionesApiService.kt`:
- **Nuevo endpoint**: `egresoProducto()`
- **Nuevo endpoint**: `reubicarProducto()`

### 2. ViewModels

#### Modificaciones en `RegistroDirectoViewModel.kt`:
- **Método `registrarEgreso()`**: Ahora llama al endpoint real de egreso
- **Método `registrarReubicacion()`**: Ahora llama al endpoint real de reubicación

#### Modificaciones en `AprobacionViewModel.kt`:
- **Método `toDomainModel()`**: Ahora mapea correctamente los datos del solicitante y aprobador

### 3. UI

#### Modificaciones en `DetalleAprobacionScreen.kt`:
- **Control de permisos**: Los botones de aprobar/rechazar solo se muestran a JEFE y SUPERVISOR
- **Nuevo parámetro**: `authViewModel` para verificar el rol del usuario actual
- **Validación**: `canApprove = currentUser?.rol == Rol.JEFE || currentUser?.rol == Rol.SUPERVISOR`

#### Modificaciones en `MainActivity.kt`:
- Actualizada la llamada a `DetalleAprobacionScreen` para incluir `authViewModel`

#### `AprobacionCard.kt`:
- Ya mostraba el nombre del solicitante correctamente

---

## Funcionalidades Implementadas

### ✅ Actualización de Stock
- **INGRESO**: Incrementa stock del producto al asignar a ubicación
- **EGRESO**: Reduce stock del producto al retirar de ubicación
- **REUBICACIÓN**: Mueve producto sin afectar stock total (solo cambia ubicación)

### ✅ Gestión de Ubicaciones
- **EGRESO**: Reduce cantidad en ubicación origen, elimina asignación si llega a 0
- **REUBICACIÓN**: Reduce cantidad en origen, incrementa en destino, elimina asignación origen si llega a 0

### ✅ Control de Permisos
- **Registro Directo**: Solo JEFE y SUPERVISOR (endpoints con `@PreAuthorize`)
- **Solicitud de Movimiento**: OPERADOR, SUPERVISOR, JEFE (requiere aprobación)
- **Aprobar/Rechazar**: Solo JEFE y SUPERVISOR (validado en backend y frontend)

### ✅ Visualización
- **Nombre del solicitante**: Se muestra en cards y detalle de aprobación
- **Botones de acción**: Solo visibles para JEFE y SUPERVISOR
- **Información del aprobador**: Se muestra cuando la solicitud está aprobada/rechazada

---

## Configuración Requerida

### Backend

1. **Sincronización de Ubicaciones**:
   - Ejecutar el script `sync_ubicaciones.sql` en la base de datos de aprobaciones-service
   - O implementar un endpoint en ubicaciones-service para sincronización automática

2. **Configuración de URL**:
   - En `application.yml` de aprobaciones-service, verificar:
   ```yaml
   ubicaciones:
     service:
       url: http://localhost:8084
   ```

### Frontend

No requiere configuración adicional. Los endpoints ya están configurados en `RetrofitClient`.

---

## Endpoints Nuevos

### Ubicaciones Service (Puerto 8084)

```
POST /api/ubicaciones/egreso
Body: {
  "sku": "AP30001",
  "codigoUbicacion": "P1-A-01",
  "cantidad": 10,
  "motivo": "Venta"
}
Roles: JEFE, SUPERVISOR
```

```
POST /api/ubicaciones/reubicar
Body: {
  "sku": "AP30001",
  "codigoUbicacionOrigen": "P1-A-01",
  "codigoUbicacionDestino": "P2-B-15",
  "cantidad": 5,
  "motivo": "Reorganización"
}
Roles: JEFE, SUPERVISOR
```

---

## Flujo de Trabajo

### Registro Directo (JEFE/SUPERVISOR)
1. Acceder a "Registro Directo"
2. Seleccionar tipo: Ingreso, Egreso o Reubicación
3. Completar formulario
4. Confirmar → **Ejecuta inmediatamente**
5. Stock y ubicaciones se actualizan en tiempo real

### Solicitud de Movimiento (OPERADOR)
1. Acceder a "Solicitar Movimiento"
2. Seleccionar tipo: Ingreso, Egreso o Reubicación
3. Completar formulario
4. Enviar solicitud → Estado PENDIENTE
5. Esperar aprobación de JEFE/SUPERVISOR
6. Al aprobar → **Ejecuta automáticamente**
7. Stock y ubicaciones se actualizan

### Aprobación (JEFE/SUPERVISOR)
1. Ver solicitudes pendientes en "Aprobaciones"
2. Ver detalle de solicitud (muestra nombre del solicitante)
3. Aprobar o Rechazar con observaciones
4. Al aprobar → **Sistema ejecuta el movimiento automáticamente**

---

## Notas Importantes

1. **Tabla de Mapeo**: La tabla `ubicacion_mapping` en aprobaciones-service debe estar sincronizada con las ubicaciones reales. Se recomienda implementar un proceso de sincronización automática.

2. **Manejo de Errores**: Si falla la ejecución del movimiento después de aprobar, la aprobación no se revierte. En producción, implementar un sistema de reintentos o compensación.

3. **Validaciones**: El backend valida:
   - Stock suficiente antes de egresos
   - Cantidad suficiente en ubicación origen antes de reubicaciones
   - Permisos de usuario según rol
   - Formato de códigos de ubicación

4. **Operadores**: No ven botones de aprobar/rechazar, solo pueden ver el estado de sus solicitudes.

---

## Testing

### Casos de Prueba Recomendados

1. **Ingreso**:
   - Verificar que el stock del producto aumenta
   - Verificar que la cantidad en ubicación aumenta

2. **Egreso**:
   - Verificar que el stock del producto disminuye
   - Verificar que la cantidad en ubicación disminuye
   - Verificar que la asignación se elimina si cantidad llega a 0

3. **Reubicación**:
   - Verificar que el stock total del producto no cambia
   - Verificar que la cantidad disminuye en origen
   - Verificar que la cantidad aumenta en destino
   - Verificar que la asignación origen se elimina si cantidad llega a 0

4. **Permisos**:
   - OPERADOR no puede acceder a Registro Directo
   - OPERADOR no ve botones de aprobar/rechazar
   - JEFE y SUPERVISOR pueden hacer todo

5. **Visualización**:
   - Verificar que se muestra el nombre del solicitante
   - Verificar que se muestra el nombre del aprobador después de aprobar

---

## Autor

Cambios implementados por Manus AI
Fecha: 25 de noviembre de 2025
