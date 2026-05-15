package com.smartlogix.ubicacionesservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsignarProductoRequest {
    
    @NotBlank(message = "El SKU es obligatorio")
    private String sku;
    
    @NotBlank(message = "El código de ubicación es obligatorio")
    private String codigoUbicacion;
    
    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    private Integer cantidad;
}
