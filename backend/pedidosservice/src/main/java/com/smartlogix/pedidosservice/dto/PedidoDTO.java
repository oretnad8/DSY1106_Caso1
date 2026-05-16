package com.smartlogix.pedidosservice.dto;

import com.smartlogix.pedidosservice.model.EstadoPedido;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Updated to use new EstadoPedido.
 * This DTO might be legacy but valid if updated.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoDTO {
    private Long id; // NV
    private String cliente;
    private LocalDateTime fechaCreacion;
    private EstadoPedido estado;
    private EstadoPedido estadoActual; // For backward compatibility with React

    // Asignación
    private Integer vendedorId;
    private String vendedorNombre;
    private Integer operadorId;
    private String operadorNombre;

    // Transporte
    private String transportistaNombre;
    private String fotoEvidencia;
    private String numeroFactura;
    private LocalDateTime fechaEntrega;
    private Integer totalItems;
}

