package com.smartlogix.pedidosservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UbicacionResponseDTO {
    private Integer idUbicacion;
    private String codigoUbicacion;
    private Integer pasillo;
    private Character piso;
    private Integer numero;
    private List<ProductoEnUbicacionDTO> productos;
    private Integer totalProductos;
    private Integer cantidadTotal;
}

