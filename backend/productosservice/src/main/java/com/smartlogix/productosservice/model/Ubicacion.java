package com.smartlogix.productosservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ubicaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ubicacion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ubicacion")
    private Integer idUbicacion;
    
    @Column(name = "codigo_ubicacion", length = 15, unique = true, nullable = false)
    private String codigoUbicacion; // P1-A-01
    
    @Column(nullable = false)
    private Integer pasillo; // 1 a 5
    
    @Column(length = 1, nullable = false)
    private Character piso; // A, B, C
    
    @Column(nullable = false)
    private Integer numero; // 1 a 60
}
