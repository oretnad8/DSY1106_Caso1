package com.smartlogix.pedidosservice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class HojaPickingDTO {
    private Long pedidoId;
    private String cliente;
    private LocalDateTime fechaCreacion;
    private String estado;
    private List<ItemPickingDTO> items;

    @Data
    @Builder
    public static class ItemPickingDTO {
        private String sku;
        private String descripcion;
        private Integer cantidadSolicitada;
        private Integer cantidadPickeada;
        private String ubicacionSugerida;
    }
}

