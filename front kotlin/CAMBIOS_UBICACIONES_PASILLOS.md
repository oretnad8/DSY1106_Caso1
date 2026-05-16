# Actualización del Sistema de Ubicaciones - Soporte para 5 Pasillos

## Resumen de Cambios

Se ha actualizado el sistema de ubicaciones para soportar **5 pasillos** con hasta **60 posiciones** cada uno y **3 pisos** (A, B, C) por ubicación, resultando en un total de **900 ubicaciones** (5 × 60 × 3).

### Formato de Código de Ubicación

**Formato anterior:** `A-12`, `B-45`, `C-60` (solo piso y número)

**Formato nuevo:** `P1-A-12`, `P3-B-45`, `P5-C-60` (pasillo, piso y número)

**Formato de código escaneado:** `P1/A1` → se convierte automáticamente a `P1-A-01`

---

## Archivos Modificados

### 1. **Modelos y Entidades**

#### `app/src/main/java/com/pneuma/fotomarwms_grupo5/model/Ubicacion.kt`
- ✅ Agregado campo `pasillo: Int` al modelo `Ubicacion`
- ✅ Actualizado formato de `codigoUbicacion` a `P{pasillo}-{piso}-{numero}`
- ✅ Creado enum `Pasillo` con valores P1-P5
- ✅ Actualizado enum `Piso` con labels
- ✅ Creado objeto `UbicacionFormatter` con utilidades:
  - `formatCodigo(pasillo, piso, numero)`: Formatea código estándar
  - `parseScannedCode(scannedCode)`: Convierte `P1/A1` → `P1-A-01`
  - `parseCodigo(codigo)`: Extrae componentes del código

#### `app/src/main/java/com/pneuma/fotomarwms_grupo5/db/entities/UbicacionLocal.kt`
- ✅ Agregado campo `pasillo: Int` para cache local
- ✅ Actualizado formato de código en comentarios

#### `app/src/main/java/com/pneuma/fotomarwms_grupo5/network/ApiModels.kt`
- ✅ Agregado campo `pasillo: Int` a `UbicacionResponse`
- ✅ Actualizado comentarios con nuevo formato

---

### 2. **Servicios y Repositorio**

#### `app/src/main/java/com/pneuma/fotomarwms_grupo5/network/UbicacionesApiService.kt`
- ✅ Actualizado endpoint `getUbicaciones()` con parámetro `pasillo`
- ✅ Agregado nuevo endpoint `getUbicacionesByPasilloYPosicion(pasillo, posicion)`
- ✅ Documentación actualizada con ejemplos de nuevos endpoints

#### `app/src/main/java/com/pneuma/fotomarwms_grupo5/repository/UbicacionRepository.kt`
- ✅ Actualizado método `getUbicaciones()` con soporte para filtro por pasillo
- ✅ Agregado método `getUbicacionesByPasilloYPosicion()`
- ✅ Actualizado mapeo de respuestas API para incluir campo `pasillo`

#### `app/src/main/java/com/pneuma/fotomarwms_grupo5/db/daos/UbicacionDao.kt`
- ✅ Agregado query `getByPasillo(pasillo)`
- ✅ Agregado query `getByPasilloYPiso(pasillo, piso)`
- ✅ Actualizado ordenamiento en queries existentes

---

### 3. **ViewModels**

#### `app/src/main/java/com/pneuma/fotomarwms_grupo5/viewmodels/UbicacionViewModel.kt`
- ✅ Agregado estado `pasilloSeleccionado: StateFlow<Pasillo?>`
- ✅ Agregado método `getUbicacionesByPasillo(pasillo)`
- ✅ Agregado método `getUbicacionesByPasilloYPiso(pasillo, piso)`
- ✅ Agregado método `clearPasilloFilter()`
- ✅ Agregado método `clearAllFilters()`
- ✅ Actualizado método `generarCodigo()` para incluir pasillo
- ✅ Actualizado método `isCodigoValido()` con nuevo formato regex
- ✅ Actualizado método `syncAsignacionesPendientes()` con lógica de filtros combinados

---

### 4. **UI - Screens y Componentes**

#### `app/src/main/java/com/pneuma/fotomarwms_grupo5/ui/screen/GestionUbicacionesScreen.kt`
- ✅ Agregado filtro de pasillo arriba del filtro de piso
- ✅ Agregado estado `pasilloSeleccionado` del ViewModel
- ✅ Implementado Row con FilterChips para pasillos 1-5
- ✅ Actualizado header de ubicaciones para mostrar filtros activos
- ✅ Actualizado lógica de retry en ErrorState con filtros combinados

#### `app/src/main/java/com/pneuma/fotomarwms_grupo5/ui/screen/componentes/AsignarUbicacionDialog.kt`
- ✅ Agregado selector de pasillo (P1-P5) arriba del selector de piso
- ✅ Agregado botón "Escanear Código de Ubicación" con icono de cámara
- ✅ Agregado estado `showBarcodeScanner` y diálogo `BarcodeScannerDialog`
- ✅ Implementado parseo automático de código escaneado (`P1/A1` → `P1-A-01`)
- ✅ Actualizado vista previa de código con formato `UbicacionFormatter.formatCodigo()`
- ✅ Actualizado validación y generación de código en botón "Asignar"
- ✅ Agregado manejo de errores para códigos escaneados inválidos

