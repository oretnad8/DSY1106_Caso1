package com.smartlogix.ubicacionesservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReubicarProductoRequest {
    
    @NotBlank(message = "El SKU es requerido")
    private String sku;
    
    @NotBlank(message = "El código de ubicación origen es requerido")
    private String codigoUbicacionOrigen;
    
    @NotBlank(message = "El código de ubicación destino es requerido")
    private String codigoUbicacionDestino;
    
    @NotNull(message = "La cantidad es requerida")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    private Integer cantidad;
    
    private String motivo;
}

