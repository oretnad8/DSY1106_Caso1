package com.smartlogix.ubicacionesservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoEnUbicacionDTO {
    private String sku;
    private String descripcion;
    private Integer cantidadEnUbicacion;
    private String lpn;
    private Boolean vencimientoCercano;
}
