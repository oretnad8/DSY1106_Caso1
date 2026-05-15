package com.smartlogix.pedidosservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EgresoProductoRequestDTO {
    private String sku;
    private String codigoUbicacion;
    private Integer cantidad;
    private String motivo;
}

