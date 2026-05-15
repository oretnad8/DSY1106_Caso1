package com.smartlogix.pedidosservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoDetalleDTO {
    private Long id;
    private String cliente;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaEntrega;
    private String estadoActual;
    private String numeroFactura;
    private String fotoEntrega;

    // Vendedor
    private ResponsableDTO vendedor;

    // Operador de picking
    private ResponsableDTO operadorPicking;

    // Operador de transporte
    private ResponsableDTO operadorTransporte;

    // Items del pedido
    private List<ItemDetalleDTO> detalles;

    // Timeline completa
    private List<HistorialEstadoDTO> historial;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponsableDTO {
        private Integer id;
        private String nombre;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemDetalleDTO {
        private String sku;
        private String descripcion;
        private Integer cantidadSolicitada;
        private Integer cantidadPickeada;
        private String ubicacionSugerida;
    }
}