---

## Nuevos Endpoints Soportados

Según el documento `endpointsubinews.docx`:

| Método | Endpoint | Descripción | Ejemplo |
|--------|----------|-------------|---------|
| GET | `/api/ubicaciones` | Todas las ubicaciones (900) | - |
| GET | `/api/ubicaciones?piso=A` | Todos los pasillos, piso A (300) | - |
| GET | `/api/ubicaciones?pasillo=1` | Todas las posiciones y pisos del pasillo 1 (180) | - |
| GET | `/api/ubicaciones?pasillo=3&piso=B` | Pasillo 3, piso B (60) | P3-B-01 a P3-B-60 |
| GET | `/api/ubicaciones/{codigo}` | Ubicación específica | P2-A-15 |
| GET | `/api/ubicaciones/pasillo/{pasillo}/posicion/{posicion}` | Posición específica (3 pisos) | P4-A-25, P4-B-25, P4-C-25 |
| POST | `/api/ubicaciones/asignar` | Asignar producto | Body con código P1-A-01 |

---

## Funcionalidades Implementadas

### 1. **Selector de Pasillo en Diálogo de Asignación**
- Chips de selección única para pasillos P1-P5
- Ubicado arriba del selector de piso
- Integrado con la vista previa de código

### 2. **Escáner de Código de Barras para Ubicaciones**
- Botón dedicado "Escanear Código de Ubicación"
- Parseo automático del formato `P1/A1` → `P1-A-01`
- Auto-selección de pasillo, piso y número tras escaneo exitoso
- Validación de formato y rangos (pasillo 1-5, piso A-C, número 1-60)
- Mensaje de error si el código escaneado es inválido

### 3. **Filtro de Pasillo en Gestión de Ubicaciones**
- Nuevo filtro de pasillo arriba del filtro de piso
- Chips de selección: "Todos", "P1", "P2", "P3", "P4", "P5"
- Filtros combinables (pasillo + piso)
- Indicador de filtros activos en el header

### 4. **Utilidades de Formateo**
- `UbicacionFormatter.formatCodigo()`: Genera códigos estándar
- `UbicacionFormatter.parseScannedCode()`: Convierte códigos escaneados
- `UbicacionFormatter.parseCodigo()`: Extrae componentes

---

## Validaciones

### Formato de Código
- **Regex:** `^P[1-5]-[ABC]-([0-5]?[0-9]|60)$`
- **Ejemplos válidos:** `P1-A-01`, `P3-B-25`, `P5-C-60`
- **Ejemplos inválidos:** `P6-A-01`, `P1-D-01`, `P1-A-61`

### Formato de Código Escaneado
- **Regex:** `P(\d)/([ABC])(\d+)`
- **Ejemplos válidos:** `P1/A1`, `P3/B25`, `P5/C60`
- **Conversión:** `P1/A1` → `P1-A-01`

---

## Migración de Datos

⚠️ **Importante:** Los códigos de ubicación existentes en la base de datos deben migrarse del formato antiguo al nuevo:

- `A-01` → `P1-A-01` (asignar pasillo por defecto)
- `B-25` → `P1-B-25`
- `C-60` → `P1-C-60`

Se recomienda ejecutar un script de migración en el backend para actualizar todos los registros existentes.

---

## Testing Recomendado

1. **Pruebas de API:**
   - Verificar que todos los endpoints retornen el campo `pasillo`
   - Probar filtros combinados (pasillo + piso)
   - Validar endpoint de posición específica

2. **Pruebas de UI:**
   - Escanear códigos de barras con formato `P1/A1`
   - Verificar auto-selección de campos tras escaneo
   - Probar filtros combinados en GestionUbicaciones
   - Validar vista previa de código en diálogo

3. **Pruebas de Validación:**
   - Intentar escanear códigos inválidos
   - Verificar mensajes de error apropiados
   - Probar rangos límite (P5, C, 60)

---

## Próximos Pasos

1. ✅ Actualizar backend para soportar nuevos endpoints
2. ✅ Migrar datos existentes al nuevo formato
3. ⏳ Probar integración completa con backend
4. ⏳ Generar códigos de barras físicos con formato `P1/A1`
5. ⏳ Capacitar usuarios en nuevo sistema de ubicaciones

---

## Notas Adicionales

- El sistema mantiene compatibilidad con el patrón **local-first** para asignaciones
- Los filtros son **combinables** (pasillo + piso)
- El escáner de código de barras utiliza el componente `BarcodeScannerDialog` existente
- La validación de códigos es **estricta** para evitar errores de asignación

---

**Fecha de actualización:** 12 de noviembre de 2025  
**Versión:** 2.0 - Soporte para 5 pasillos
