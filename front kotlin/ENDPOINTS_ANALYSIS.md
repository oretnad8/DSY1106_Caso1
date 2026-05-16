# Análisis de Endpoints - FotomarWMS

## Microservicios y Puertos

1. **AUTH-SERVICE (Puerto 8081)**
   - POST /api/auth/login - Iniciar sesión
   - POST /api/auth/logout - Cerrar sesión
   - GET /api/auth/validate - Validar token JWT

2. **USUARIOS-SERVICE (Puerto 8082)**
   - GET /api/usuarios - Listar todos los usuarios
   - GET /api/usuarios/{id} - Obtener usuario por ID
   - POST /api/usuarios - Crear nuevo usuario
   - PUT /api/usuarios/{id} - Actualizar usuario
   - DELETE /api/usuarios/{id} - Eliminar usuario
   - PUT /api/usuarios/{id}/toggle-activo - Activar/Desactivar usuario

3. **PRODUCTOS-SERVICE (Puerto 8083)**
   - GET /api/productos/search - Buscar productos (query opcional)
   - GET /api/productos/{sku} - Obtener producto por SKU
   - POST /api/productos - Crear producto
   - PUT /api/productos/{sku} - Actualizar producto
   - DELETE /api/productos/{sku} - Eliminar producto

4. **UBICACIONES-SERVICE (Puerto 8084)**
   - GET /api/ubicaciones - Listar todas las ubicaciones (query: piso=A|B|C)
   - GET /api/ubicaciones/{codigo} - Obtener ubicación por código
   - POST /api/ubicaciones/asignar - Asignar producto a ubicación

5. **APROBACIONES-SERVICE (Puerto 8085)**
   - GET /api/aprobaciones - Listar aprobaciones (query: estado=PENDIENTE|APROBADO|RECHAZADO)
   - GET /api/aprobaciones/{id} - Obtener aprobación por ID
   - POST /api/aprobaciones - Crear solicitud de aprobación
   - PUT /api/aprobaciones/{id}/aprobar - Aprobar solicitud
   - PUT /api/aprobaciones/{id}/rechazar - Rechazar solicitud
   - GET /api/aprobaciones/mis-solicitudes - Ver mis solicitudes

6. **MENSAJES-SERVICE (Puerto 8086)**
   - GET /api/mensajes - Listar mensajes del usuario
   - GET /api/mensajes/resumen - Resumen de mensajes
   - GET /api/mensajes/enviados - Mensajes enviados por mí
   - GET /api/mensajes/{id} - Obtener mensaje por ID
   - POST /api/mensajes - Enviar mensaje
   - PUT /api/mensajes/{id}/marcar-leido - Marcar mensaje como leído
   - PUT /api/mensajes/{id}/toggle-importante - Cambiar importancia

7. **INVENTARIO-SERVICE (Puerto 8087)**
   - GET /api/inventario/progreso - Obtener progreso del inventario
   - POST /api/inventario/conteo - Registrar conteo físico
   - GET /api/inventario/diferencias - Listar diferencias
   - POST /api/inventario/finalizar - Finalizar inventario y ajustar

## Tipos de Movimiento (Aprobaciones)
- INGRESO - Nueva entrada de productos
- EGRESO - Salida de productos
- REUBICACION - Cambio de ubicación (requiere idUbicacionOrigen e idUbicacionDestino)

## Estructura de Datos Principales

### Producto
```json
{
  "sku": "FL30001",
  "descripcion": "Flash Canon Speedlite",
  "stock": 8,
  "codigoBarraIndividual": "9876543210123",
  "lpn": "LPN-004",
  "lpnDesc": "Caja Flash",
  "fechaVencimiento": null
}
```

### Ubicación
```json
{
  "codigo": "A-12",
  "piso": "A",
  "numero": 12
}
```

### Asignación de Producto a Ubicación
```json
{
  "sku": "CA30001",
  "codigoUbicacion": "A-12",
  "cantidad": 10
}
```

### Solicitud de Movimiento
```json
{
  "tipoMovimiento": "INGRESO",
  "sku": "CA30001",
  "cantidad": 5,
  "motivo": "Nueva compra de cámaras Canon"
}
```

### Usuario
```json
{
  "nombre": "Carlos Rodriguez",
  "email": "carlos@fotomar.cl",
  "password": "123456",
  "rol": "OPERADOR"
}
```

### Mensaje
```json
{
  "idDestinatario": 4,
  "titulo": "Reunión de inventario",
  "contenido": "Se convoca a reunión para revisar el inventario del mes.",
  "importante": true
}
```

### Conteo de Inventario
```json
{
  "sku": "CA30001",
  "idUbicacion": 12,
  "cantidadFisica": 8
}
```
