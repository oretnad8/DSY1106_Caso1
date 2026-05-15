package com.smartlogix.aprobacionesservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tabla para mapear IDs de ubicación a códigos
 * Sincronizada desde ubicaciones-service
 */
@Entity
@Table(name = "ubicacion_mapping")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UbicacionMapping {
    
    @Id
    private Integer idUbicacion;
    
    @Column(name = "codigo_ubicacion", length = 10, nullable = false, unique = true)
    private String codigoUbicacion;
}

