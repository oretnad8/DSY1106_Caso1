package com.smartlogix.ubicacionesservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReubicarProductoResponse {
    private String mensaje;
    private String sku;
    private String codigoUbicacionOrigen;
    private String codigoUbicacionDestino;
    private Integer cantidadMovida;
    private Integer cantidadRestanteEnOrigen;
    private Integer cantidadEnDestino;
}

