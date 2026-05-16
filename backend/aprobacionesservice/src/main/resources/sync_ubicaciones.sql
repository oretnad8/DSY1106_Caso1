-- Script para sincronizar ubicaciones desde ubicaciones-service a aprobaciones-service
-- Este script debe ejecutarse después de inicializar ambas bases de datos

-- Crear tabla de mapeo si no existe
CREATE TABLE IF NOT EXISTS ubicacion_mapping (
    id_ubicacion INT PRIMARY KEY,
    codigo_ubicacion VARCHAR(10) NOT NULL UNIQUE
);

-- Insertar ubicaciones (900 ubicaciones: 5 pasillos × 60 posiciones × 3 pisos)
-- Formato: P{pasillo}-{piso}-{numero}

-- Este script asume que las ubicaciones ya existen en la base de datos de ubicaciones-service
-- y que los IDs son secuenciales desde 1 hasta 900

-- Generar ubicaciones para los 5 pasillos (P1 a P5)
-- Cada pasillo tiene 60 posiciones (01 a 60)
-- Cada posición tiene 3 pisos (A, B, C)

-- Nota: Este script debe ser ejecutado manualmente o mediante un proceso de sincronización
-- En producción, se recomienda implementar un endpoint en ubicaciones-service que permita
-- consultar todas las ubicaciones y sincronizarlas automáticamente

-- Ejemplo de inserción manual (las primeras ubicaciones):
-- INSERT INTO ubicacion_mapping (id_ubicacion, codigo_ubicacion) VALUES (1, 'P1-A-01');
-- INSERT INTO ubicacion_mapping (id_ubicacion, codigo_ubicacion) VALUES (2, 'P1-B-01');
-- INSERT INTO ubicacion_mapping (id_ubicacion, codigo_ubicacion) VALUES (3, 'P1-C-01');
-- ... (continuar para todas las 900 ubicaciones)

-- Alternativa: Ejecutar este query en la base de datos de ubicaciones-service
-- y copiar los resultados a aprobaciones-service:
-- SELECT id_ubicacion, codigo_ubicacion FROM ubicaciones ORDER BY id_ubicacion;
