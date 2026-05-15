package com.smartlogix.productosservice.dto;

import com.smartlogix.productosservice.model.Producto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoResponse {
    private String sku;
    private String descripcion;
    private Integer stock;
    private String codigoBarrasIndividual;
    private String lpn;
    private String lpnDesc;
    private LocalDate fechaVencimiento;
    private Boolean vencimientoCercano;
    private List<UbicacionStockDTO> ubicaciones;
    
    public static ProductoResponse fromEntity(Producto producto) {
        List<UbicacionStockDTO> ubicacionesDTO = producto.getUbicaciones().stream()
                .map(pu -> UbicacionStockDTO.builder()
                        .idUbicacion(pu.getUbicacion().getIdUbicacion())
                        .codigoUbicacion(pu.getUbicacion().getCodigoUbicacion())
                        .pasillo(pu.getUbicacion().getPasillo())
                        .piso(pu.getUbicacion().getPiso())
                        .numero(pu.getUbicacion().getNumero())
                        .cantidad(pu.getCantidadEnUbicacion())
                        .build())
                .collect(Collectors.toList());
        
        return ProductoResponse.builder()
                .sku(producto.getSku())
                .descripcion(producto.getDescripcion())
                .stock(producto.getStock())
                .codigoBarrasIndividual(producto.getCodigoBarrasIndividual())
                .lpn(producto.getLpn())
                .lpnDesc(producto.getLpnDesc())
                .fechaVencimiento(producto.getFechaVencimiento())
                .vencimientoCercano(producto.getVencimientoCercano())
                .ubicaciones(ubicacionesDTO)
                .build();
    }
}
