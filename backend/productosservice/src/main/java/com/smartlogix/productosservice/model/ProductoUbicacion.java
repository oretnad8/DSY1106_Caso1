package com.smartlogix.productosservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "producto_ubicacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoUbicacion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne
    @JoinColumn(name = "sku", nullable = false)
    private Producto producto;
    
    @ManyToOne
    @JoinColumn(name = "id_ubicacion", nullable = false)
    private Ubicacion ubicacion;
    
    @Column(name = "cantidad_en_ubicacion", nullable = false)
    private Integer cantidadEnUbicacion;
}
