package com.smartlogix.pedidosservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // NV

    private String cliente;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaEntrega;

    @Column(nullable = true)
    private String numeroFactura;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String fotoEntrega;

    private Integer vendedorId;
    private String vendedorNombre;

    @Enumerated(EnumType.STRING)
    private EstadoPedido estadoActual;

    private Integer operadorId;
    private String operadorNombre;

    private Integer transportistaId;
    private String transportistaNombre;

    @Builder.Default
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePedido> detalles = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HistorialEstado> historial = new ArrayList<>();

    // Helper methods
    public void addDetalle(DetallePedido detalle) {
        detalles.add(detalle);
        detalle.setPedido(this);
    }

    public void addHistorial(HistorialEstado historialItem) {
        historial.add(historialItem);
        historialItem.setPedido(this);
    }
}

