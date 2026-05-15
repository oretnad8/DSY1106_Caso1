package com.smartlogix.aprobacionesservice.dto;

import com.smartlogix.aprobacionesservice.model.Aprobacion;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AprobacionRequest {
    
    @NotNull(message = "El tipo de movimiento es obligatorio")
    private Aprobacion.TipoMovimiento tipoMovimiento;
    
    @NotBlank(message = "El SKU es obligatorio")
    private String sku;
    
    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    private Integer cantidad;
    
    @NotBlank(message = "El motivo es obligatorio")
    private String motivo;
    
    private Integer idUbicacionOrigen;
    
    private Integer idUbicacionDestino;
}
