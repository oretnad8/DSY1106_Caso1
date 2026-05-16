package com.smartlogix.productosservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "productos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    @Id
    @Column(length = 9)
    private String sku;

    @Column(length = 255, nullable = false)
    private String descripcion;

    @Column(nullable = false)
    private Integer stock = 0;

    @Column(name = "codigo_barras_individual", length = 50)
    private String codigoBarrasIndividual;

    @Column(length = 50)
    private String lpn;

    @Column(name = "lpn_desc", length = 255)
    private String lpnDesc;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(name = "vencimiento_cercano", nullable = false)
    private Boolean vencimientoCercano = false;

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductoUbicacion> ubicaciones = new ArrayList<>();

    public void calcularVencimientoCercano() {
        if (fechaVencimiento != null) {
            LocalDate dosMesesDespues = LocalDate.now().plusMonths(2);
            this.vencimientoCercano = fechaVencimiento.isBefore(dosMesesDespues);
        } else {
            this.vencimientoCercano = false;
        }
    }
}
