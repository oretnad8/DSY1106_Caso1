package com.smartlogix.ubicacionesservice.dto;

import com.smartlogix.ubicacionesservice.model.ProductoUbicacion;
import com.smartlogix.ubicacionesservice.model.Ubicacion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UbicacionResponse {
        private Integer idUbicacion;
        private String codigoUbicacion; // P1-A-01
        private Integer pasillo; // 1-5
        private Character piso; // A, B, C
        private Integer numero; // 1-60
        private Integer nivel;
        private Boolean esEstante;
        private List<ProductoEnUbicacionDTO> productos;
        private Integer totalProductos;
        private Integer cantidadTotal;

        public static UbicacionResponse fromEntity(Ubicacion ubicacion) {
                List<ProductoEnUbicacionDTO> productosDTO = ubicacion.getProductos().stream()
                                .map(pu -> ProductoEnUbicacionDTO.builder()
                                                .sku(pu.getProducto().getSku())
                                                .descripcion(pu.getProducto().getDescripcion())
                                                .cantidadEnUbicacion(pu.getCantidadEnUbicacion())
                                                .lpn(pu.getProducto().getLpn())
                                                .vencimientoCercano(pu.getProducto().getVencimientoCercano())
                                                .build())
                                .collect(Collectors.toList());

                Integer cantidadTotal = ubicacion.getProductos().stream()
                                .mapToInt(ProductoUbicacion::getCantidadEnUbicacion)
                                .sum();

                return UbicacionResponse.builder()
                                .idUbicacion(ubicacion.getIdUbicacion())
                                .codigoUbicacion(ubicacion.getCodigoUbicacion())
                                .pasillo(ubicacion.getPasillo())
                                .piso(ubicacion.getPiso())
                                .numero(ubicacion.getNumero())
                                .nivel(ubicacion.getNivel())
                                .esEstante(ubicacion.getEsEstante())
                                .productos(productosDTO)
                                .totalProductos(productosDTO.size())
                                .cantidadTotal(cantidadTotal)
                                .build();
        }
}
