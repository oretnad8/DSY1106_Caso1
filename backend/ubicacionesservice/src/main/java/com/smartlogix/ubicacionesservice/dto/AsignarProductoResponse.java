package com.smartlogix.ubicacionesservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsignarProductoResponse {
    private String mensaje;
    private String sku;
    private String codigoUbicacion;
    private Integer cantidadAsignada;
    private Integer stockRestante;
}
