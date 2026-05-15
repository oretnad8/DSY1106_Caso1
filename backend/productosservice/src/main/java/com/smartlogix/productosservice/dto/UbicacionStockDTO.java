package com.smartlogix.productosservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UbicacionStockDTO {
    private Integer idUbicacion;
    private String codigoUbicacion; // P1-A-01
    private Integer pasillo; // 1-5
    private Character piso; // A, B, C
    private Integer numero; // 1-60
    private Integer cantidad;
}
