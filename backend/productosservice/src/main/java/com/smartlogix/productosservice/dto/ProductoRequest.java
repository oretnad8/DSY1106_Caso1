package com.smartlogix.productosservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoRequest {
    
    @NotBlank(message = "El SKU es obligatorio")
    @Pattern(regexp = "^[A-Z]{2}\\d{5}$", message = "SKU debe tener formato: 2 letras mayúsculas + 5 números (ej: CA30001)")
    private String sku;
    
    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;
    
    @NotNull(message = "El stock es obligatorio")
    private Integer stock;
    
    private String codigoBarrasIndividual;
    
    private String lpn;
    
    private String lpnDesc;
    
    private LocalDate fechaVencimiento;
}
